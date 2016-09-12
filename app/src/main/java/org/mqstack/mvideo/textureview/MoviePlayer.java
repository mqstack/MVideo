package org.mqstack.mvideo.textureview;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Surface;

import org.mqstack.mvideo.util.L;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by mq on 16/8/22.
 */

public class MoviePlayer {

    private File mSourceFile;
    private Surface mOutputSurface;
    FrameCallback mFrameCallback;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mLoop;
    private volatile boolean mIsStopRequested;

    // Declare this here to reduce allocations.
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();


    public MoviePlayer(File source, Surface outputSurface, FrameCallback callback) throws IOException {
        mSourceFile = source;
        mOutputSurface = outputSurface;
        mFrameCallback = callback;

        MediaExtractor extractor = null;

        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(source.toString());
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + mSourceFile);
            }
            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);
            mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
            mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
        } finally {
            if (extractor != null) {
                extractor.release();
            }
        }
    }

    public void play() throws IOException {
        MediaExtractor extractor = null;
        MediaCodec decoder = null;

        if (!mSourceFile.canRead()) {
            throw new FileNotFoundException("File can not read.");
        }

        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(mSourceFile.toString());
            int index = selectTrack(extractor);
            if (index < 0) {
                throw new RuntimeException("No video track.");
            }
            extractor.selectTrack(index);
            MediaFormat format = extractor.getTrackFormat(index);
            String type = format.getString(MediaFormat.KEY_MIME);

            decoder = MediaCodec.createDecoderByType(type);
            decoder.configure(format, mOutputSurface, null, 0);
            decoder.start();
            doExtract(extractor, index, decoder, mFrameCallback);
        } finally {
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
        }
    }

    private void doExtract(MediaExtractor extractor, int trackIndex,
                           MediaCodec decoder, FrameCallback mFrameCallback) {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decodeInputBuffers = decoder.getInputBuffers();
        int inputChunck = 0;
        long firstInputTimeNsec = -1;

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {

            if (mIsStopRequested) {
                L.d("Stop requested.");
                return;
            }

            if (!inputDone) {
                int inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufferIndex >= 0) {
                    if (firstInputTimeNsec == -1) {
                        firstInputTimeNsec = System.nanoTime();
                    }
                    ByteBuffer inputBuf = decodeInputBuffers[inputBufferIndex];
                    int chunckSize = extractor.readSampleData(inputBuf, 0);
                    if (chunckSize < 0) {
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        L.d("End of stream.");
                    } else {
                        if (extractor.getSampleTrackIndex() != trackIndex) {
                            L.d("Track index wrong" + extractor.getSampleTrackIndex() + "--expected:" + trackIndex);
                        }
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufferIndex, 0, chunckSize, presentationTimeUs, 0);
                        L.d("Submit frame" + inputChunck + " to dec, size" + chunckSize);
                        inputChunck++;
                        extractor.advance();
                    }
                } else {
                    L.d("Input buffer not available.");
                }
            }

            if (!outputDone) {
                int decoderStatus = decoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    L.d("No output from decoder yet.");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    L.d("Decoder output buffer changed.");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat format = decoder.getOutputFormat();
                    L.d("Decoder output format changed " + format);
                } else if (decoderStatus < 0) {
                    throw new RuntimeException("unexpected output for decoder, status " + decoderStatus);
                }else{
                    if(firstInputTimeNsec != 0){
                        long nowNsec = System.nanoTime();
                        L.d("First frame lag:" + (nowNsec - firstInputTimeNsec));
                        firstInputTimeNsec = 0;
                    }

                    boolean doLoop = false;
                    L.d("Surface decoder give buffer, status " + decoderStatus + ", size " + mBufferInfo.size);
                    if((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                        L.d("Output EOS.");
                        if(mLoop){
                            doLoop = true;
                        }else{
                            outputDone = true;
                        }
                    }

                    boolean doRender = mBufferInfo.size != 0;
                    if(doRender && mFrameCallback != null){
                        mFrameCallback.preRender(mBufferInfo.presentationTimeUs);
                    }
                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                    if(doRender && mFrameCallback != null){
                        mFrameCallback.postRender();
                    }

                    if(doLoop){
                        L.d("Reached EOS, do loop.");
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        inputDone = false;
                        decoder.flush();
                        mFrameCallback.loopReset();
                    }
                }
            }
        }

    }

    private int selectTrack(MediaExtractor extractor) {
        int trackNums = extractor.getTrackCount();
        for (int i = 0; i < trackNums; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                L.d("Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }
        return -1;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public void setLoopMode(boolean loop) {
        this.mLoop = loop;
    }

    public void requestStop() {
        mIsStopRequested = true;
    }

    /**
     * Callback invoked when rendering video frames.  The MoviePlayer client must
     * provide one of these.
     */
    public interface FrameCallback {
        /**
         * Called immediately before the frame is rendered.
         *
         * @param presentationTimeUsec The desired presentation time, in microseconds.
         */
        void preRender(long presentationTimeUsec);

        /**
         * Called immediately after the frame render call returns.  The frame may not have
         * actually been rendered yet.
         * TODO: is this actually useful?
         */
        void postRender();

        /**
         * Called after the last frame of a looped movie has been rendered.  This allows the
         * callback to adjust its expectations of the next presentation time stamp.
         */
        void loopReset();
    }

    /**
     * Interface to be implemented by class that manages playback UI.
     * <p>
     * Callback methods will be invoked on the UI thread.
     */
    public interface PlayerFeedback {
        void playbackStopped();
    }
}
