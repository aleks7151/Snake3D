package com.example.snake3d;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glIsBuffer;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glEnable;

public class ActivityGL extends GLSurfaceView {

    public final static int POSITION_COUNT = 3;
    public final static int COLOR_COUNT = 3;

    public static Context context;

    public static int programId;

    public static FloatBuffer vertexData;
    public static FloatBuffer vertexData_color;
    public static FloatBuffer vertexData_normal;

    public static int uColorLocation;
    public static int aPositionLocation;
    public static int aNormalLocation;
    public static int uMatrixLocation;
    public static int model;
    public static int new_Color;

    public static float[] mProjectionMatrix = new float[16];
    public static float[] mViewMatrix = new float[16];
    public static float[] mModelMatrix = new float[16];
    public static float[] mScaleMatrix = new float[16];
    public static float[] mRotateMatrix = new float[16];
    public static float[] mTempMatrix = new float[16];
    public static float[] mMatrix = new float[16];

    public static float left = -1.0f;
    public static float right = 1.0f;
    public static float bottom = -1.0f;
    public static float top = 1.0f;
    public static float near;
    public static float far;

    public static int width_context;
    public static int height_context;

    public static int col = 5;//количество яйчеек в змейке изначально

    public static float a;

    public static float[] snake_m = new float[435 * 3];

    public static long time = System.currentTimeMillis();
    public static boolean start = false;

    public ActivityGL(Context context, int width, int height)
    {
        this(context, null);
        width_context = width;
        height_context = height;
    }

    public ActivityGL(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ActivityGL(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(new PartRenderer(context));
    }

    public static final String vertexShaderCode =
            "attribute vec4 a_Position;" +
                    "uniform mat4 u_Matrix;" +
                    "uniform mat4 m_model;" +
                    "attribute vec4 a_Color;" +
                    "attribute vec3 a_Normal;" +
                    "varying vec4 v_Color;" +
                    "varying vec3 v_Position;" +
                    "varying vec3 v_Normal;" +
                    "void main() {" +
                    "    v_Color = a_Color;" +
                    "    vec3 n_Normal = normalize(a_Normal);" +
                    "    v_Normal = mat3(m_model) * n_Normal;" +
                    "    v_Position = vec3(m_model * a_Position);" +
                    "    gl_Position = u_Matrix * a_Position;" +
                    "    gl_PointSize = 20.0;" +
                    "}";

    public static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 new_Color;" +
                    "uniform vec3 u_LightPos;" +
                    "uniform vec3 u_Camera;" +
                    "varying vec4 v_Color;" +
                    "varying vec3 v_Position;" +
                    "varying vec3 v_Normal;" +
                    "void main() {" +
                    "    vec3 n_Normal = normalize(v_Normal);" +
//                    "    vec3 lightvector = normalize(u_LightPos - v_Position);" +
                    "    vec3 lightvector = normalize(u_LightPos);" +
                    "    vec3 lookvector = normalize(u_Camera - v_Position);" +
                    "    float ambient = 0.25;" +
                    "    float k_diffuse = 1.15;" +
                    "    float k_specular= 0.2;" +
                    "    float distance = length(u_LightPos - v_Position);" +
                    "    float diffuse = k_diffuse * max(dot(n_Normal, lightvector), 0.0);" +
//                    "    diffuse = diffuse * (1.0 / ((0.07 * distance * distance)));" +
                    "    vec3 reflectvector = reflect(-lightvector, n_Normal);" +
                    "    float specular = k_specular * pow( max(dot(lookvector,reflectvector),0.0), 40.0 );" +
                    "    if (new_Color == vec4(0, 0, 0, 0))" +
                    "       gl_FragColor = (specular + diffuse + ambient) * v_Color;" +
                    "    else" +
                    "       gl_FragColor = (specular + diffuse + ambient) * new_Color;" +
                    "}";

    public static float k_right;
    public static float k_left;
    public static float k_top;
    public static float k_bottom;
    public static float z1;

    public static float[] init_snake(float z_snake) {
        float[] snake = {
                //1
                0, 0, 0,
                0, -a, 0,
                a, -a, 0,
                //1

                //2
                0, 0, 0,
                a, -a, 0,
                a, 0, 0,
                //2

                //3
                0, -a, 0,
                0, -a, z1 - z_snake,
                a, -a, 0,
                //3

                //4
                a, -a, z1 - z_snake,
                a, -a, 0,
                0, -a, z1 - z_snake,
                //4

                //5
                a, -a, z1 - z_snake,
                a, 0, z1 - z_snake,
                a, -a, 0,
                //5

                //6
                a, -a, 0,
                a, 0, z1 - z_snake,
                a, 0, 0,
                //6

                //7
                0, 0, z1 - z_snake,
                0, 0, 0,
                a, 0, z1 - z_snake,
                //7

                //8
                0, 0, 0,
                a, 0, 0,
                a, 0, z1 - z_snake,
                //8

                //9
                0, 0, z1 - z_snake,
                0, -a, z1 - z_snake,
                0, 0, 0,
                //9

                //10
                0, -a, 0,
                0, 0, 0,
                0, -a, z1 - z_snake,
                //10

                //11
                0, 0, z1 - z_snake,
                a, -a, z1 - z_snake,
                0, -a, z1 - z_snake,
                //11

                //12
                0, 0, z1 - z_snake,
                a, 0, z1 - z_snake,
                a, -a, z1 - z_snake,
                //12
        };
        return (snake);
    }

    public static float tmp_z;//УБРАТЬ

    public static void prepareData() {
        z1 = -2;

        float mnoz0 = 2.5f;
        float mnoz1 = 2.5f;

        k_right = right * mnoz0;
        k_left = left * mnoz0;
        k_top = top * mnoz1;
        k_bottom = bottom * mnoz1;

        a = (k_right - k_left) / 8;

        k_bottom = k_top - (int) ((k_top - k_bottom) / a) * a;

        float z_snake = z1 + a;
        tmp_z = z_snake;

        float[] vertices = init_snake(z_snake);


        for (int i = 0; i < 435; i++) {
            snake_m[i * 3] = 0;
            snake_m[i * 3 + 1] = -a * i;
            snake_m[i * 3 + 2] = 0;
        }

        float[] vertices_normal = new float[vertices.length];

        for (int i = 0; i < 12; i++) {
            for (int i1 = 0; i1 < 3; i1++) {
                int i2;
                int i3;
                if (i1 == 0) {
                    i2 = 1;
                    i3 = 2;
                } else if (i1 == 1) {
                    i2 = 2;
                    i3 = 0;
                } else {
                    i2 = 0;
                    i3 = 1;
                }
                float[] a_vector = {
                        vertices[i * 9 + i2 * 3] - vertices[i * 9 + i1 * 3],
                        vertices[i * 9 + i2 * 3 + 1] - vertices[i * 9 + i1 * 3 + 1],
                        vertices[i * 9 + i2 * 3 + 2] - vertices[i * 9 + i1 * 3 + 2],
                };
                float[] b_vector = {
                        vertices[i * 9 + i3 * 3] - vertices[i * 9 + i1 * 3],
                        vertices[i * 9 + i3 * 3 + 1] - vertices[i * 9 + i1 * 3 + 1],
                        vertices[i * 9 + i3 * 3 + 2] - vertices[i * 9 + i1 * 3 + 2],
                };
                vertices_normal[i * 9 + i1 * 3] = a_vector[1] * b_vector[2] - b_vector[1] * a_vector[2];
                vertices_normal[i * 9 + i1 * 3 + 1] = b_vector[0] * a_vector[2] - a_vector[0] * b_vector[2];
                vertices_normal[i * 9 + i1 * 3 + 2] = a_vector[0] * b_vector[1] - b_vector[0] * a_vector[1];
            }
        }
        float[] vertices_color = new float[vertices.length];

        for (int i = 0; i < vertices.length; i += 3) {
            vertices_color[i] = 1;
            vertices_color[i + 1] = 1;
            vertices_color[i + 2] = 1;
        }


        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);

        vertexData_normal = ByteBuffer
                .allocateDirect(vertices_normal.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData_normal.put(vertices_normal);

        vertexData_color = ByteBuffer
                .allocateDirect(vertices_color.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData_color.put(vertices_color);

        for (int i = 0; i < col; i++)
            checkAlpha[i] = false;
    }

    public static int loadShader(int type, String shaderCode) {

        // Создаем шейдер вершин (GLES20.GL_VERTEX_SHADER)
        // или шейдер фрагментов (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // Добавляем исходный код и компилируем
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static void bindData() {
        // координаты
        aPositionLocation = glGetAttribLocation(programId, "a_Position");
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                false, 12, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        //нормали
        aNormalLocation = glGetAttribLocation(programId, "a_Normal");
        vertexData_normal.position(0);
        glVertexAttribPointer(aNormalLocation, POSITION_COUNT, GL_FLOAT,
                false, 12, vertexData_normal);
        glEnableVertexAttribArray(aNormalLocation);

        // цвет
        //uColorLocation = glGetUniformLocation(programId, "v_Color");
        uColorLocation = glGetAttribLocation(programId, "a_Color");
        vertexData_color.position(0);
        glVertexAttribPointer(uColorLocation, COLOR_COUNT, GL_FLOAT, false, 12, vertexData_color);
        glEnableVertexAttribArray(uColorLocation);

        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix");

        new_Color = glGetUniformLocation(programId, "new_Color");
        glUniform4f(new_Color, 0, 0, 0, 0);

        int uLightLocation = glGetUniformLocation(programId, "u_LightPos");
        glUniform3f(uLightLocation, -2, 1.15f, 1.6f);//0, 0, 0.8

        model = glGetUniformLocation(programId, "m_model");

        int uCameraLocation = glGetUniformLocation(programId, "u_Camera");
        glUniform3f(uCameraLocation, camera[0], camera[1], camera[2]);
    }

    public static float[] camera = new float[3];

    public static void createViewMatrix() {
        // точка положения камеры
        float eyeX = -2.5f;//0
        float eyeY = 0.5f;//-0.321f;//-0.321f
        float eyeZ = 1.4f;//3.45f
        camera[0] = eyeX;
        camera[1] = eyeY;
        camera[2] = eyeZ;

        // точка направления камеры
        float centerX = 0;
        float centerY = -0.9f;//0.041f;//0.041f
        float centerZ = 0;

        // up-вектор
        float upX = 0;
        float upY = 0;
        float upZ = 1;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    public static void createProjectionMatrix(int width, int height) {
        float ratio;
        left = -1.0f;
        right = 1.0f;
        bottom = -1.0f;
        top = 1.0f;
        near = 2.0f;
        far = 10.0f;
        if (width > height) {
            ratio = (float) width / height;
            left *= ratio;
            right *= ratio;
        } else {
            ratio = (float) height / width;
            bottom *= ratio;
            top *= ratio;
        }
    }

    public static void bindMatrix() {
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotateMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mScaleMatrix, 0);
        glUniformMatrix4fv(model, 1, false, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);
    }

    public static float[] alpha = new float[col];
    public static float[] alphaTrans = new float[col];
    public static boolean[] helpAlpha = new boolean[col];
    public static boolean[] checkAlpha = new boolean[col];
    public static double random = -1;
    public static void snake_main() {
        for (int i = 0; i < col; i++) {
            if (random < 0.2f && (checkAlpha[i] || alpha[i] > 0)){
                glUniform4f(new_Color, 0.85f, 0.85f, 0.85f, 1);

                if (checkAlpha[i])
                    alpha[i] += 9;
                else
                    alpha[i] -= 9;
                Matrix.rotateM(mRotateMatrix, 0, alpha[i], 0, 1,0);
                Matrix.translateM(mRotateMatrix, 0, -a / 2, 0, a / 2);
                Matrix.translateM(mModelMatrix, 0, a / 2, 0, -a / 2);
                if (alpha[i] == 63)
                {
                    checkAlpha[i] = false;
                    if (i < col - 1)
                        checkAlpha[i + 1] = true;
                }
            }
            else if (random >= 0.2f && random < 0.4 && (checkAlpha[i] || alpha[i] > 0)){
                glUniform4f(new_Color, 0.85f, 0.85f, 0.85f, 1);

                if (checkAlpha[i])
                    alpha[i] += 9;
                else
                    alpha[i] -= 9;
                Matrix.rotateM(mRotateMatrix, 0, alpha[i], 0, 1,0);
                Matrix.translateM(mRotateMatrix, 0, -a / 2, 0, a / 2);
                Matrix.translateM(mModelMatrix, 0, a / 2, 0, -a / 2);
                if (alpha[i] >= 60 && i < col - 1)
                    checkAlpha[i + 1] = true;
                if (alpha[i] == 90){
                    alpha[i] = 0;
                    checkAlpha[i] = false;
                }
            }
            else if (random >= 0.4f && random < 0.6f && (checkAlpha[i] || alpha[i] < 0)){
                glUniform4f(new_Color, 0.85f, 0.85f, 0.85f, 1);

                if (helpAlpha[i])
                    alpha[i] += a / 10;
                else if (checkAlpha[i])
                    alpha[i] -= a / 10;
                else
                    alpha[i] += a / 10;
                Matrix.translateM(mModelMatrix, 0, alpha[i], 0, 0);
                if (alpha[i] >= a / 2 - 0.0001f){
                    if (i < col - 1)
                        checkAlpha[i + 1] = true;
                    helpAlpha[i] = false;
                }
                else if (alpha[i] <= -a / 2 + 0.0001f)
                    checkAlpha[i] = false;
            }
            else if (random > 0.6f && random < 0.8f && (checkAlpha[i] || alpha[i] > 0)){
                glUniform4f(new_Color, 0.85f, 0.85f, 0.85f, 1);

                if (checkAlpha[i])
                    alpha[i] += 9;
                else
                    alphaTrans[i] -= a / 10;
                Matrix.rotateM(mRotateMatrix, 0, alpha[i], 0, 1,0);
                Matrix.translateM(mRotateMatrix, 0, -a, 0, a);
                Matrix.translateM(mModelMatrix, 0, a, 0, -a);
                Matrix.translateM(mModelMatrix, 0, alphaTrans[i], 0, 0);
                if (alpha[i] == 63 && i < col - 1)
                        checkAlpha[i + 1] = true;
                else if (alpha[i] == 90 && checkAlpha[i])
                    checkAlpha[i] = false;
                else if (alphaTrans[i] - 0.0001f <= -a)
                    alpha[i] = 0;
            }
            else if (random >= 0.8f && (checkAlpha[i] || alpha[i] > 1)){
                glUniform4f(new_Color, 0.85f, 0.85f, 0.85f, 1);

                if (checkAlpha[i])
                    alpha[i] += 0.2f;
                else
                    alpha[i] -= 0.2f;
                Matrix.scaleM(mScaleMatrix, 0, alpha[i], 1, alpha[i]);
                Matrix.translateM(mModelMatrix, 0, -(alpha[i] - 1) * a / 2, 0, (alpha[i] - 1) * a / 2);
                if (alpha[i] + 0.0001f >= 1.4f && checkAlpha[i])
                {
                    checkAlpha[i] = false;
                    if (i < col - 1)
                        checkAlpha[i + 1] = true;
                }
            }
            Matrix.translateM(mModelMatrix, 0, snake_m[i * 3], snake_m[i * 3 + 1], snake_m[i * 3 + 2]);
            bindMatrix();
            glDrawArrays(GL_TRIANGLES, 0, 36);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
            Matrix.setIdentityM(mRotateMatrix, 0);
            bindMatrix();
            glUniform4f(new_Color, 0, 0, 0, 0);
        }
    }

    public boolean checkAnimation()
    {
        for (int i = 0; i < col; i++)
            if (checkAlpha[i])
                return false;
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()== MotionEvent.ACTION_DOWN && checkAnimation()){
            checkAlpha[0] = true;
            random = Math.random();
            for (int i = 0; i < col; i++){
                alpha[i] = 0;
                if (random >= 0.8f)
                    alphaTrans[i] = 1;
                else
                    alphaTrans[i] = 0;
                helpAlpha[i] = true;
            }
        }
        return super.onTouchEvent(event);
    }
}
