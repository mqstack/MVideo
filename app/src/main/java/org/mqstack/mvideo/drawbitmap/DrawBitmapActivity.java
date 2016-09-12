package org.mqstack.mvideo.drawbitmap;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.mqstack.mvideo.R;

/**
 * Created by mq on 16/6/29.
 */

public class DrawBitmapActivity extends AppCompatActivity {

    private MVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        videoView = (MVideoView) findViewById(R.id.mVideo);
        videoView = new MVideoView(this);
        videoView.setVideoFrame(new VideoFrame(BitmapFactory.decodeResource(getResources(), R.mipmap.test)));
        videoView.requestRender();
        setContentView(videoView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.onPause();
    }

//    private GLSurfaceView surface;
//    private TextureRenderer renderer;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//
//        // If you don't know what we're doing here, take a look at the
//        // epilepsy sample.
//        surface = new GLSurfaceView(this);
//        renderer = new TextureRenderer();
//        surface.setEGLContextClientVersion(2);
//        surface.setRenderer(renderer);
//
//        setContentView(surface);
//    }
//
//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        surface.onResume();
//    }
//
//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        surface.onPause();
//        renderer.tearDown();
//    }
}
