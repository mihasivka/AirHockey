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

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import ar.airhockey.objects.Mallet;
import ar.airhockey.objects.Table;
import ar.airhockey.programs.ColorShaderProgram;
import ar.airhockey.programs.TextureShaderProgram;
import ar.airhockey.utils.LoggerConfig;
import ar.airhockey.utils.MatrixHelper;
import ar.airhockey.utils.ShaderHelper;
import ar.airhockey.utils.TextResourceReader;
import ar.airhockey.utils.TextureHelper;


public class AirHockeyRenderer implements Renderer {
    private final Context context;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private Table table;
    private Mallet mallet;

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;

    private int[] texture=new int[2];

    public AirHockeyRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);


        table = new Table();
        mallet = new Mallet();

        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);

        texture[0] = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface);
        texture[1] = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface2);

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 10f);

        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, 0f, -3f);
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);

        final float[] temp = new float[16];
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        // No culling of back faces
        glDisable(GL_CULL_FACE);
        // No depth testing
        glDisable(GL_DEPTH_TEST);

        // Draw the table.
        textureProgram.useProgram();
        //Dodajanje 2. texture za exercise7
        textureProgram.setUniforms2(projectionMatrix, texture[0],texture[1]);

        table.bindData(textureProgram);
        textureProgram.setuTextureUnit(0);
        //Dodajanje 2. texture za tutorial
        textureProgram.setuTextureUnit2(1);
        table.draw();

        // Draw the mallets.
        colorProgram.useProgram();
        colorProgram.setUniforms(projectionMatrix);
        mallet.bindData(colorProgram);
        mallet.draw();
    }

}
