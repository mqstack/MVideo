package org.mqstack.mvideo.util;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import org.mqstack.mvideo.drawbitmap.RenderContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;

/**
 * Created by mq on 16/6/12.
 */

public class RendererUtil {

    private static final float[] TEX_VERTICES = {
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f
    };

    private static final float[] POS_VERTICES = {
            -1.0f, -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f, 1.0f
    };

    private static final String VERTEX_SHADER = "attribute vec4 a_position;\n"
            + "attribute vec2 a_texcoord;\n"
            + "uniform mat4 u_model_view; \n"
            + "varying vec2 v_texcoord;\n"
            + "void main() {\n" + "  gl_Position = u_model_view*a_position;\n"
            + "  v_texcoord = a_texcoord;\n" + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "uniform sampler2D tex_sampler;\n"
            + "uniform float alpha;\n"
            + "varying vec2 v_texcoord;\n"
            + "void main() {\n"
            + "vec4 color = texture2D(tex_sampler, v_texcoord);\n"
            + "gl_FragColor = color;\n"
            + "}\n";

    private static final int FLOAT_SIZE_BYTES = 4;

    public static int createTexture(Bitmap bitmap) {
        int texture = createTexture();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        int internalFormat = GLUtils.getInternalFormat(bitmap);
        int type = GLUtils.getType(bitmap);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, internalFormat, bitmap, type, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGLError("teximage2d");
        return texture;
    }

    public static int createTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(textures.length, textures, 0);
        checkGLError("glGenTextures");
        return textures[0];
    }

    public static void checkGLError(String name) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            L.d("name:" + name + "--gl error: " + error);
            Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
            StackTraceElement[] currentElements = traces.get(Thread.currentThread());
            for (StackTraceElement e : currentElements) {
                L.d(e.toString() + "\n");
            }
        }
    }

    public static void renderBackground() {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    public static RenderContext createProgram() {
        return createProgram(POS_VERTICES, TEX_VERTICES);
    }

    public static RenderContext createProgram(float[] pos, float[] tex) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        if (vertexShader == 0) {
            return null;
        }
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        if (fragmentShader == 0) {
            return null;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGLError("glAttachShader");
            GLES20.glAttachShader(program, fragmentShader);
            checkGLError("glAttachShader");

            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                String info = GLES20.glGetProgramInfoLog(program);
                GLES20.glDeleteProgram(program);
                program = 0;
                L.d("Could not link program:" + info);
            }
        }
        // Bind attributes and uniforms
        RenderContext context = new RenderContext();
        context.texSamplerHandle = GLES20.glGetUniformLocation(program,
                "tex_sampler");
        context.alphaHandle = GLES20.glGetUniformLocation(program, "alpha");
        context.texCoordHandle = GLES20.glGetAttribLocation(program,
                "a_texcoord");
        context.posCoordHandle = GLES20.glGetAttribLocation(program,
                "a_position");
        context.modelViewMatHandle = GLES20.glGetUniformLocation(program,
                "u_model_view");
        context.texVertices = createVerticesBuffer(tex);
        context.posVertices = createVerticesBuffer(pos);

        context.shaderProgram = program;
        return context;

    }

    public static void renderTexture(RenderContext context, int texture, int width, int height) {
        //Use shader program
        GLES20.glUseProgram(context.shaderProgram);
        if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            createProgram();
            checkGLError("createProgram");
        }

        //Set view point
        GLES20.glViewport(0, 0, width, height);
        checkGLError("glViewport");

        //Disable bending
        GLES20.glDisable(GLES20.GL_BLEND);

        // Set the vertex attributes
        GLES20.glVertexAttribPointer(context.texCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, context.texVertices);
        GLES20.glEnableVertexAttribArray(context.texCoordHandle);
        GLES20.glVertexAttribPointer(context.posCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, context.posVertices);
        GLES20.glEnableVertexAttribArray(context.posCoordHandle);
        checkGLError("vertex attribute setup");

        // Set the input texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGLError("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGLError("glBindTexture");
        GLES20.glUniform1i(context.texSamplerHandle, 0);
        GLES20.glUniform1f(context.alphaHandle, context.alpha);
        GLES20.glUniformMatrix4fv(context.modelViewMatHandle, 1, false, context.mModelViewMat,
                0);
        checkGLError("modelViewMatHandle");
        // Draw!
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFinish();
    }

    public static FloatBuffer createVerticesBuffer(float[] vertices) {
        if (vertices.length != 8) {
            throw new RuntimeException("vertices num need 4");
        }

        FloatBuffer buffer = ByteBuffer
                .allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(vertices);
        return buffer;

    }

    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                String info = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                shader = 0;
                L.d("Could not load shader" + shaderType + "info: "
                        + info);
            }
        }
        return shader;
    }
}
