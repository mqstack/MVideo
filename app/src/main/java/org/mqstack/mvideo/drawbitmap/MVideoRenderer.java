package org.mqstack.mvideo.drawbitmap;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import org.mqstack.mvideo.R;
import org.mqstack.mvideo.util.L;
import org.mqstack.mvideo.util.ShaderHelper;
import org.mqstack.mvideo.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by mq on 16/6/12.
 */

public class MVideoRenderer implements GLSurfaceView.Renderer {

    private GLSurfaceView surfaceView;

    private VideoFrame frame;

    private boolean isResume = false;

    private RenderContext renderContext;

    private int[] textures = new int[1];

    private int program = -1;
    private int vertexShader;
    private int fragmentShader;

    private int viewWidth;
    private int viewHeight;

    public MVideoRenderer(GLSurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public void setFrame(VideoFrame frame) {
        this.frame = frame;
    }

    public void resume() {
        isResume = true;
    }

    public void pause() {
        isResume = false;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        L.d("onSurfaceCreated");
        GLES20.glGenTextures(1, textures, 0);
        if (textures[0] == GLES20.GL_FALSE)
            throw new RuntimeException("Error loading texture");

        // bind the texture and set parameters
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load a bitmap from resources folder and pass it to OpenGL
        // in the end, we recycle it to free unneeded resources
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, frame.bitmap, 0);
//        renderContext = RendererUtil.createProgram();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewWidth = width;
        viewHeight = height;
        useProgram();
        bindTexture();
    }

    private void useProgram() {
        if (program != -1) {
            GLES20.glDeleteProgram(program);
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(fragmentShader);
            GLES20.glDeleteTextures(textures.length, textures, 0); // free the texture!
        }
//        program = ShaderHelper.buildProgram(
//                TextResourceReader.readTextFileFromResource(surfaceView.getContext(), R.raw.vertex_shader),
//                TextResourceReader.readTextFileFromResource(surfaceView.getContext(), R.raw.fragment_shader));
        vertexShader = ShaderHelper.compileVertexShader(
                TextResourceReader.readTextFileFromResource(surfaceView.getContext(), R.raw.vertex_shader));
        fragmentShader = ShaderHelper.compileFragmentShader(
                TextResourceReader.readTextFileFromResource(surfaceView.getContext(), R.raw.fragment_shader));

        //Link them into a program
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        ShaderHelper.validateProgram(program);
        GLES20.glUseProgram(program);
    }

    private void bindTexture() {
        // discover the 'position' of the uScreen and uTexture
        int uScreenPos = GLES20.glGetUniformLocation(program, "uScreen");
        int uTexture = GLES20.glGetUniformLocation(program, "uTexture");

// The uScreen matrix
// This is explained in detail in the Triangle2d sample.
        float[] uScreen =
                {
                        2f / viewWidth, 0f, 0f, 0f,
                        0f, -2f / viewHeight, 0f, 0f,
                        0f, 0f, 0f, 0f,
                        -1f, 1f, 0f, 1f
                };

// Now, let's set the value.
        FloatBuffer b = ByteBuffer.allocateDirect(uScreen.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        b.put(uScreen).position(0);
        GLES20.glUniformMatrix4fv(uScreenPos, b.limit() / uScreen.length, false, b);

// Activate the first texture (GL_TEXTURE0) and bind it to our handle
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glUniform1i(uTexture, 0);

// set the viewport and a fixed, white background
        GLES20.glViewport(0, 0, viewWidth, viewHeight);
        GLES20.glClearColor(1f, 1f, 1f, 1f);

// since we're using a PNG file with transparency, enable alpha blending.
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        surfaceView.requestRender();
//        if (isResume && frame != null) {
//            RendererUtil.renderBackground();
//            RendererUtil.renderTexture(renderContext, frame.getTexture(),
//                    frame.getWidth(), frame.getHeight());
//        }

        // get the position of our attributes
        int aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        int aTexPos = GLES20.glGetAttribLocation(program, "aTexPos");

// Ok, now is the FUN part.
// First of all, our image is a rectangle right? but in OpenGL, we can only draw
// triangles! To remedy that we will use 4 vertices (V1 to V4) and draw using
// the TRIANGLE_STRIP option. If you look closely to our positions, you will note
// that we're drawing a 'N' (or 'Z') shaped line... and TRIANGLE_STRIP 'closes' the
// remaining GAP between the vertices, so we have a rectangle (or square)! Yay!
//
// Apart from V1 to V4, we also specify the position IN THE TEXTURE. Each vertex
// of our rectangle must relate to a position in the texture. The texture coordinates
// are ALWAYS 0,0 on bottom-left and 1,1 on top-right. Take a look at the values
// used and you will understand it easily. If not, mess a little bit with the values
// and take a look at the result.
        float[] data =
                {
                        0, 0,  //V1
                        0f, 0f,     //Texture coordinate for V1

                        0, viewWidth * 9 / 16,  //V2
                        0f, 1f,

                        viewWidth, 0, //V3
                        1f, 0f,

                        viewWidth, viewWidth * 9 / 16,  //V4
                        1f, 1f
                };

// constants. You know the drill by now.
        final int FLOAT_SIZE = 4;
        final int POSITION_SIZE = 2;
        final int TEXTURE_SIZE = 2;
        final int TOTAL_SIZE = POSITION_SIZE + TEXTURE_SIZE;
        final int POSITION_OFFSET = 0;
        final int TEXTURE_OFFSET = 2;

// Again, a FloatBuffer will be used to pass the values
        FloatBuffer b = ByteBuffer.allocateDirect(data.length * FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
        b.put(data);

// Position of our image
        b.position(POSITION_OFFSET);
        GLES20.glVertexAttribPointer(aPosition, POSITION_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
        GLES20.glEnableVertexAttribArray(aPosition);

// Positions of the texture
        b.position(TEXTURE_OFFSET);
        GLES20.glVertexAttribPointer(aTexPos, TEXTURE_SIZE, GLES20.GL_FLOAT, false, TOTAL_SIZE * FLOAT_SIZE, b);
        GLES20.glEnableVertexAttribArray(aTexPos);

// Clear the screen and draw the rectangle
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

}
