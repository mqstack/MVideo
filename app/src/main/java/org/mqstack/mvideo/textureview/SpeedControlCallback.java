package org.mqstack.mvideo.textureview;

import org.mqstack.mvideo.util.L;

/**
 * Created by mq on 16/8/22.
 */

public class SpeedControlCallback implements MoviePlayer.FrameCallback {

    private static final long ONE_MILLION = 1000000L;
    private static boolean CHECK_SLEEP_TIME = true;

    private long mFixedFrameDurationUsec;
    private long mPrevPresentUsec;
    private long mPrevMonoUsec;
    private boolean mLoopReset;

    /**
     * Sets a fixed playback rate.  If set, this will ignore the presentation time stamp
     * in the video file.  Must be called before playback thread starts.
     */
    public void setFixedPlaybackRate(int fps) {
        mFixedFrameDurationUsec = ONE_MILLION / fps;
    }

    @Override
    public void preRender(long presentationTimeUsec) {
        // For the first frame, we grab the presentation time from the video
        // and the current monotonic clock time.  For subsequent frames, we
        // sleep for a bit to try to ensure that we're rendering frames at the
        // pace dictated by the video stream.
        //
        // If the frame rate is faster than vsync we should be dropping frames.  On
        // Android 4.4 this may not be happening.


        if(mPrevMonoUsec == 0){
            mPrevMonoUsec = System.nanoTime() / 1000;
            mPrevPresentUsec = presentationTimeUsec;
        }else{
            long frameDelta;
            if(mLoopReset){
                mPrevPresentUsec = presentationTimeUsec - ONE_MILLION / 30;
                mLoopReset = false;
            }

            if(mFixedFrameDurationUsec != 0){
                frameDelta = mFixedFrameDurationUsec;
            }else{
                frameDelta = presentationTimeUsec - mPrevPresentUsec;
            }

            if(frameDelta < 0){
                L.d("Frame delta should > 0");
                frameDelta = 0;
            }else if(frameDelta == 0){
                L.d("Current frame and previous frame are the same time.");

            }else if(frameDelta > 10 * ONE_MILLION){
                L.d("Inter-frame pause was " + frameDelta / ONE_MILLION
                        + "secs, capping at 5 secs.");
                frameDelta = 5 * ONE_MILLION;
            }

            long desiredUsec = mPrevMonoUsec + frameDelta;
            long nowUsec = System.nanoTime() / 1000;
            while(nowUsec < desiredUsec - 100){
                long sleepTime = desiredUsec - nowUsec;
                if(sleepTime > 500000){
                    sleepTime = 500000;
                }

                try{
                    if(CHECK_SLEEP_TIME){
                        long startNsec = System.nanoTime();
                        Thread.sleep(sleepTime / 1000, (int)(sleepTime % 1000 * 1000));

                        long actualSleepNsec = System.nanoTime() - startNsec;
                        L.d("Sleep time : " + sleepTime + ", actual time : " + actualSleepNsec / 1000);
                    }else{
                        Thread.sleep(sleepTime / 1000, (int)(sleepTime % 1000 * 1000));
                    }
                }catch (InterruptedException e){

                }
                nowUsec = System.nanoTime() / 1000;
            }
            mPrevMonoUsec += frameDelta;
            mPrevPresentUsec += frameDelta;
        }


    }

    @Override
    public void postRender() {

    }

    @Override
    public void loopReset() {
        mLoopReset = true;
    }
}
