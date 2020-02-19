package com.example.snake3d;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import static android.app.PendingIntent.getActivity;
import static android.content.Context.MODE_PRIVATE;
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
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix3fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glEnable;

public class ChooseControlGL extends GLSurfaceView implements Renderer {

    private final static int POSITION_COUNT = 3;
    private final static int COLOR_COUNT = 3;

    private Context context;

    private int programId;

    private FloatBuffer vertexData;
    private FloatBuffer vertexData_color;
    private FloatBuffer vertexData_normal;

    private int uColorLocation;
    private int aPositionLocation;
    private int aNormalLocation;
    private int uMatrixLocation;
    private int model;
    private int new_Color;

    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];
    private float[] mRotateMatrix = new float[16];
    private float[] mTempMatrix = new float[16];
    private float[] mMatrix = new float[16];

    public float left = -1.0f;
    public float right = 1.0f;
    public float bottom = -1.0f;
    public float top = 1.0f;
    private float near;
    private float far;

    public static int width_context;
    public static int height_context;

    public static int col = 7;//количество яйчеек в змейке изначально

    public static float a;

    public static float[] snake_m = new float[435 * 3];

    public ChooseControlGL(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(this);
        this.context = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width_context = size.x;
        height_context = size.y;
        for (int i = 0; i < 3; i++)
            for(int j = 0; j < 7; j++)
                depth[i][j] = 0.6f;
    }

    private final String vertexShaderCode =
            "uniform int l_top;" +
                    "uniform int l_down;" +
                    "uniform int l_rotate;" +
                    "uniform float a;" +
                    "uniform float scale;" +
                    "uniform int chooseC;" +

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

                    "    vec4 a_pos = m_model * a_Position;" +
                    "    vec4 tmp = a_pos;" +
                    "    if (chooseC == 0 && l_top == 1){" +
                    "       if (a_Position.xyz == vec3(-a + a / 2.0 + a / 4.0, 0.0, 0.0)){" +
                    "           if (l_rotate == 0)" +
                    "               tmp.x = tmp.x - (a / 2.0) * (scale - 1.0);" +
                    "           else" +
                    "               tmp.x = tmp.x + (a / 2.0) * (scale - 1.0);" +
                    "       }" +
                    "       else if (a_Position.xyz == vec3(-a - a / 2.0 + a / 4.0, -a - a / 2.0 - a / 4.0 - a / 2.0, 0.0)){" +
                    "           if (l_rotate == 0)" +
                    "               tmp.y = tmp.y + 7.5 / 8.0 * a * (scale - 1.0);" +
                    "           else" +
                    "               tmp.y = tmp.y - 7.5 / 8.0 * a * (scale - 1.0);" +
                    "       }" +
                    "    }" +
                    "    else if (chooseC == 0 && l_down == 1){" +
                    "       if (a_Position.xyz == vec3(-a + a / 2.0 + a / 4.0, - 2.0 * a - a, 0.0)){" +
                    "           if (l_rotate == 0)" +
                    "               tmp.x = tmp.x - (a / 2.0) * (scale - 1.0);" +
                    "           else" +
                    "               tmp.x = tmp.x + (a / 2.0) * (scale - 1.0);" +
                    "       }" +
                    "       else if (a_Position.xyz == vec3(-a - a / 2.0 + a / 4.0, -a - a / 4.0 + a / 2.0, 0.0)){" +
                    "           if (l_rotate == 0)" +
                    "               tmp.y = tmp.y - 7.5 / 8.0 * a * (scale - 1.0);" +
                    "           else" +
                    "               tmp.y = tmp.y + 7.5 / 8.0 * a * (scale - 1.0);" +
                    "       }" +
                    "    }" +
                    "    else if (chooseC == 1 && (l_top == 1 || l_down == 1)){" +//право верх
                    "       if (a_Position.xyz == vec3(a + a / 2.0, 3.0 * a, 0.0)){" +
                    "           float pif = sqrt(step(a_Position.x - (-a - a / 2.0), 2.0) + step(a_Position.y, 2.0));" +
                    "           if (l_rotate == 0)" +
                    "               tmp.y = tmp.y + pif * scale / 1.5;" +
                    "           else if (l_rotate == 1)" +
                    "               tmp.x = tmp.x + pif * scale / 1.5;" +
                    "           else if (l_rotate == 2)" +
                    "               tmp.y = tmp.y - pif * scale / 1.5;" +
                    "           else if (l_rotate == 3)" +
                    "               tmp.x = tmp.x - pif * scale / 1.5;" +
                    "       }" +
                    "       else if (a_Position.xyz == vec3(a, 2.0 * a + a / 2.0, 0.0)){" +//a + a / 2, 3 * a, 0
                    "           float pif = sqrt(step(a_Position.x - (-a - a / 2.0), 2.0) + step(a_Position.y - (a / 2.0), 2.0));" +
                    "           if (l_rotate == 0){" +
                    "               tmp.x = (m_model * vec4(a + a / 2.0, 3.0 * a, 0.0, 1.0)).x;" +
                    "               tmp.y = tmp.y + pif * scale / 1.5;" +
                    "           }" +
                    "           else if (l_rotate == 1){" +
                    "               tmp.y = (m_model * vec4(a + a / 2.0, 3.0 * a, 0.0, 1.0)).y;" +
                    "               tmp.x = tmp.x + pif * scale / 1.5;" +
                    "           }" +
                    "           else if (l_rotate == 2){" +
                    "               tmp.x = (m_model * vec4(a + a / 2.0, 3.0 * a, 0.0, 1.0)).x;" +
                    "               tmp.y = tmp.y - pif * scale / 1.5;" +
                    "           }" +
                    "           else if (l_rotate == 3){" +
                    "               tmp.y = (m_model * vec4(a + a / 2.0, 3.0 * a, 0.0, 1.0)).y;" +
                    "               tmp.x = tmp.x - pif * scale / 1.5;" +
                    "           }" +
                    "       }" +
                    "    }" +

                    "    v_Position = vec3(tmp);" +
                    "    gl_Position = u_Matrix * tmp;" +
                    "    gl_PointSize = 20.0;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 new_Color;" +
                    "uniform vec3 u_LightPos;" +
                    "uniform vec3 u_Camera;" +
                    "varying vec4 v_Color;" +
                    "varying vec3 v_Position;" +
                    "varying vec3 v_Normal;" +
                    "void main() {" +
                    "    vec3 n_Normal = normalize(v_Normal);" +
                    "    vec3 lightvector = normalize(u_LightPos - v_Position);" +
                    "    vec3 lookvector = normalize(u_Camera - v_Position);" +
                    "    float ambient = 0.2;" +
                    "    float k_diffuse = 0.8;" +
                    "    float k_specular= 0.4;" +
                    "    float distance = length(u_LightPos - v_Position);" +
                    "    float diffuse = k_diffuse * max(dot(n_Normal, lightvector), 0.0);" +
                    "    diffuse = diffuse * (1.0 / ((0.07 * distance * distance)));" +
                    "    vec3 reflectvector = reflect(-lightvector, n_Normal);" +
                    "    float specular = k_specular * pow( max(dot(lookvector,reflectvector),0.0), 40.0 );" +
                    "    if (new_Color == vec4(0, 0, 0, 0))" +
                    "       gl_FragColor = (specular + diffuse + ambient) * v_Color;" +
                    "    else" +
                    "       gl_FragColor = (specular + diffuse + ambient) * new_Color;" +
                    "}";

    private static int loadShader(int type, String shaderCode) {

        // Создаем шейдер вершин (GLES20.GL_VERTEX_SHADER)
        // или шейдер фрагментов (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // Добавляем исходный код и компилируем
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0f, 0f, 0f, 1f);
        int vertexShaderId = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderId = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        programId = GLES20.glCreateProgram();             // создаем пустую программу OpenGL ES
        GLES20.glAttachShader(programId, vertexShaderId);   // добавляем в нее шейдер вершин
        GLES20.glAttachShader(programId, fragmentShaderId); // добавляем в нее шейдер фрагментов
        GLES20.glLinkProgram(programId);
        glUseProgram(programId);
        createViewMatrix();
        createProjectionMatrix(width_context, height_context);
        prepareData();
        bindData();
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mTempMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        tmpX = k_right - 0.35f * a;
        scale = 1;
        border = checkTap();
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        bindMatrix();
    }

    public static float k_right;
    public static float k_left;
    public static float k_top;
    public static float k_bottom;

    private float z3;
    private float z1;
    private float z2;

    private int init_setka(float[] setka, float z1_setka) {
        int ch_setka = 0;
//        for (int i = 0; i < 16; i++){
        setka[ch_setka] = k_left;// + a * i;
        setka[ch_setka + 1] = k_top;
        setka[ch_setka + 2] = z1_setka;
        ch_setka += 3;
        setka[ch_setka] = k_left;// + a * i;
        setka[ch_setka + 1] = k_bottom;
        setka[ch_setka + 2] = z1_setka;
        ch_setka += 3;
//        }
//        for(int i = 0; i < 28; i++){
        setka[ch_setka] = k_left;
        setka[ch_setka + 1] = k_top;// - a * i;
        setka[ch_setka + 2] = z1_setka;
        ch_setka += 3;
        setka[ch_setka] = k_right;
        setka[ch_setka + 1] = k_top;// - a * i;
        setka[ch_setka + 2] = z1_setka;
        ch_setka += 3;
//        }

        return (ch_setka);
    }

    private float[] init_snake(float z_snake) {
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

    float[] controlFirst;
    private float[] init_controlFirst()
    {
        float[] control = {
                //правая
                0, 0, 0,
                -a / 2, -3 * a, 0,
                0, -3 * a, 0,

                0, 0, 0,
                -a / 2, 0, 0,
                -a / 2, -3 * a, 0,

                //верх
                -a / 2 + a / 4, 0, 0,
                -a + a / 4, -a / 2, 0,
                -a / 2 + a / 4, -a / 2, 0,

                -a / 2 + a / 4, 0, 0,
                -a + a / 4, 0, 0,
                -a + a / 4, -a / 2, 0,

                //низ
                -a / 2 + a / 4, - 2 * a - a / 2, 0,
                -a + a / 4, - 2 * a - a, 0,
                -a / 2 + a / 4, - 2 * a - a, 0,

                -a / 2 + a / 4, - 2 * a - a / 2, 0,
                -a + a / 4, - 2 * a - a / 2, 0,
                -a + a / 4, - 2 * a - a, 0,

                //верх лево
                -a + a / 4, 0, 0,
                -a - a / 2 + a / 4, -a - a - a / 4, 0,      //используется в шейдере a; 2a + a/4
                -a + a / 2 + a / 4, 0, 0,                   //используется в шейдере

                -a + a / 4, 0, 0,
                -a - a / 2 + a / 4, -a - a / 4, 0,
                -a - a / 2 + a / 4, -a - a - a / 4, 0,      //используется в шейдере

                //низ лево
                -a + a / 2 + a / 4, - 2 * a - a, 0,         //используется в шейдере
                -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,
                -a + a / 4, - 3 * a, 0,

                -a + a / 2 + a / 4, - 2 * a - a, 0,         //используется в шейдере
                -a - a / 2 + a / 4, -a + a / 2 - a / 4, 0,  //используется в шейдере
                -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,

                //лево середина
                -a + a / 4, -a - a / 4 + 0.1f * a, 0,
                -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,
                -a + a / 4, -a - a / 2 - a / 4 - 0.1f * a, 0,

                -a + a / 4, -a - a / 4 + 0.1f * a, 0,
                -a - a / 2 + a / 4, -a - a / 4, 0,
                -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,

                /////Отрисовка этой херни сверху

                //правая
                0, 0, 0,
                -a / 8, -3 * a, 0,
                0, -3 * a, 0,

                0, 0, 0,
                -a / 8, 0, 0,
                -a / 8, -3 * a, 0,

                //верх
                a / 8 -a / 2 + a / 4, 0, 0,
                a / 8 -a + a / 4, -a / 8, 0,
                a / 8 -a / 2 + a / 4, -a / 8, 0,

                a / 8 -a / 2 + a / 4, 0, 0,
                a / 8 -a + a / 4, 0, 0,
                a / 8 -a + a / 4, -a / 8, 0,

                //низ
                a / 8 -a / 2 + a / 4, - 2 * a - a * 7 / 8, 0,
                a / 8 -a + a / 4, - 2 * a - a, 0,
                a / 8 -a / 2 + a / 4, - 2 * a - a, 0,

                a / 8 -a / 2 + a / 4, - 2 * a - a * 7 / 8, 0,
                a / 8 -a + a / 4, - 2 * a - a * 7 / 8, 0,
                a / 8 -a + a / 4, - 2 * a - a, 0,

                //верх лево
                a / 8 -a + a / 4, 0, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 4 - a / 4, 0,
                a / 8 -a + a / 8 + a / 4, 0, 0,

                a / 8 -a + a / 4, 0, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 4, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 4 - a / 4, 0,

                //низ лево
                a / 8 -a + a / 8 + a / 4, - 2 * a - a, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,
                a / 8 -a + a / 4, - 3 * a, 0,

                a / 8 -a + a / 8 + a / 4, - 2 * a - a, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 4 - a / 4, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,

                //лево середина
                a / 8 -a - a * 3 / 8 + a / 4, -a - a / 4 + 0.1f * a, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,
                a / 8 -a - a * 3 / 8 + a / 4, -a - a / 2 - a / 4 - 0.1f * a, 0,

                a / 8 -a - a * 3 / 8 + a / 4, -a - a / 4 + 0.1f * a, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 4, 0,
                a / 8 -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,
        };
        controlFirst = new float[6 * 18];
        for (int i = 0; i < controlFirst.length; i++)
            controlFirst[i] = control[i];
        return control;
    }

    float[] controlSecond;
    private float[] init_controlSecond()
    {
        float[] control = {
                //нижняя
                a + a / 2, a / 2, 0,
                -a - a / 2, 0, 0,
                a + a / 2, 0, 0,

                a + a / 2, a / 2, 0,
                -a - a / 2, a / 2, 0,
                -a - a / 2, 0, 0,

                //левая
                -a, 3 * a, 0,
                -a - a / 2, 0, 0,
                -a, 0, 0,

                -a, 3 * a, 0,
                -a - a / 2, 3 * a, 0,
                -a - a / 2, 0, 0,

                //правая (право верх)
                a + a / 2, 3 * a, 0,            //используется в шейдере
                a, 0, 0,
                a + a / 2, 0, 0,

                a + a / 2, 3 * a, 0,            //используется в шейдере
                a, 2 * a + a / 2, 0,            //используется в шейдере
                a, 0, 0,

                //верхняя (лево верх)
                a + a / 2, 3 * a, 0,            //используется в шейдере
                -a - a / 2, 2 * a + a / 2, 0,
                a, 2 * a + a / 2, 0,            //используется в шейдере

                a + a / 2, 3 * a, 0,            //используется в шейдере
                -a - a / 2, 3 * a, 0,
                -a - a / 2, 2 * a + a / 2, 0,


                ///////Отрисовка этой херни сверху 2.0
                a + a / 2, 3 * a, 0,
                -a - a / 2, 0, 0,
                a + a / 2, 0, 0,

                a + a / 2, 3 * a, 0,
                -a - a / 2, 3 * a, 0,
                -a - a / 2, 0, 0,
        };
        controlSecond = new float[4 * 18];
        for (int i = 0; i < controlSecond.length; i++)
            controlSecond[i] = control[i];
        return control;
    }

    private float[] init_vertices_0() {
        float[] vertices_0 = {
                //пол
                k_left, k_top, z1,
                k_left, k_bottom, z1,
                k_right, k_top, z1,

                k_left, k_bottom, z1,
                k_right, k_bottom, z1,
                k_right, k_top, z1,
                //пол

                //дальняя
                k_left, k_top, z2,
                k_right, k_top, z2,
                k_left, k_top, z3,

                k_right, k_top, z2,
                k_right, k_top, z3,
                k_left, k_top, z3,
                //дальняя

                //левая
                k_left, k_top, z3,
                k_left, k_bottom, z3,
                k_left, k_top, z2,

                k_left, k_bottom, z2,
                k_left, k_top, z2,
                k_left, k_bottom, z3,
                //левая

                //ближняя
                k_left, k_bottom, z2,
                k_left, k_bottom, z3,
                k_right, k_bottom, z2,

                k_right, k_bottom, z3,
                k_right, k_bottom, z2,
                k_left, k_bottom, z3,
                //ближняя

                //правая
                k_right, k_bottom, z2,
                k_right, k_bottom, z3,
                k_right, k_top, z2,

                k_right, k_top, z3,
                k_right, k_top, z2,
                k_right, k_bottom, z3,
                //правая
        };
        return (vertices_0);
    }

    private int init_vertices(int a_s, float[] vertices) {
        vertices[a_s] = 0;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = 0;
        a_s += 3;
        vertices[a_s] = 0;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = -(z2 - z1);
        a_s += 3;
        vertices[a_s] = a;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = -(z2 - z1);
        a_s += 3;
        vertices[a_s] = a;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = -(z2 - z1);
        a_s += 3;
        vertices[a_s] = a;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = 0;
        a_s += 3;
        vertices[a_s] = 0;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = 0;
        a_s += 3;

        vertices[a_s] = 0;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = 0;
        a_s += 3;
        vertices[a_s] = 0;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = -(z2 - z1);
        a_s += 3;
        vertices[a_s] = 0;
        vertices[a_s + 1] = a;
        vertices[a_s + 2] = -(z2 - z1);
        a_s += 3;
        vertices[a_s] = 0;
        vertices[a_s + 1] = a;
        vertices[a_s + 2] = -(z2 - z1);
        a_s += 3;
        vertices[a_s] = 0;
        vertices[a_s + 1] = a;
        vertices[a_s + 2] = 0;
        a_s += 3;
        vertices[a_s] = 0;
        vertices[a_s + 1] = 0;
        vertices[a_s + 2] = 0;
        return (a_s);
    }

    float tmp_z;//УБРАТЬ

    private void prepareData() {
        z1 = -2;
        float z1_setka = z1 + 0.01f;

        z3 = 0.5f;

        float mnoz0 = 2.5f;
        float mnoz1 = 2.5f;

        k_right = right * mnoz0;
        k_left = left * mnoz0;
        k_top = top * mnoz1;
        k_bottom = bottom * mnoz1;

        a = (k_right - k_left) / 15;

        k_bottom = k_top - (int) ((k_top - k_bottom) / a) * a;

        float z_snake = z1 + a;
        tmp_z = z_snake;//УБРАТЬ

        z2 = z_snake + 2 * (z_snake - z1);

        float[] setka = new float[264];
        int ch_setka = init_setka(setka, z1_setka);//0;

        float[] snake = init_snake(z_snake);

        float[] controlFirst = init_controlFirst();
        float[] controlSecond = init_controlSecond();
        float[] control = new float[controlFirst.length + controlSecond.length];
        for (int i = 0; i < controlFirst.length; i++)
            control[i] = controlFirst[i];
        for (int i = 0; i < controlSecond.length; i++)
            control[i + controlFirst.length] = controlSecond[i];

        int snake_ch = snake.length;

        for (int i = 0; i < 435; i++) {
            snake_m[i * 3] = 0;
            snake_m[i * 3 + 1] = -a * i;
            snake_m[i * 3 + 2] = 0;
        }

        float[] vertices_0 = init_vertices_0();

        float[] vertices = new float[vertices_0.length + ch_setka + snake_ch + 2 * 2 * 3 * 3 + control.length];
        for (int i = 0; i < ch_setka; i++) {
            vertices[i] = setka[i];
        }
        int a_s = 0;
        for (int i = ch_setka; i < vertices_0.length + ch_setka; i++) {
            vertices[i] = vertices_0[a_s];
            a_s++;
        }
        a_s = 0;
        for (int i = vertices_0.length + ch_setka; i < vertices_0.length + snake_ch + ch_setka; i++) {
            vertices[i] = snake[a_s];
            a_s++;
        }

        a_s = 0;
        for (int i = vertices_0.length + snake_ch + ch_setka; i < vertices_0.length + snake_ch + ch_setka + control.length; i++){
            vertices[i] = control[a_s];
            a_s++;
        }


        a_s = vertices_0.length + snake_ch + ch_setka + control.length;

        init_vertices(a_s, vertices);

        float[] vertices_normal = new float[vertices.length];

        for (int i = 0; i < 10 + 10 + 2 * 2 + 2 + control.length / 9; i++) {
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
                        vertices[ch_setka + i * 9 + i2 * 3] - vertices[ch_setka + i * 9 + i1 * 3],
                        vertices[ch_setka + i * 9 + i2 * 3 + 1] - vertices[ch_setka + i * 9 + i1 * 3 + 1],
                        vertices[ch_setka + i * 9 + i2 * 3 + 2] - vertices[ch_setka + i * 9 + i1 * 3 + 2],
                };
                float[] b_vector = {
                        vertices[ch_setka + i * 9 + i3 * 3] - vertices[ch_setka + i * 9 + i1 * 3],
                        vertices[ch_setka + i * 9 + i3 * 3 + 1] - vertices[ch_setka + i * 9 + i1 * 3 + 1],
                        vertices[ch_setka + i * 9 + i3 * 3 + 2] - vertices[ch_setka + i * 9 + i1 * 3 + 2],
                };
                vertices_normal[ch_setka + i * 9 + i1 * 3] = a_vector[1] * b_vector[2] - b_vector[1] * a_vector[2];
                vertices_normal[ch_setka + i * 9 + i1 * 3 + 1] = b_vector[0] * a_vector[2] - a_vector[0] * b_vector[2];
                vertices_normal[ch_setka + i * 9 + i1 * 3 + 2] = a_vector[0] * b_vector[1] - b_vector[0] * a_vector[1];
            }
        }

        for (int i = 0; i < ch_setka; i += 3) {
            vertices_normal[i] = vertices_normal[ch_setka];
            vertices_normal[i + 1] = vertices_normal[ch_setka + 1];
            vertices_normal[i + 2] = vertices_normal[ch_setka + 2];
        }

        float[] vertices_color_0 = {
                1, 0, 0,
                1, 0, 0,
                0, 1, 0,

                1, 0, 0,
                0, 1, 0,
                0, 1, 0,
        };

        float[] vertices_color = new float[vertices.length];
        for (int i = 0; i < ch_setka; i++) {
            vertices_color[i] = 1;
        }
        a_s = 0;
        for (int i = ch_setka; i < ch_setka + vertices_color_0.length; i++) {
            vertices_color[i] = vertices_color_0[a_s];
            a_s++;
        }
        for (int i = ch_setka + vertices_color_0.length; i < ch_setka + vertices_0.length; i += 3) {
            vertices_color[i] = 0;
            vertices_color[i + 1] = 0;
            vertices_color[i + 2] = 1;
        }

        for (int i = ch_setka + vertices_0.length; i < vertices.length; i += 3) {
            vertices_color[i] = 1;
            vertices_color[i + 1] = 1;
            vertices_color[i + 2] = 0;
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
    }

    private int l_top;
    private int l_down;
    private int l_rotate;
    private int locaionScale;
    private int chooseC;
    private void bindData() {
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
        glUniform3f(uLightLocation, 0, 0, 0.8f);//0, 0, 0.8

        model = glGetUniformLocation(programId, "m_model");

        int uCameraLocation = glGetUniformLocation(programId, "u_Camera");
        glUniform3f(uCameraLocation, camera[0], camera[1], camera[2]);



        l_top = glGetUniformLocation(programId, "l_top");
        glUniform1i(l_top, 0);
        l_down = glGetUniformLocation(programId, "l_down");
        glUniform1i(l_down, 0);
        l_rotate = glGetUniformLocation(programId, "l_rotate");
        glUniform1i(l_rotate, 0);

        chooseC = glGetUniformLocation(programId, "chooseC");
        glUniform1i(chooseC, 0);

        int a_shader = glGetUniformLocation(programId, "a");
        glUniform1f(a_shader, a);

        locaionScale = glGetUniformLocation(programId, "scale");
        glUniform1f(locaionScale, scale);
    }

    private float[] camera = new float[3];

    float eyeX, eyeY, eyeZ;
    private float[] invertView = new float[16];
    private void createViewMatrix() {
        // точка положения камеры
        eyeX = 0;//0
        eyeY = -1.321f;//-0.321f
        eyeZ = 4.3f;//3.45f
        camera[0] = eyeX;
        camera[1] = eyeY;
        camera[2] = eyeZ;

        // точка направления камеры
        float centerX = 0;
        float centerY = -0.6f;//0.041f;//0.041f
        float centerZ = 0;

        // up-вектор
        float upX = 0;
        float upY = 1;
        float upZ = 0;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        Matrix.invertM(invertView, 0, mViewMatrix, 0);
    }

    private void createProjectionMatrix(int width, int height) {
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
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    private void bindMatrix() {
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mModelMatrix, 0, mRotateMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mMatrix, 0, mScaleMatrix, 0);
        glUniformMatrix4fv(model, 1, false, mMatrix, 0);

        Matrix.setIdentityM(mMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);
    }

    private void draw_snake(float x, float y) {
        Matrix.translateM(mModelMatrix, 0, x, y, 0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 34, 36);
        Matrix.translateM(mModelMatrix, 0, -x, -y, 0);
    }

    private void draw_floar(float x, float y) {
        Matrix.translateM(mModelMatrix, 0, x, y, 0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 4, 6);
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    private void draw_setka(float x, float y) {
        for (int i = 0; i < 16; i++) {
            Matrix.translateM(mModelMatrix, 0, a * i + x, y, 0);
            bindMatrix();
            glDrawArrays(GL_LINES, 0, 2);
            Matrix.setIdentityM(mModelMatrix, 0);
        }
        for (int i = 0; i < 30; i++) {
            Matrix.translateM(mModelMatrix, 0, x, -a * i + y, 0);
            bindMatrix();
            glDrawArrays(GL_LINES, 2, 2);
            Matrix.setIdentityM(mModelMatrix, 0);
        }
        bindMatrix();
    }

    private void draw_wall_vert(int i0, float k, int rot, float x) {
        for (int i = i0; i < 15 + i0; i++) {
            Matrix.rotateM(mRotateMatrix, 0, rot, 0, 0, 1);
            Matrix.translateM(mModelMatrix, 0, k_left + a * i + x, k, z2);//+x, чтобы копировать вправо/влево
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 172, 6);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mRotateMatrix, 0);
        }
    }

    private void draw_wall_hor(int i0, float k, int rot, float y) {
        for (int i = i0; i < 29 + i0; i++) {
            Matrix.rotateM(mRotateMatrix, 0, rot, 0, 0, 1);
            Matrix.translateM(mModelMatrix, 0, k, k_bottom + a * i + y, z2);//+y, чтобы копировать вверх/вниз
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 178, 6);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mRotateMatrix, 0);
        }
    }

    private void floar_main() {
        glDrawArrays(GL_TRIANGLES, 4, 30);//ВКЛЮЧИТЬ
//        glDrawArrays(GL_TRIANGLES,4, 6);
        if (y) {
            draw_floar(0, k_top - k_bottom);
            draw_floar(0, k_bottom - k_top);
        }
        if (x) {
            draw_floar(k_right - k_left, 0);
            draw_floar(k_left - k_right, 0);
        }
        if (left_top)
            draw_floar(k_left - k_right, k_top - k_bottom);
        if (right_top)
            draw_floar(k_right - k_left, k_top - k_bottom);
        if (left_bottom)
            draw_floar(k_left - k_right, k_bottom - k_top);
        if (right_bottom)
            draw_floar(k_right - k_left, k_bottom - k_top);
        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();
    }

    private void wall_main() {
        glUniform4f(new_Color, 1, 0, 0, 1);

        draw_wall_vert(0, k_top, 0, 0);
        draw_wall_vert(1, k_bottom, 180, 0);
        if (x) {
            draw_wall_vert(0, k_top, 0, k_left - k_right);
            draw_wall_vert(1, k_bottom, 180, k_left - k_right);
            draw_wall_vert(0, k_top, 0, k_right - k_left);
            draw_wall_vert(1, k_bottom, 180, k_right - k_left);
        }
        Matrix.setIdentityM(mRotateMatrix, 0);
        draw_wall_hor(0, k_left, 0, 0);
        draw_wall_hor(1, k_right, 180, 0);
        if (y) {
            draw_wall_hor(0, k_left, 0, k_top - k_bottom);
            draw_wall_hor(1, k_right, 180, k_top - k_bottom);
            draw_wall_hor(0, k_left, 0, k_bottom - k_top);
            draw_wall_hor(1, k_right, 180, k_bottom - k_top);
        }


        Matrix.setIdentityM(mRotateMatrix, 0);
        glUniform4f(new_Color, 0, 0, 0, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();
    }

    private void snake_main() {
        for (int i = 0; i < col; i++) {
            Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
            Matrix.translateM(mModelMatrix, 0, snake_m[i * 3], snake_m[i * 3 + 1], snake_m[i * 3 + 2]);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);
            if (y) {
                draw_snake(0, k_bottom - k_top);
                draw_snake(0, k_top - k_bottom);
            }
            if (x) {
                draw_snake(k_right - k_left, 0);
                draw_snake(k_left - k_right, 0);
            }
            if (left_top)
                draw_snake(k_left - k_right, k_top - k_bottom);
            if (left_bottom)
                draw_snake(k_left - k_right, k_bottom - k_top);
            if (right_top)
                draw_snake(k_right - k_left, k_top - k_bottom);
            if (right_bottom)
                draw_snake(k_right - k_left, k_bottom - k_top);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
            bindMatrix();
        }
    }

    private float[][] depth = new float[3][7];
    private void digit(int dig, int number, boolean[] up, boolean[] down)
    {
        float num = (float)number;
        float big = 0.025f;

//лево верх
        glUniform4f(new_Color, 1, 1, 1, 1);

        if (down[0] || dig == 0 || dig == 4 || dig == 5 || dig == 6 || dig == 8 || dig == 9) {
            if (up[0] && depth[number][0] < 0.6f)
                depth[number][0] += big;
            else if (down[0] && depth[number][0] > 0)
                depth[number][0] -= big;

            Matrix.translateM(mModelMatrix, 0, k_left + 2 * a + num * 2.3f * a, k_top, 0 + a);
            Matrix.scaleM(mScaleMatrix, 0, 0.5f, depth[number][0], 2f);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);

            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
        }

//лево низ
        if (down[1] || dig == 0 || dig == 2 || dig == 6 || dig == 8) {
            if (up[1] && depth[number][1] < 0.6f)
                depth[number][1] += big;
            else if (down[1] && depth[number][1] > 0)
                depth[number][1] -= big;

            Matrix.translateM(mModelMatrix, 0, k_left + 2 * a + num * 2.3f * a, k_top, -2 * a + a);
            Matrix.scaleM(mScaleMatrix, 0, 0.5f, depth[number][1], 2f);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);

            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
        }

//право верх
        if (down[2] || dig == 0 || dig == 1 || dig == 2 || dig == 3 || dig == 4 || dig == 7 || dig == 8 || dig == 9) {
            if (up[2] && depth[number][2] < 0.6f)
                depth[number][2] += big;
            else if (down[2] && depth[number][2] > 0)
                depth[number][2] -= big;

            Matrix.translateM(mModelMatrix, 0, k_left + 2 * a + 1.5f * a + num * 2.3f * a, k_top, 0 + a);
            Matrix.scaleM(mScaleMatrix, 0, 0.5f, depth[number][2], 2f);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);

            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
        }

//право низ
        if (down[3] || dig == 0 || dig == 1 || dig == 3 || dig == 4 || dig == 5 || dig == 6 || dig == 7 || dig == 8 || dig == 9) {
            if (up[3] && depth[number][3] < 0.6f)
                depth[number][3] += big;
            else if (down[3] && depth[number][3] > 0)
                depth[number][3] -= big;

            Matrix.translateM(mModelMatrix, 0, k_left + 2 * a + 1.5f * a + num * 2.3f * a, k_top, -2 * a + a);
            Matrix.scaleM(mScaleMatrix, 0, 0.5f, depth[number][3], 2f);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);

            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
        }

//верх
        if (down[4] || dig == 0 || dig == 2 || dig == 3 || dig == 5 || dig == 6 || dig == 7 || dig == 8 || dig == 9) {
            if (up[4] && depth[number][4] < 0.6f)
                depth[number][4] += big;
            else if (down[4] && depth[number][4] > 0)
                depth[number][4] -= big;

            Matrix.rotateM(mRotateMatrix, 0, -90, 0, 1, 0);
            Matrix.translateM(mModelMatrix, 0, k_left + 2 * a + num * 2.3f * a, k_top, -0.5f * a + a);
            Matrix.scaleM(mScaleMatrix, 0, 0.5f, depth[number][4], 2f);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);

            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
        }
//середина
        if (down[5] || dig == 2 || dig == 3 || dig == 4 || dig == 5 || dig == 6 || dig == 8 || dig == 9) {
            if (up[5] && depth[number][5] < 0.6f)
                depth[number][5] += big;
            else if (down[5] && depth[number][5] > 0)
                depth[number][5] -= big;

            Matrix.rotateM(mRotateMatrix, 0, -90, 0, 1, 0);
            Matrix.translateM(mModelMatrix, 0, k_left + 2 * a + num * 2.3f * a, k_top, -2.25f * a + a);
            Matrix.scaleM(mScaleMatrix, 0, 0.5f, depth[number][5], 2f);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);

            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
        }
//низ
        if (down[6] || dig == 0|| dig == 2 || dig == 3 || dig == 5 || dig == 6 || dig == 8 || dig == 9) {
            if (up[6] && depth[number][6] < 0.6f)
                depth[number][6] += big;
            else if (down[6] && depth[number][6] > 0)
                depth[number][6] -= big;

            Matrix.rotateM(mRotateMatrix, 0, -90, 0, 1, 0);
            Matrix.translateM(mModelMatrix, 0, k_left + 2 * a + num * 2.3f * a, k_top, -4 * a + a);
            Matrix.scaleM(mScaleMatrix, 0, 0.5f, depth[number][6], 2f);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);

            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
        }

        glUniform4f(new_Color, 0, 0, 0, 0);
        bindMatrix();
    }

    private void digit_main(int col)
    {
        boolean[] up = new boolean[7];
        boolean[] down = new boolean[7];
        for (int i = 2; i >= 0; i--){
            for (int j = 0; j < 7; j++){
                up[j] = false;
                down[j] = false;
            }
            int ost = col % 10;
            if (ost == 0) {
                up[0] = true;
                up[1] = true;
                up[2] = true;
                up[3] = true;
                up[4] = true;
                up[6] = true;
                down[5] = true;
            }
            else if (ost == 1) {
                up[2] = true;
                up[3] = true;
                down[0] = true;
                down[1] = true;
                down[4] = true;
                down[5] = true;
                down[6] = true;
            }
            else if (ost == 2){
                up[1] = true;
                up[2] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                down[0] = true;
                down[3] = true;
            }
            else if (ost == 3){
                up[2] = true;
                up[3] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                down[0] = true;
                down[1] = true;
            }
            else if (ost == 4){
                up[0] = true;
                up[2] = true;
                up[3] = true;
                up[5] = true;
                down[1] = true;
                down[4] = true;
                down[6] = true;
            }
            else if (ost == 5){
                up[0] = true;
                up[3] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                down[1] = true;
                down[2] = true;
            }
            else if (ost == 6){
                up[0] = true;
                up[1] = true;
                up[3] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                down[2] = true;
            }
            else if (ost == 7){
                up[2] = true;
                up[3] = true;
                up[4] = true;
                down[0] = true;
                down[1] = true;
                down[5] = true;
                down[6] = true;
            }
            else if (ost == 8){
                up[0] = true;
                up[1] = true;
                up[2] = true;
                up[3] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
            }
            else if (ost == 9){
                up[0] = true;
                up[2] = true;
                up[3] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                down[1] = true;
            }
            digit(ost, i, up, down);
            col /= 10;
        }
    }

    private void text()
    {
        digit_main(col);

        glUniform4f(new_Color, 1, 0, 1, 1);

        Matrix.translateM(mModelMatrix, 0, k_left + 2 * a - 0.04f, k_top, a + 0.1f);
        Matrix.scaleM(mScaleMatrix, 0, 7, 0.06f, 4.4f);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 34, 36);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();

        glUniform4f(new_Color, 0, 0, 0, 0);
    }

    private boolean check_vec(float[] cel, float[] point)
    {
        for (int i = 0; i < 3; i++)
            if (cel[i] != point[i])
                return false;
        return true;
    }

    private void shader_func(float[] a_Position, float[] tmp, int l_rotate, float scale, int l_down, int l_top, int chooseC, ObjectOutputStream oos)
    {
        if (chooseC == 0 && l_top == 1){
            if (check_vec(a_Position, new float[] {-a + a / 2 + a / 4, 0, 0})){
                if (l_rotate == 0)
                   tmp[0] = tmp[0] - (a / 2) * (scale - 1);
                else
                   tmp[0] = tmp[0] + (a / 2) * (scale - 1);
           }
           else if (check_vec(a_Position, new float[] {-a - a / 2 + a / 4, -a - a / 2 - a / 4 - a / 2, 0})){
               if (l_rotate == 0)
                   tmp[1] = tmp[1] + 7.5f / 8 * a * (scale - 1);
               else
                   tmp[1] = tmp[1] - 7.5f / 8 * a * (scale - 1);
           }
        }
        else if (chooseC == 0 && l_down == 1){
            if (check_vec(a_Position, new float[] {-a + a / 2 + a / 4, - 2 * a - a, 0})){
                if (l_rotate == 0)
                   tmp[0] = tmp[0] - (a / 2) * (scale - 1);
                else
                   tmp[0] = tmp[0] + (a / 2) * (scale - 1);
            }
            else if (check_vec(a_Position, new float[] {-a - a / 2 + a / 4, -a - a / 4 + a / 2, 0})){
                if (l_rotate == 0)
                   tmp[1] = tmp[1] - 7.5f / 8 * a * (scale - 1);
                else
                   tmp[1] = tmp[1] + 7.5f / 8 * a * (scale - 1);
            }
        }
        else if (chooseC == 1 && (l_top == 1 || l_down == 1)){//право верх
            if (check_vec(a_Position, new float[] {a + a / 2, 3 * a, 0})){
                float pif = (float)Math.sqrt(Math.pow(a_Position[0] - (-a - a / 2), 2) + Math.pow(a_Position[1], 2));
                if (l_rotate == 0)
                    tmp[1] = tmp[1] + pif * scale / 1.5f;
                else if (l_rotate == 1)
                    tmp[0] = tmp[0] + pif * scale / 1.5f;
                else if (l_rotate == 2)
                    tmp[1] = tmp[1] - pif * scale / 1.5f;
                else if (l_rotate == 3)
                    tmp[0] = tmp[0] - pif * scale / 1.5f;
            }
            else if (check_vec(a_Position, new float[] {a, 2 * a + a / 2, 0})){
                float pif = (float)Math.sqrt(Math.pow(a_Position[0] - (-a - a / 2), 2) + Math.pow(a_Position[1] - (a / 2), 2));
                float[] nnn = new float[4];
                Matrix.multiplyMV(nnn, 0, m0Matrix, 0, new float[] {a + a / 2, 3 * a, 0, 1}, 0);
                if (l_rotate == 0){
                    tmp[0] = nnn[0];
                    tmp[1] = tmp[1] + pif * scale / 1.5f;
                }
                else if (l_rotate == 1){
                    tmp[1] = nnn[1];
                    tmp[0] = tmp[0] + pif * scale / 1.5f;
                }
                else if (l_rotate == 2){
                    tmp[0] = nnn[0];
                    tmp[1] = tmp[1] - pif * scale / 1.5f;
                }
                else if (l_rotate == 3){
                    tmp[1] = nnn[1];
                    tmp[0] = tmp[0] - pif * scale / 1.5f;
                }
            }
        }
        try{
            for (int i = 0; i < 3; i++)
                oos.write(String.format("%.10f ", tmp[i]).getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void record_buttons0(int check, ObjectOutputStream oos)
    {
        float[] tmp = new float[4];
        float[] tmp_start = new float[4];
        Matrix.setIdentityM(m1Model, 0);
        Matrix.setIdentityM(m2Rotate, 0);
        Matrix.setIdentityM(m3Scale, 0);
        if (check == 1)
            Matrix.rotateM(m2Rotate, 0, 180, 0, 0, 1);

        if (check == 1)
            Matrix.translateM(m1Model, 0, 0, -3 * a * scale, 0);

        Matrix.translateM(m1Model, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        Matrix.scaleM(m3Scale, 0, scale, scale, 1);
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = 24 * 3 + j * 3; i < 3 + 24 * 3 + j * 3; i++) {
                tmp[i - (24 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (24 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 1, 0, 0, oos);//лево низ
        }

        for (int j = 0; j < 6; j++) {
            for (int i = 18 * 3 + j * 3; i < 3 + 18 * 3 + j * 3; i++) {
                tmp[i - (18 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (18 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 0, 1, 0, oos);//лево верх
        }

        Matrix.setIdentityM(m3Scale, 0);
        Matrix.scaleM(m3Scale, 0, 1, scale, 1);
        Matrix.translateM(m2Rotate, 0, -a / 4 * (scale - 1), 0, 0);
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = j * 3; i < 3 + j * 3; i++) {
                tmp[i - (j * 3)] = controlFirst[i];
                tmp_start[i - (j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 0, 0, 0, oos);//правая
        }

        Matrix.translateM(m2Rotate, 0, a / 4 * (scale - 1), 0, 0);

        Matrix.translateM(m2Rotate, 0, -(scale - 1) * a - ((a / 2) * (scale - 1)) / 2, 0, 0);//-(scaleX - 1) * a - ((a / 2) * (scaleX - 1)) / 2
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = 30 * 3 + j * 3; i < 3 + 30 * 3 + j * 3; i++) {
                tmp[i - (30 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (30 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 0, 0, 0, oos);//лево середина
        }
        Matrix.translateM(m2Rotate, 0, (scale - 1) * a + ((a / 2) * (scale - 1)) / 2, 0, 0);//(scaleX - 1) * a + ((a / 2) * (scaleX - 1)) / 2

        Matrix.setIdentityM(m3Scale, 0);
        Matrix.scaleM(m3Scale, 0, scale, 1, 1);
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = 6 * 3 + j * 3; i < 3 + 6 * 3 + j * 3; i++) {
                tmp[i - (6 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (6 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 0, 0, 0, oos);//верхняя
        }

        Matrix.translateM(m2Rotate, 0, 0, -(scale - 1) * 3 * a, 0);
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = 12 * 3 + j * 3; i < 3 + 12 * 3 + j * 3; i++) {
                tmp[i - (12 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (12 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 0, 0, 0, oos);//нижняя
        }
    }

    private void buttons0(int check)
    {
        if (check == 1) {
            Matrix.rotateM(mRotateMatrix, 0, 180, 0, 0, 1);
            glUniform1i(l_rotate, 1);
        }

        if (check == 1)
            Matrix.translateM(mModelMatrix, 0, 0, -3 * a * scale, 0);

        glUniform4f(new_Color, 0, 1, 1, 1);
        Matrix.translateM(mModelMatrix, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        Matrix.scaleM(mScaleMatrix, 0, scale, scale, 1);
        bindMatrix();

        glUniform1f(locaionScale, scale);
        glUniform1i(l_down, 1);
        glDrawArrays(GL_TRIANGLES, 94, 6);//лево низ
        glUniform1i(l_down, 0);
        glUniform1i(l_top, 1);
        glDrawArrays(GL_TRIANGLES, 88, 6);//лево верх
        glUniform1i(l_top, 0);


        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, 1, scale, 1);
        Matrix.translateM(mRotateMatrix, 0, -a / 4 * (scale - 1), 0, 0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 70, 6);//правая
        Matrix.translateM(mRotateMatrix, 0, a / 4 * (scale - 1), 0, 0);

        Matrix.translateM(mRotateMatrix, 0, -(scale - 1) * a - ((a / 2) * (scale - 1)) / 2, 0, 0);//-(scaleX - 1) * a - ((a / 2) * (scaleX - 1)) / 2
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 100, 6);//лево середина
        Matrix.translateM(mRotateMatrix, 0, (scale - 1) * a + ((a / 2) * (scale - 1)) / 2, 0, 0);//(scaleX - 1) * a + ((a / 2) * (scaleX - 1)) / 2

        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, scale, 1, 1);
        bindMatrix();

        glDrawArrays(GL_TRIANGLES, 76, 6);//верхняя

        Matrix.translateM(mRotateMatrix, 0, 0, -(scale - 1) * 3 * a, 0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 82, 6);//нижняя

        glUniform4f(new_Color, 0, 0, 0, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        bindMatrix();

        glUniform1i(l_rotate, 0);
    }

    void ass0(int check)
    {
        if (check == 1) {
            Matrix.rotateM(mRotateMatrix, 0, 180, 0, 0, 1);
            Matrix.translateM(mRotateMatrix, 0, 0, 3 * a, 0);
        }

        Matrix.rotateM(mModelMatrix, 0, 90, 1, 0, 0);

        glUniform4f(new_Color, 0, 1, 1, 1);
        Matrix.translateM(mModelMatrix, 0, -3f * a, tmp_z + 2 * a, -k_top + 0.001f);
        bindMatrix();

        glDrawArrays(GL_TRIANGLES, 130, 6);//лево низ
        glDrawArrays(GL_TRIANGLES, 124, 6);//лево верх
        glDrawArrays(GL_TRIANGLES, 106, 6);//правая
        glDrawArrays(GL_TRIANGLES, 136, 6);//лево середина
        glDrawArrays(GL_TRIANGLES, 112, 6);//верхняя
        glDrawArrays(GL_TRIANGLES, 118, 6);//нижняя

        glUniform4f(new_Color, 0, 0, 0, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        bindMatrix();
    }

    private void record_buttons1(int num, ObjectOutputStream oos)
    {
        float[] tmp = new float[4];
        float[] tmp_start = new float[4];
        Matrix.setIdentityM(m1Model, 0);
        Matrix.setIdentityM(m2Rotate, 0);
        Matrix.setIdentityM(m3Scale, 0);

        Matrix.translateM(m1Model, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        if (num == 0)
            Matrix.rotateM(m2Rotate, 0, 45, 0, 0, 1);
        else if (num == 1)
            Matrix.rotateM(m2Rotate, 0, -45, 0, 0, 1);
        else if (num == 2)
            Matrix.rotateM(m2Rotate, 0, -135, 0, 0, 1);
        else if (num == 3)
            Matrix.rotateM(m2Rotate, 0, 135, 0, 0, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a, 0, 0);//чтобы крутилась вокруг другой точки
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);//компенсировать сдвиг от scale
        Matrix.translateM(m2Rotate, 0, a * 0.15f, a * 0.15f, 0);//создать промежуток между кнопками
        glUniform1i(l_rotate, num);
        mvBindMatrix();

        glUniform1f(locaionScale, scale);

        Matrix.scaleM(m3Scale, 0, 1, scale, 1);

        Matrix.translateM(m2Rotate, 0, -1.5f * a * (scale - 1), 0, 0);
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = 6 * 3 + j * 3; i < 3 /*length_tmp*/ + 6 * 3 + j * 3; i++) {
                tmp[i - (6 * 3 + j * 3)] = controlSecond[i];
                tmp_start[i - (6 * 3 + j * 3)] = controlSecond[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, num, scale, 0, 0, 1, oos);//право низ
        }
        glDrawArrays(GL_TRIANGLES, 148, 6);//право низ

        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);

        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = 12 * 3 + j * 3; i < 3 /*length_tmp*/ + 12 * 3 + j * 3; i++) {
                tmp[i - (12 * 3 + j * 3)] = controlSecond[i];
                tmp_start[i - (12 * 3 + j * 3)] = controlSecond[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, num, scale, 0, 1, 1, oos);//право верх
        }

        Matrix.translateM(m2Rotate, 0, -1.5f * a * (scale - 1), 0, 0);
        mvBindMatrix();

        Matrix.setIdentityM(m3Scale, 0);
        Matrix.scaleM(m3Scale, 0, scale, 1, 1);
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = j * 3; i < 3 /*length_tmp*/ + j * 3; i++) {
                tmp[i - (j * 3)] = controlSecond[i];
                tmp_start[i - (j * 3)] = controlSecond[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, num, scale, 0, 0, 1, oos);//лево низ
        }

        Matrix.translateM(m2Rotate, 0, 0, 3 * a * (scale - 1), 0);
        mvBindMatrix();

        for (int j = 0; j < 6; j++) {
            for (int i = 18 * 3 + j * 3; i < 3 /*length_tmp*/ + 18 * 3 + j * 3; i++) {
                tmp[i - (18 * 3 + j * 3)] = controlSecond[i];
                tmp_start[i - (18 * 3 + j * 3)] = controlSecond[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, num, scale, 1, 0, 1, oos);//лево верх
        }
    }

    private void buttons1(int num)
    {
        Matrix.translateM(mModelMatrix, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        if (num == 0)
            Matrix.rotateM(mRotateMatrix, 0, 45, 0, 0, 1);
        else if (num == 1)
            Matrix.rotateM(mRotateMatrix, 0, -45, 0, 0, 1);
        else if (num == 2)
            Matrix.rotateM(mRotateMatrix, 0, -135, 0, 0, 1);
        else if (num == 3)
            Matrix.rotateM(mRotateMatrix, 0, 135, 0, 0, 1);
        Matrix.translateM(mRotateMatrix, 0, 1.5f * a, 0, 0);//чтобы крутилась вокруг другой точки
        Matrix.translateM(mRotateMatrix, 0, 1.5f * a * (scale - 1), 0, 0);//компенсировать сдвиг от scale
        Matrix.translateM(mRotateMatrix, 0, a * 0.15f, a * 0.15f, 0);//создать промежуток между кнопками
        glUniform1i(l_rotate, num);
        bindMatrix();

        glUniform4f(new_Color, 0, 1, 1, 1);

        glUniform1f(locaionScale, scale);

        Matrix.scaleM(mScaleMatrix, 0, 1, scale, 1);

        Matrix.translateM(mRotateMatrix, 0, -1.5f * a * (scale - 1), 0, 0);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 148, 6);//право низ
        Matrix.translateM(mRotateMatrix, 0, 1.5f * a * (scale - 1), 0, 0);
        bindMatrix();

        Matrix.translateM(mRotateMatrix, 0, 1.5f * a * (scale - 1), 0, 0);
        bindMatrix();
        glUniform1i(l_top, 1);
        glDrawArrays(GL_TRIANGLES, 154, 6);//право верх
        glUniform1i(l_top, 0);
        Matrix.translateM(mRotateMatrix, 0, -1.5f * a * (scale - 1), 0, 0);
        bindMatrix();

        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, scale, 1, 1);
        bindMatrix();


        glDrawArrays(GL_TRIANGLES, 142, 6);//лево низ

        Matrix.translateM(mRotateMatrix, 0, 0, 3 * a * (scale - 1), 0);
        bindMatrix();
        glUniform1i(l_down, 1);
        glDrawArrays(GL_TRIANGLES, 160, 6);//лево верх
        glUniform1i(l_down, 0);

        glUniform4f(new_Color, 0, 0, 0, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        bindMatrix();

        glUniform1i(l_rotate, 0);
    }

    void ass1(int num)
    {
        float sX = 0.2f;
        float sY = 0.2f;
        Matrix.rotateM(mModelMatrix, 0, 90, 1, 0, 0);
        Matrix.translateM(mModelMatrix, 0, 3f * a, tmp_z + a * 0.5f, -(k_top - 0.001f));

        if (num == 0)
            Matrix.rotateM(mRotateMatrix, 0, 45, 0, 0, 1);
        else if (num == 1)
            Matrix.rotateM(mRotateMatrix, 0, -45, 0, 0, 1);
        else if (num == 2)
            Matrix.rotateM(mRotateMatrix, 0, -135, 0, 0, 1);
        else if (num == 3)
            Matrix.rotateM(mRotateMatrix, 0, 135, 0, 0, 1);
        Matrix.translateM(mRotateMatrix, 0, 1.5f * a, 0, 0);//чтобы крутилась вокруг другой точки
        Matrix.translateM(mRotateMatrix, 0, 1.5f * a * (sY - 1), 0, 0);//компенсировать сдвиг от scale
        Matrix.translateM(mRotateMatrix, 0, a * 0.15f, a * 0.15f, 0);//создать промежуток между кнопками
        Matrix.scaleM(mScaleMatrix, 0, sX, sY, 1);
        bindMatrix();

        glUniform4f(new_Color, 0, 1, 1, 1);

        glDrawArrays(GL_TRIANGLES, 166, 6);

        glUniform4f(new_Color, 0, 0, 0, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        bindMatrix();
    }

    void mainButtons()
    {
        glUniform1i(chooseC, 0);
        if (fig0) {
            buttons0(0);
            buttons0(1);
        }
        ass0(0);
        ass0(1);

        glUniform1i(chooseC, 1);
        if (!fig0)
            for (int i = 0; i < 4; i++)
                buttons1(i);
        for (int i = 0; i < 4; i++)
            ass1(i);
    }

    void polzunok()
    {
        Matrix.scaleM(mScaleMatrix, 0, 15, 0.1f, 0.7f);
        Matrix.translateM(mModelMatrix, 0, k_left, k_top, tmp_z + 4 * a);
        bindMatrix();

        glUniform4f(new_Color, 1, 1, 1, 1);
        glDrawArrays(GL_TRIANGLES, 34, 36);

        Matrix.translateM(mModelMatrix, 0, 0, 0, -a - 0.3f * a);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 34, 36);

        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, 15, 0.01f, 2);
        Matrix.translateM(mModelMatrix, 0, 0, 0, a + 0.3f * a);
        bindMatrix();
        glUniform4f(new_Color, 1, 0.5f, 0, 1);
        glDrawArrays(GL_TRIANGLES, 34, 36);


        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, 0.7f, 0.1f, 0.6f);
        Matrix.translateM(mModelMatrix, 0, tmpX - k_left - 0.35f * a, 0, -0.7f * a);
        bindMatrix();
        glUniform4f(new_Color, 0, 1, 0, 1);
        glDrawArrays(GL_TRIANGLES, 34, 36);


        Matrix.scaleM(mScaleMatrix, 0, 1, 3, 2);
        Matrix.translateM(mModelMatrix, 0, 0, 0, 0.1f * a);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 34, 36);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        bindMatrix();
        glUniform4f(new_Color, 0, 0, 0, 0);
    }


    float animSave = 0;
    float animExit = 0;
    private void SaveAndExit()
    {
//        glUniform4f(new_Color, 1, 1, 1, 1);

        Matrix.scaleM(mScaleMatrix, 0, 2.5f, 0.2f, 1);

        if (animSave >= 10) {
            Matrix.translateM(mModelMatrix, 0, k_right / 2, k_bottom + a - (1 - (0.2f - ((10 - animSave) / 50))) * a, tmp_z + 2 * a);
            glUniform4f(new_Color, (animSave - 5) / 5, 1, (animSave - 5) / 5, 1);
        }
        else {
            Matrix.translateM(mModelMatrix, 0, k_right / 2, k_bottom + a - (1 - (0.2f - (animSave / 50))) * a, tmp_z + 2 * a);
            glUniform4f(new_Color, (5 - animSave) / 5, 1, (5 - animSave) / 5, 1);
        }
        Matrix.rotateM(mRotateMatrix, 0, 45, 0, 1, 0);
        bindMatrix();

        glDrawArrays(GL_TRIANGLES, 34, 36);

        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, 1, 0.2f, 3f);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 34, 36);

        if (animSave > 0)
            animSave--;


//        glUniform4f(new_Color, 0.5f, 0.5f, 0.5f, 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        if (animExit >= 10) {
            Matrix.translateM(mModelMatrix, 0, k_right / 2, k_bottom + a - (1 - (0.2f - ((10 - animExit) / 50))) * a, tmp_z + 2 * a);
            glUniform4f(new_Color, 1 - (animExit - 5) / 10, (animExit - 5) / 10, (animExit - 5) / 10, 1);
        }
        else {
            Matrix.translateM(mModelMatrix, 0, k_right / 2, k_bottom + a - (1 - (0.2f - (animExit / 50))) * a, tmp_z + 2 * a);
            glUniform4f(new_Color, (animExit + 5) / 10, (5 - animExit) / 10, (5 - animExit) / 10, 1);
        }
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, 4, 0.2f, 1);
        Matrix.translateM(mRotateMatrix, 0, -1.5f * a, 0, 0);
        Matrix.translateM(mModelMatrix, 0, k_left, 0, -a * 0.5f);
        bindMatrix();

        glDrawArrays(GL_TRIANGLES, 34, 36);

        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, 1, 0.2f, 4);
        Matrix.translateM(mRotateMatrix, 0, 1.5f * a, 0, 1.5f * a);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 34, 36);

        if (animExit > 0)
            animExit--;

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        bindMatrix();
        glUniform4f(new_Color, 0, 0, 0, 0);
    }

    boolean x;
    boolean y;
    boolean left_top;
    boolean right_top;
    boolean left_bottom;
    boolean right_bottom;

    public static float scale;
    public static float tmpX;
    public static boolean fig0 = false;
    @Override
    public void onDrawFrame(GL10 arg0) {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glLineWidth(3.5f);

        Matrix.setIdentityM(mModelMatrix, 0);
        x = false;
        y = false;
        left_top = false;
        right_top = false;
        left_bottom = false;
        right_bottom = false;
        draw_setka(0, 0);

        floar_main();

        wall_main();

//        snake_main();

//        text();

        mainButtons();

        polzunok();

        SaveAndExit();
    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        ///В активити ControlActivity работает, а тут нет:(
        return super.onTouchEvent(event);
    }


    private float[] vec(float z)
    {
        float[] vec = new float[4];
        vec[0] = touch[0] - eyeX;
        vec[1] = touch[1] - eyeY;
        vec[2] = touch[2] - eyeZ;
        float mn = (z - eyeZ) / vec[2];
        vec[0] *= mn;
        vec[1] *= mn;
        vec[2] *= mn;
        float[] end = new float[4];
        end[0] = eyeX + vec[0];
        end[1] = eyeY + vec[1];
        end[2] = eyeZ + vec[2];
        return end;
    }

    float[] m0Matrix = new float[16];
    float[] m1Model = new float[16];
    float[] m2Rotate = new float[16];
    float[] m3Scale = new float[16];
    private void mvBindMatrix()
    {
        Matrix.setIdentityM(m0Matrix, 0);
        Matrix.multiplyMM(m0Matrix, 0, m1Model, 0, m2Rotate, 0);
        Matrix.multiplyMM(m0Matrix, 0, m0Matrix, 0, m3Scale, 0);
    }

    private float[] checkTap()
    {
        Matrix.setIdentityM(m1Model, 0);
        Matrix.setIdentityM(m2Rotate, 0);
        Matrix.setIdentityM(m3Scale, 0);

        //верхняя; право верх
        float[] check = new float[4];

        float[] vec4 = new float[4];
        float[] vecFig = {a + a / 2, 3 * a, 0, 1};
        Matrix.translateM(m1Model, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        Matrix.rotateM(m2Rotate, 0, 45, 0, 0, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a, 0, 0);//чтобы крутилась вокруг другой точки
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);//компенсировать сдвиг от scale
        Matrix.translateM(m2Rotate, 0, a * 0.15f, a * 0.15f, 0);//создать промежуток между кнопками
        Matrix.scaleM(m3Scale, 0, 1, scale, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);
        mvBindMatrix();

        Matrix.multiplyMV(vec4, 0, m0Matrix, 0, vecFig, 0);

        float pif = (float)Math.sqrt(Math.pow(a + a / 2 - (-a - a / 2), 2) + Math.pow(3 * a, 2));
        vec4[1] += pif * scale / 1.5;
        check[3] = vec4[1];

        Matrix.setIdentityM(m1Model, 0);
        Matrix.setIdentityM(m2Rotate, 0);
        Matrix.setIdentityM(m3Scale, 0);

        //правая; право верх
        Matrix.translateM(m1Model, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        Matrix.rotateM(m2Rotate, 0, -45, 0, 0, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a, 0, 0);//чтобы крутилась вокруг другой точки
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);//компенсировать сдвиг от scale
        Matrix.translateM(m2Rotate, 0, a * 0.15f, a * 0.15f, 0);//создать промежуток между кнопками
        Matrix.scaleM(m3Scale, 0, 1, scale, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);
        mvBindMatrix();

        Matrix.multiplyMV(vec4, 0, m0Matrix, 0, vecFig, 0);

        pif = (float)Math.sqrt(Math.pow(a + a / 2 - (-a - a / 2), 2) + Math.pow(3 * a, 2));
        vec4[0] += pif * scale / 1.5;
        check[1] = vec4[0];

        Matrix.setIdentityM(m1Model, 0);
        Matrix.setIdentityM(m2Rotate, 0);
        Matrix.setIdentityM(m3Scale, 0);

        //левая; право верх
        Matrix.translateM(m1Model, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        Matrix.rotateM(m2Rotate, 0, 135, 0, 0, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a, 0, 0);//чтобы крутилась вокруг другой точки
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);//компенсировать сдвиг от scale
        Matrix.translateM(m2Rotate, 0, a * 0.15f, a * 0.15f, 0);//создать промежуток между кнопками
        Matrix.scaleM(m3Scale, 0, 1, scale, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);
        mvBindMatrix();

        Matrix.multiplyMV(vec4, 0, m0Matrix, 0, vecFig, 0);

        pif = (float)Math.sqrt(Math.pow(a + a / 2 - (-a - a / 2), 2) + Math.pow(3 * a, 2));
        vec4[0] -= pif * scale / 1.5;
        check[0] = vec4[0];

        Matrix.setIdentityM(m1Model, 0);
        Matrix.setIdentityM(m2Rotate, 0);
        Matrix.setIdentityM(m3Scale, 0);

        //нижняя; право верх
        Matrix.translateM(m1Model, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        Matrix.rotateM(m2Rotate, 0, -135, 0, 0, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a, 0, 0);//чтобы крутилась вокруг другой точки
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);//компенсировать сдвиг от scale
        Matrix.translateM(m2Rotate, 0, a * 0.15f, a * 0.15f, 0);//создать промежуток между кнопками
        Matrix.scaleM(m3Scale, 0, 1, scale, 1);
        Matrix.translateM(m2Rotate, 0, 1.5f * a * (scale - 1), 0, 0);
        mvBindMatrix();

        Matrix.multiplyMV(vec4, 0, m0Matrix, 0, vecFig, 0);

        pif = (float)Math.sqrt(Math.pow(a + a / 2 - (-a - a / 2), 2) + Math.pow(3 * a, 2));
        vec4[1] -= pif * scale / 1.5;
        check[2] = vec4[1];
        return check;
    }

    private float[] checkTap0()
    {
        Matrix.setIdentityM(m1Model, 0);
        Matrix.setIdentityM(m2Rotate, 0);
        Matrix.setIdentityM(m3Scale, 0);

        //верх леаой кнопки
        float[] check = new float[4];
        float[] vec4 = new float[4];
        float[] vecFig = {-a + a / 4, 0, 0, 1};

        Matrix.translateM(m1Model, 0, transFigX, transFigY, tmp_z - a + 0.001f);
        Matrix.scaleM(m3Scale, 0, scale, 1, 1);
        mvBindMatrix();

        Matrix.multiplyMV(vec4, 0, m0Matrix, 0, vecFig, 0);
        check[3] = vec4[1];

        //низ левой кнопки
        vecFig[0] = -a + a / 4; vecFig[1] = -3 * a; vecFig[2] = 0; vecFig[3] = 1;
        Matrix.translateM(m2Rotate, 0, 0, -(scale - 1) * 3 * a, 0);
        mvBindMatrix();

        Matrix.multiplyMV(vec4, 0, m0Matrix, 0, vecFig, 0);
        check[2] = vec4[1];

        Matrix.setIdentityM(m2Rotate, 0);
        Matrix.setIdentityM(m3Scale, 0);

        //лево левой кнопки
        vecFig[0] = -a - a / 2 + a / 4; vecFig[1] = -a - a / 2 - a / 4; vecFig[2] = 0; vecFig[3] = 1;
        Matrix.scaleM(m3Scale, 0, 1, scale, 1);
        Matrix.translateM(m2Rotate, 0, -(scale - 1) * a - ((a / 2) * (scale - 1)) / 2, 0, 0);
        mvBindMatrix();

        Matrix.multiplyMV(vec4, 0, m0Matrix, 0, vecFig, 0);
        check[0] = vec4[0];
        Matrix.setIdentityM(m2Rotate, 0);

        //право правой кнопки
        Matrix.rotateM(m2Rotate, 0, 180, 0, 0, 1);
        Matrix.translateM(m1Model, 0, 0, -3 * a * scale, 0);
        Matrix.translateM(m2Rotate, 0, -(scale - 1) * a - ((a / 2) * (scale - 1)) / 2, 0, 0);
        mvBindMatrix();

        Matrix.multiplyMV(vec4, 0, m0Matrix, 0, vecFig, 0);
        check[1] = vec4[0];
        return check;
    }

    public void recordControl() {
        String filename = "control.snake";
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            if (fig0) {
                oos.write(String.format("0%.10f|%.10f)", transFigX, transFigY - ((border0[3] - border0[2]) / 2)).getBytes());
                record_buttons0(0, oos);
                record_buttons0(1, oos);
            }
            else {
                oos.write(String.format("1%.10f|%.10f)", transFigX, transFigY).getBytes());
                for (int i = 0; i < 4; i++)
                    record_buttons1(i, oos);
            }
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static float[] touch = new float[4];
    public static boolean movePol = false;
    public float[] moveFig = {0, 0, 0};
    public float[] moveFig0 = {0, 0, 0};
    public boolean touchDown(MotionEvent event)
    {
        touch[0] = event.getX() / (width_context / (right - left)) - right;
        touch[1] = event.getY() / (height_context / (top - bottom)) - top;
        touch[1] *= -1;
        touch[2] = -near;
        touch[3] = 1;
        Matrix.multiplyMV(touch, 0, invertView, 0, touch, 0);

        float[] end = vec(tmp_z + 2.2f * a);

        if (end[1] > k_top - 0.3f * a){
            float limit = tmpX;
            tmpX = end[0];
            if (end[0] < k_left + 0.35f * a)
                tmpX = k_left + 0.35f * a;
            else if (end[0] > k_right - 0.35f * a)
                tmpX = k_right - 0.35f * a;
            movePol = true;
            if (fig0) {
                scale = tmpX / ((k_right - 0.35f * a) / 1.8f) + 3.8f;
                border0 = checkTap0();//По-хорошему не менять скэйл, а завести темповую переменную, из-за этого небольшие артефакты!!!!!!!!!
                if (border0[0] < k_left || border0[1] > k_right || border0[2] < k_bottom || border0[3] > k_top){//Проверка на выход за пределы при скэйле
                    tmpX = limit;
                    scale = tmpX / ((k_right - 0.35f * a) / 1.8f) + 3.8f;
                    border0 = checkTap0();
                }
            }
            else {
                scale = tmpX / ((k_right - 0.35f * a) / 0.25f) + 0.75f;
                border = checkTap();
                if (border[0] < k_left || border[1] > k_right || border[2] < k_bottom || border[3] > k_top) {//Проверка на выход за пределы при скэйле
                    tmpX = limit;
                    scale = tmpX / ((k_right - 0.35f * a) / 0.25f) + 0.75f;
                    border = checkTap();
                }
            }
        }
        else {
            end = vec(tmp_z - a);
            if (end[1] > k_top) {
                if (end[0] > 0) {
                    fig0 = false;
                    scale = 1;
                    transFigY = 0;
                    transFigY0 = 0;
                    border = checkTap();
                }
                else {
                    fig0 = true;
                    scale = 5.6f;
                    border0 = checkTap0();
                    transFigY = (border0[3] - border0[2]) / 2;
                    transFigY0 = transFigY;
                    border0 = checkTap0();
                }
                transFigX = 0;
                transFigX0 = 0;
                tmpX = k_right - 0.35f * a;
            }
            else if (end[1] < k_bottom){
                if (end[0] > 0) {
                    recordControl();
                    animSave = 10;
                }
                else {
                    if (animExit == 0)
                        animExit = 10;
                    try {
                        TimeUnit.MILLISECONDS.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            else if (!fig0){
                float[] check = checkTap();
                if (end[0] >= check[0] && end[0] <= check[1] && end[1] >= check[2] && end[1] <= check[3]){
                    moveFig[2] = 1;
                    moveFig[0] = end[0];
                    moveFig[1] = end[1];
                }
            }
            else if (fig0){
                float[] check = checkTap0();
                if (end[0] >= check[0] && end[0] <= check[1] && end[1] >= check[2] && end[1] <= check[3]){
                    moveFig0[2] = 1;
                    moveFig0[0] = end[0];
                    moveFig0[1] = end[1];
                }
            }
        }
        return false;
    }

    private void TFfor(float[] end)
    {
        float t_0 = transFigX0 + end[0] - moveFig[0];
        float t_1 = transFigY0 + end[1] - moveFig[1];
        transFigX = t_0;
        transFigY = t_1;
        if (t_0 - (border[1] - border[0]) / 2 < k_left)
            transFigX = k_left + (border[1] - border[0]) / 2;
        else if (t_0 + (border[1] - border[0]) / 2 > k_right)
            transFigX = k_right - (border[1] - border[0]) / 2;
        if (t_1 - (border[3] - border[2]) / 2 < k_bottom)
            transFigY = k_bottom + (border[3] - border[2]) / 2;
        else if (t_1 + (border[3] - border[2]) / 2 > k_top)
            transFigY = k_top - (border[3] - border[2]) / 2;
    }

    private void TFfor0(float[] end)
    {
        float t_0 = transFigX0 + end[0] - moveFig0[0];
        float t_1 = transFigY0 + end[1] - moveFig0[1];
        transFigX = t_0;
        transFigY = t_1;
        if (t_0 - (border0[1] - border0[0]) / 2 < k_left)
            transFigX = k_left + (border0[1] - border0[0]) / 2;
        else if (t_0 + (border0[1] - border0[0]) / 2 > k_right)
            transFigX = k_right - (border0[1] - border0[0]) / 2;
        if (t_1 - (border0[3] - border0[2]) < k_bottom)
            transFigY = k_bottom + (border0[3] - border0[2]);
        else if (t_1 > k_top)
            transFigY = k_top;
    }

    float transFigX = 0;
    float transFigY = 0;
    float transFigX0 = 0;
    float transFigY0 = 0;
    public void touchMove(MotionEvent event)
    {
        touch[0] = event.getX() / (width_context / (right - left)) - right;
        touch[1] = event.getY() / (height_context / (top - bottom)) - top;
        touch[1] *= -1;
        touch[2] = -near;
        Matrix.multiplyMV(touch, 0, invertView, 0, touch, 0);

        float[] end = vec(tmp_z + 2.2f * a);

        if (movePol) {
            float limit = tmpX;
            tmpX = end[0];
            if (end[0] < k_left + 0.35f * a)
                tmpX = k_left + 0.35f * a;
            else if (end[0] > k_right - 0.35f * a)
                tmpX = k_right - 0.35f * a;
            if (fig0) {
                scale = tmpX / ((k_right - 0.35f * a) / 1.8f) + 3.8f;
                border0 = checkTap0();
                if (border0[0] < k_left || border0[1] > k_right || border0[2] < k_bottom || border0[3] > k_top){//Проверка на выход за пределы при скэйле
                    tmpX = limit;
                    scale = tmpX / ((k_right - 0.35f * a) / 1.8f) + 3.8f;
                    border0 = checkTap0();
                }
            }
            else {
                scale = tmpX / ((k_right - 0.35f * a) / 0.25f) + 0.75f;
                border = checkTap();
                if (border[0] < k_left || border[1] > k_right || border[2] < k_bottom || border[3] > k_top) {//Проверка на выход за пределы при скэйле
                    tmpX = limit;
                    scale = tmpX / ((k_right - 0.35f * a) / 0.25f) + 0.75f;
                    border = checkTap();
                }
            }
        }
        else if (moveFig[2] == 1)
            TFfor(vec(tmp_z - a));
        else if (moveFig0[2] == 1)
            TFfor0(vec(tmp_z - a));
    }

    public float[] border0 = new float[4];//массив границ сверху снизу справа слева
    public float[] border = new float[4];//массив границ сверху снизу справа слева
    public void touchUp(MotionEvent event)
    {
        movePol = false;
        moveFig[2] = 0;
        moveFig0[2] = 0;
        transFigX0 = transFigX;
        transFigY0 = transFigY;
    }
}
