package ar.airhockey;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

public class AirHockeyActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);

        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            //Req an OpenGL ES 2.0 compatible context
            glSurfaceView.setEGLContextClientVersion(2);
            //assign our renderer
            glSurfaceView.setRenderer(new AirHockeyRenderer(getApplicationContext()));
            rendererSet = true;

        }else{
            Toast.makeText(this, "This device does not support OpenGL ES 2.0",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        setContentView(glSurfaceView);


    }

    protected void onPause(){
        super.onPause();
        if(rendererSet) glSurfaceView.onPause();
    }

    protected void onResume(){
        super.onResume();
        if(rendererSet) glSurfaceView.onResume();
    }


}