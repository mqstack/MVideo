package org.mqstack.mvideo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.mqstack.mvideo.camera2.Camera2Activity;
import org.mqstack.mvideo.drawbitmap.DrawBitmapActivity;
import org.mqstack.mvideo.record.CameraPreviewActivity;
import org.mqstack.mvideo.textureview.TextureViewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        videoView = (MVideoView) findViewById(R.id.mVideo);
    }

    public void drawBitmapClick(View view) {
        startActivity(new Intent(MainActivity.this, DrawBitmapActivity.class));
    }

    public void textureViewPlayerClick(View view) {
        startActivity(new Intent(MainActivity.this, TextureViewActivity.class));
    }

    public void camerapreviewClick(View view) {
        startActivity(new Intent(MainActivity.this, CameraPreviewActivity.class));
    }

    public void camerapreview2Click(View view) {
        startActivity(new Intent(MainActivity.this, Camera2Activity.class));
    }

}
