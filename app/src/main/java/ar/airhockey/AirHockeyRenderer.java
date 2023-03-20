package ar.airhockey;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import ar.airhockey.utils.LoggerConfig;
import ar.airhockey.utils.ShaderHelper;
import ar.airhockey.utils.TextResourceReader;


public class AirHockeyRenderer implements Renderer {

    private final Context context;
    private static final int  BYTES_PER_FLOAT = 4;
    private final FloatBuffer vertexData;
    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private int program;
    int aColorLocation, uPointSizeLocation, aPositionLocation,uMatrixLocation;
    private final float[] projectionMatrix = new float [16];
    private final float[] modelMatrix =  new float[16];

    public AirHockeyRenderer(Context context){
        this.context = context;
        float[] tableVerticesWithTriangles = {
                // Triangle Fan x, y, r, g, b
                .0f, .0f, 1f,1f,1f,
                -.5f, -.8f, .75f, .75f, .75f,
                .5f, -.8f, .75f, .75f, .75f,
                .5f, .8f, .75f, .75f, .75f,
                -.5f, .8f, .75f, .75f, .75f,
                -.5f, -.8f, .75f, .75f, .75f,

                -.5f, -.8f, .0f, .0f, .0f, .5f, -.8f, .0f, .0f, .0f, // bottom line
                .5f, -.8f, .0f, .0f, .0f, .5f, .8f, .0f, .0f, .0f, // right line
                .5f, .8f, .0f, .0f, .0f, -.5f, .8f, .0f, .0f, .0f,// top line
                -.5f, .8f, .0f, .0f, .0f, -.5f, -.8f, .0f, .0f, .0f, // left line
                -.5f, .0f, 1f, .0f, .0f, .5f, .0f, 1f, .0f, .0f, // middle line

                .0f, -.4f, .0f, .0f, 1f, // first mallet
                .0f, .4f, 1f, .0f, .0f, // second mallet
                .0f, .0f, 0f, .0f, .0f // puck

        };

        vertexData = ByteBuffer.allocateDirect(
                tableVerticesWithTriangles.length*BYTES_PER_FLOAT).order(
                ByteOrder.nativeOrder()).asFloatBuffer();
        vertexData.put(tableVerticesWithTriangles);
    }
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.6f, 0.6f, 0.6f, 1.0f);

        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context,
                R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context,
                R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if(LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }
        glUseProgram(program);

        uPointSizeLocation = glGetUniformLocation(program, "u_PointSize");
        aColorLocation = glGetAttribLocation(program, "a_Color");
        aPositionLocation = glGetAttribLocation(program, "a_Position");
        uMatrixLocation = glGetUniformLocation(program, "u_Matrix");


        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,GL_FLOAT,
                false, STRIDE  , vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aColorLocation,COLOR_COMPONENT_COUNT,GL_FLOAT,
                false, STRIDE  , vertexData);
        glEnableVertexAttribArray(aColorLocation);

    }


    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);
        final float aspectRatio = width > height ?
                (float) width / (float) height:
                (float) height / (float) width;
        if (width > height){
            //Landscape
            orthoM(projectionMatrix, 0,-aspectRatio, aspectRatio, -1f,1f,-1f,1f);
        }else{
            //Portrait or square
            orthoM(projectionMatrix, 0,-1f,1f,-aspectRatio, aspectRatio, -1f,1f);
        }
        glUniformMatrix4fv(uMatrixLocation,1,false,projectionMatrix,0);
        MatrixHelper.perspectiveM(projectionMatrix,45,(float) width / (float) height,
                1f,10f);
        setIdentityM(modelMatrix,0);
        if (height > width)
            translateM(modelMatrix, 0, 0f, 0f, -4f);
        else
            translateM(modelMatrix, 0, 0f, 0f, -2f);
        rotateM(modelMatrix,0,-60f,1f,0f,0f);
        float[] temp = new float[16];
        multiplyMM(temp,0,projectionMatrix,0,modelMatrix,0);
        System.arraycopy(temp, 0 , projectionMatrix, 0 , temp.length);
        glUniformMatrix4fv(uMatrixLocation,1,false,projectionMatrix,0);
    }


    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);
        //Triangles
        // Table
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
        // Border
        glDrawArrays(GL_LINES, 6, 10);
        // Mallets
        glUniform1f(uPointSizeLocation, 30f);
        glDrawArrays(GL_POINTS, 16, 1);
        glDrawArrays(GL_POINTS, 17, 1);
        // Puck
        glUniform1f(uPointSizeLocation, 15f);
        glDrawArrays(GL_POINTS, 18, 1);



    }
}