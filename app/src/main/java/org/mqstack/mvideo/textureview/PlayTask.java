package org.mqstack.mvideo.textureview;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;

/**
 * Created by mq on 16/8/23.
 */

public class PlayTask implements Runnable {

    private static final int STOP_PLAY = 0;
    private MoviePlayer mPlayer;
    private MoviePlayer.PlayerFeedback mFeedback;
    private LocalHander mHander;
    private Thread mThread;

    private boolean mLoop = false;
    private boolean isStoped;
    private Object stopLock = new Object();

    public PlayTask(MoviePlayer player, MoviePlayer.PlayerFeedback feedback) {
        this.mPlayer = player;
        this.mFeedback = feedback;
        this.mHander = new LocalHander();
    }

    @Override
    public void run() {
        try {
            mPlayer.play();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            synchronized (stopLock) {
                isStoped = true;
                stopLock.notifyAll();
            }
            mHander.sendMessage(mHander.obtainMessage(STOP_PLAY, mFeedback));
        }
    }

    public void setLoopMode(boolean loop) {
        this.mLoop = loop;
    }

    public void execute() {
        mPlayer.setLoopMode(mLoop);
        mThread = new Thread(this, "New Play Thread");
        mThread.start();
    }

    public void requestStop() {
        mPlayer.requestStop();
    }

    public void waitForStop() {
        synchronized (stopLock) {
            while (!isStoped) {
                try {
                    stopLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class LocalHander extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STOP_PLAY:
                    MoviePlayer.PlayerFeedback feedback = (MoviePlayer.PlayerFeedback) msg.obj;
                    feedback.playbackStopped();
                    break;
                default:
                    throw new RuntimeException("Unknown msg " + msg.what);
            }
        }
    }
}
