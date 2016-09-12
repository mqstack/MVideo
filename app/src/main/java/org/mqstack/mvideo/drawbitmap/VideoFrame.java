package org.mqstack.mvideo.drawbitmap;

import android.graphics.Bitmap;

import org.mqstack.mvideo.util.RendererUtil;

/**
 * Hold texture
 * Should called in gl thread
 * <p>
 * Created by mq on 16/6/12.
 */

public class VideoFrame {

    public int texture = -1;
    public int width;
    public int height;
    public Bitmap bitmap;


    public VideoFrame(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        this.bitmap = bitmap;
        setTexture(RendererUtil.createTexture(bitmap), bitmap.getWidth(), bitmap.getHeight());
    }

    private void setTexture(int texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

}
