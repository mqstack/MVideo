package org.mqstack.mvideo.textureview;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.CheckBox;

import org.mqstack.mvideo.R;
import org.mqstack.mvideo.bind.Bind;
import org.mqstack.mvideo.bind.DataBind;
import org.mqstack.mvideo.bind.OnClick;
import org.mqstack.mvideo.util.L;

import java.io.File;
import java.io.IOException;

/**
 * Created by mq on 16/6/29.
 */

public class TextureViewActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, MoviePlayer.PlayerFeedback {

    @Bind(R.id.movie_texture_view)
    TextureView mTextureView;

    private PlayTask mPlayTask;

    @Bind(R.id.loopPlayback_checkbox)
    CheckBox loopCheckBox;

    @Bind(R.id.locked60fps_checkbox)
    CheckBox fpsCheckBox;

    @Bind(R.id.play_stop_button)
    Button playButton;

    private boolean showStopLabel = false;
    private boolean mSurfaceTextureReady;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textureview_player);
        DataBind.bind(this);
        mTextureView.setSurfaceTextureListener(this);
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayTask != null) {
            stopPlayBack();
            mPlayTask.waitForStop();
        }
    }

    @OnClick(R.id.play_stop_button)
    public void clickPlayStop() {
        if (showStopLabel) {
            stopPlayBack();
        } else {
            if(mPlayTask != null){
                return;
            }
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/1.mp4";
            SpeedControlCallback callback = new SpeedControlCallback();
            if (((CheckBox) findViewById(R.id.locked60fps_checkbox)).isChecked()) {
                callback.setFixedPlaybackRate(60);
            }
            SurfaceTexture st = mTextureView.getSurfaceTexture();
            Surface surface = new Surface(st);
            MoviePlayer player = null;
            try {
                player = new MoviePlayer(new File(filePath), surface, callback);
            } catch (IOException e) {
                e.printStackTrace();
                L.d("unable to play movie");
                surface.release();
                return;
            }
            adjustRatio(player.getVideoWidth(), player.getVideoHeight());
            mPlayTask = new PlayTask(player, this);
            if (loopCheckBox.isChecked()) {
                mPlayTask.setLoopMode(true);
            }
            showStopLabel = true;

            updateUI();
            mPlayTask.execute();
        }
    }

    private void stopPlayBack(){
        if (mPlayTask != null) {
            mPlayTask.requestStop();
        }
    }

    private void adjustRatio(int width, int height) {
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        double ratio = (double) height / width;

        int newWidth, newHeight;
        if (viewHeight > viewWidth * ratio) {
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * ratio);
        } else {
            newHeight = viewHeight;
            newWidth = (int) (viewHeight / ratio);
        }

        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;

        Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }

    private void updateUI() {
        if (showStopLabel) {
            playButton.setText(getString(R.string.stop));
        } else {
            playButton.setText(getString(R.string.play));
        }
        playButton.setEnabled(mSurfaceTextureReady);
        loopCheckBox.setEnabled(!showStopLabel);
        fpsCheckBox.setEnabled(!showStopLabel);

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTextureReady = true;
        updateUI();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurfaceTextureReady = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void playbackStopped() {
        showStopLabel = false;
        mPlayTask = null;
        updateUI();
    }
}
