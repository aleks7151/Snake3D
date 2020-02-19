package com.example.snake3d;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glViewport;

public class PartRenderer implements GLSurfaceView.Renderer {

    private Context context;

    public PartRenderer(Context context)
    {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.957f, 0.2109f, 0.171875f, 1f);
//        glClearColor(0, 0, 0, 1f);
        int vertexShaderId = ActivityGL.loadShader(GLES20.GL_VERTEX_SHADER, ActivityGL.vertexShaderCode);
        int fragmentShaderId = ActivityGL.loadShader(GLES20.GL_FRAGMENT_SHADER, ActivityGL.fragmentShaderCode);
        ActivityGL.programId = GLES20.glCreateProgram();             // создаем пустую программу OpenGL ES
        GLES20.glAttachShader(ActivityGL.programId, vertexShaderId);   // добавляем в нее шейдер вершин
        GLES20.glAttachShader(ActivityGL.programId, fragmentShaderId); // добавляем в нее шейдер фрагментов
        GLES20.glLinkProgram(ActivityGL.programId);
        glUseProgram(ActivityGL.programId);
        ActivityGL.createViewMatrix();
        ActivityGL.createProjectionMatrix(ActivityGL.width_context, ActivityGL.height_context);
        ActivityGL.prepareData();
        ActivityGL.bindData();
        Matrix.setIdentityM(ActivityGL.mModelMatrix, 0);
        Matrix.setIdentityM(ActivityGL.mTempMatrix, 0);
        Matrix.setIdentityM(ActivityGL.mRotateMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        ActivityGL.bindMatrix();
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        if (System.currentTimeMillis() - ActivityGL.time > 13 && ActivityGL.start){
            ActivityGL.time = System.currentTimeMillis();
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glLineWidth(3.5f);

        Matrix.setIdentityM(ActivityGL.mModelMatrix, 0);

        ActivityGL.snake_main();
    }

}
