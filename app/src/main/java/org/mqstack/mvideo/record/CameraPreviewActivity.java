package org.mqstack.mvideo.record;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import org.mqstack.mvideo.R;
import org.mqstack.mvideo.util.L;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mq on 16/9/2.
 */

public class CameraPreviewActivity extends AppCompatActivity {

    //    private Camera mCamera;
    private Preview preview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        preview = new Preview(this);
        preview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        ((LinearLayout) findViewById(R.id.content)).addView(preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preview.initCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.pause();
    }

    public void captureClick(View view) {
        preview.mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

            }
        }, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                camera.startPreview();
                new AsyncTask<byte[], Void, Void>() {
                    @Override
                    protected Void doInBackground(byte[]... params) {
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/" + System.currentTimeMillis() + ".jpg");
                        try {
                            FileOutputStream os = new FileOutputStream(file);
                            os.write(params[0]);
                            os.flush();
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;

                    }
                }.execute(data);
            }
        });
    }

}
