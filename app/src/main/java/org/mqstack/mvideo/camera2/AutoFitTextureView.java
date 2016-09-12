package org.mqstack.mvideo.camera2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by mq on 16/9/9.
 */

public class AutoFitTextureView extends TextureView {

    private int aWidth;
    private int aHeight;

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width or height can not be negative.");
        }
        aWidth = width;
        aHeight = height;
        requestLayout();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (aWidth == 0 || aHeight == 0) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * aWidth / aHeight) {
                setMeasuredDimension(width, width * aHeight / aWidth);
            } else {
                setMeasuredDimension(height * aWidth / aHeight, height);
            }
        }


    }
}
