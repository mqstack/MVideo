package org.mqstack.mvideo.drawbitmap;

import java.nio.FloatBuffer;

/**
 * Created by mq on 16/6/13.
 */

public class RenderContext {

    public int shaderProgram;
    public int texSamplerHandle;
    public int alphaHandle;
    public int texCoordHandle;
    public int posCoordHandle;
    public FloatBuffer texVertices;
    public FloatBuffer posVertices;
    public float alpha = 1f;
    public int modelViewMatHandle;
    public float[] mModelViewMat = {
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0,
            0, 1
    };
}
