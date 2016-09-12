package org.mqstack.mvideo.drawbitmap;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by mq on 16/6/12.
 */

public class MVideoView extends GLSurfaceView {

    private MVideoRenderer renderer;

    public MVideoView(Context context) {
        this(context, null);
    }

    public MVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusableInTouchMode(true);
        renderer = new MVideoRenderer(this);
        setEGLContextClientVersion(2);
        setRenderer(renderer);
//        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setVideoFrame(VideoFrame frame) {
        renderer.setFrame(frame);
    }

    @Override
    public void onResume() {
        super.onResume();
        renderer.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        renderer.pause();
    }

}
