package com.example.snake3d;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.glGenTextures;
import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES20.GL_FRONT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_NONE;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCheckFramebufferStatus;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCullFace;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glIsBuffer;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;
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
import static android.opengl.GLES30.glDrawBuffers;
import static android.opengl.GLES30.glReadBuffer;

public class OpenGLRenderer extends GLSurfaceView implements Renderer {

    private final static int POSITION_COUNT = 3;
    private final static int COLOR_COUNT = 3;

    private Context context;

    private int programId;

    private FloatBuffer vertexData;
    private FloatBuffer vertexBias;
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

    private float left = -1.0f;
    private float right = 1.0f;
    private float bottom = -1.0f;
    private float top = 1.0f;
    private float near;
    private float far;

    public static int width_context;
    public static int height_context;

    public static int col = 3;//количество яйчеек в змейке изначально
    public static boolean change_digit = false;

    public static float a;
    public static float b;
    public static float speed;

    public static boolean[] anim_eat = new boolean[435];
    public static float[] snake_m = new float[435 * 3];
    public static float[] snake_before_m = new float[435 * 3];
    public static boolean[] change_border = new boolean[435];


    public static float[] snake_nach = new float[3];

    private long time = System.currentTimeMillis();
    public static boolean start = false;

    public OpenGLRenderer(Context context) {
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
            "attribute vec4 a_Position;" +
                    "attribute vec2 a_Bias;" +
                    "uniform sampler2D text;" +
                    "uniform mat4 u_Matrix;" +
                    "uniform mat4 m_model;" +
                    "uniform mat4 matLightSpace;" +
                    "uniform float shaderShadow;" +
                    "attribute vec4 a_Color;" +
                    "attribute vec3 a_Normal;" +
                    "varying float v_shaderShadow;" +
                    "varying vec4 v_Color;" +
                    "varying vec3 v_Position;" +
                    "varying vec3 v_Normal;" +
                    "varying vec2 v_Bias;" +
                    "void main() {" +
                    "mat4 r = u_Matrix * m_model;" +
                    "   v_Bias = a_Bias;" +
                    "    v_shaderShadow = shaderShadow;" +
                    "       v_Color = a_Color;" +
                    "       vec3 n_Normal = normalize(a_Normal);" +
                    "       v_Normal = mat3(m_model) * n_Normal;" +
                    "vec4 vvv = m_model * a_Position;" +
                    "       v_Position = vvv.xyz;" +
                    "if (shaderShadow == 0.0 || shaderShadow == 2.0)" +
                    "       gl_Position = u_Matrix * a_Position;" +
//                    "       gl_Position = matLightSpace * vvv;" +
                    "else" +
                    "       gl_Position = matLightSpace * vvv;" +
                    "       gl_PointSize = 20.0;" +
                    "}";


    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D text;" +
                    "uniform vec4 new_Color;" +
                    "uniform vec3 u_LightPos;" +
                    "uniform vec3 u_Camera;" +
                    "varying float v_shaderShadow;" +
                    "varying vec4 v_Color;" +
                    "varying vec3 v_Position;" +
                    "varying vec3 v_Normal;" +
                    "varying vec2 v_Bias;" +
                    "void main() {" +
                    "    if (v_shaderShadow == 0.0 || v_shaderShadow == 2.0){" +
                    "    vec3 n_Normal = normalize(v_Normal);" +
                    "    vec3 lightvector = normalize(vec3(8, -3, 8.8));"+//(2.0, -0.721, 6.8));" +//(u_LightPos - v_Position);" +
                    "    vec3 lookvector = normalize(u_Camera - v_Position);" +
                    "    float ambient = 0.3;" +
                    "    float k_diffuse = 1.0;" +
                    "    float k_specular= 0.3;" +
                    "    float distance = length(u_LightPos - v_Position);" +
                    "    float diffuse = k_diffuse * max(dot(n_Normal, lightvector), 0.0);" +
//                    "    diffuse = diffuse * (1.0 / ((0.07 * distance * distance)));" +
                    "    vec3 reflectvector = reflect(-lightvector, n_Normal);" +
                    "    float specular = k_specular * pow( max(dot(lookvector,reflectvector),0.0), 40.0 );" +
                    "   gl_FragColor = vec4(1, 1, 1, 1);" +
                    "if (v_shaderShadow == 2.0)" +
                    "gl_FragColor = texture2D(text, vec2(v_Bias.x, v_Bias.y));" +
                    "    if (new_Color == vec4(0, 0, 0, 0))" +
                    "       gl_FragColor *= (specular + diffuse + ambient) * v_Color;" +
                    "    else" +
                    "       gl_FragColor *= (specular + diffuse + ambient) * new_Color;" +
                    "    }" +
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

    private int name;

    IntBuffer depthMapFBO = IntBuffer.allocate(1);
    IntBuffer depthMap = IntBuffer.allocate(1);
    IntBuffer gl_none = IntBuffer.allocate(1);
    int shaderShadow;
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



        glCullFace(GL_FRONT);
        glGenFramebuffers(1, depthMapFBO);
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO.get(0));
        glGenTextures(1, depthMap);
        glBindTexture(GL_TEXTURE_2D, depthMap.get(0));
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width_context, height_context, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap.get(0), 0);
        glDrawBuffers(1, gl_none);
        glReadBuffer(GL_NONE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glCullFace(GL_BACK);


//        final int[] textureIds = new int[1];
//        glGenTextures(1, textureIds, 0);
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic);
//        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_2D, textureIds[0]);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//        bitmap.recycle();
//        glBindTexture(GL_TEXTURE_2D, 0);
//
//
//        name = textureIds[0];
//
        texture =
                GLES20.glGetUniformLocation(programId, "text");
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, name);
//        GLES20.glUniform1i(texture, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        bindMatrix();
    }

    public static float k_right;
    public static float k_left;
    public static float k_top;
    public static float k_bottom;

    private float z3;
    private float z1;
    private float z2;

    private void nul_alpha() {
        for (int i = 0; i < 30; i++) {
            alpha_l[i] = 0;
            alpha_l[i] = 0;
            if (i < 16) {
                alpha_t[i] = 0;
                alpha_b[i] = 0;
            }
        }
    }

    private void nul_run_close() {
        for (int i = 0; i < 30; i++) {
            Snake.run_l[i] = false;
            Snake.run_r[i] = false;
            Snake.close_l[i] = false;
            Snake.close_r[i] = false;
            if (i < 16) {
                Snake.run_t[i] = false;
                Snake.run_b[i] = false;
                Snake.close_t[i] = false;
                Snake.close_b[i] = false;
            }
        }
    }

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
        snake_nach[0] = k_left + a * 7;
        snake_nach[1] = k_top - a * 8;
        snake_nach[2] = z_snake;
        return (snake);
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
        a_s += 3;
        return (a_s);
    }

    float tmp_z;//УБРАТЬ

    float centerX = 0;
    float centerY = 0;
    int fig;
    private float[] readControl()
    {
        int space = 0;
        String filename = "control.snake";
        FileInputStream foss;
        try {
            foss = context.openFileInput(filename);

            ObjectInputStream oos = new ObjectInputStream(foss);
            int b;
            while ((b = oos.read()) >= 0)
                if ((char)b == ' ')
                    space++;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        float[] control = new float[space];

        for (int i = 0; i < space; i++)
            control[i] = 0;

        int num = 0;
        float tmp = 0;
        float len_tmp = 10;
        boolean check_float = true;
        boolean fs = true;
        boolean min = false;
        try {
            foss = context.openFileInput(filename);
            ObjectInputStream oos = new ObjectInputStream(foss);
            int b;
            fig = oos.read() - 48;
            while ((b = oos.read()) >= 0) {
                if ((char) b == ')') {
                    centerY += tmp;
                    if (min)
                        centerY *= -1;
                    tmp = 0;
                    len_tmp = 10;
                    check_float = true;
                    min = false;
                    break;
                }
                if ((char) b == '|') {
                    fs = false;
                    check_float = true;
                    centerX += tmp;
                    if (min)
                        centerX *= -1;
                    min = false;
                    tmp = 0;
                    len_tmp = 10;
                }
                else if ((char) b == ',')
                    check_float = false;
                else if ((char)b == '-')
                    min = true;
                else if (fs && (char) b >= '0' && (char) b <= '9') {
                    if (check_float)
                        centerX = centerX * 10 + b - 48;
                    else {
                        tmp += (float) (b - 48) / len_tmp;
                        len_tmp *= 10;
                    }
                } else if ((char) b >= '0' && (char) b <= '9') {
                    if (check_float)
                        centerY = centerY * 10 + b - 48;
                    else {
                        tmp += (float) (b - 48) / len_tmp;
                        len_tmp *= 10;
                    }
                }
            }
            Log.d("center", centerX + " " + centerY);
            while ((b = oos.read()) >= 0){
                if ((char)b == ' ') {
                    check_float = true;
                    control[num] = control[num] + tmp;
                    if (min)
                        control[num] *= -1;
                    tmp = 0;
                    len_tmp = 10;
                    num++;
                    min = false;
                }
                else if ((char)b == ',')
                    check_float = false;
                else if ((char)b == '-')
                    min = true;
                else if ((char)b >= '0' && (char)b <= '9'){
                    if (check_float)
                        control[num] = control[num] * 10 + b - 48;
                    else {
                        tmp = tmp + (float)(b - 48) / len_tmp;
                        len_tmp *= 10;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return control;
    }

    private int len_control;
    private float[] bias;
    private float[] control;

    private void prepareData() {
        nul_alpha();
        nul_run_close();
        z1 = -2;
        float z1_setka = z1 + 0.01f;

        z3 = 0.5f;

        float mnoz0 = 2.5f;
        float mnoz1 = 2.5f;

        k_right = right * mnoz0;
        k_left = left * mnoz0;
        k_top = top * mnoz1;
        k_bottom = -k_top;//bottom * mnoz1;

        a = (k_right - k_left) / 15;
        b = 6;
        speed = a / b;

        k_bottom = k_top - (int)((k_top - k_bottom) / a) * a;

        float z_snake = z1 + a;
        tmp_z = z_snake;

        z2 = z_snake + 2 * (z_snake - z1);

        float[] setka = new float[264];
        int ch_setka = init_setka(setka, z1_setka);

        float[] snake = init_snake(z_snake);

        int snake_ch = snake.length;

        for (int i = 0; i < 435; i++) {
            snake_m[i * 3] = 0;
            snake_m[i * 3 + 1] = -a * i;
            snake_m[i * 3 + 2] = 0;

            snake_before_m[i * 3] = 0;
            snake_before_m[i * 3 + 1] = -a * i;
            snake_before_m[i * 3 + 2] = 0;

            anim_eat[i] = false;

            change_border[i] = false;
        }

        Snake.food();

        float[] vertices_0 = init_vertices_0();

        control = readControl();
        len_control = control.length / 3;

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
        a_s = vertices_0.length + snake_ch + ch_setka;

        a_s = init_vertices(a_s, vertices);

        for (int i = 0; i < control.length; i++){
            vertices[a_s] = control[i];
            a_s++;
        }

        float[] vertices_normal = new float[vertices.length];

        for (int i = 0; i < /*10 + 10 + 2 * 2 + 2 + control.length / 9*/(vertices.length - ch_setka) / 9; i++) {
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
//                1, 0, 0,
//                1, 0, 0,
//                0, 1, 0,
//
//                1, 0, 0,
//                0, 1, 0,
//                0, 1, 0,
                0, 7f / 255f, 60f / 255f,
                0, 7f / 255f, 60f / 255f,
                0, 7f / 255f, 60f / 255f,

                0, 7f / 255f, 60f / 255f,
                0, 7f / 255f, 60f / 255f,
                0, 7f / 255f, 60f / 255f,
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
            vertices_color[i + 2] = 1;
        }

        bias = new float[vertices.length / 3 * 2];
        for (int i = 0; i < bias.length; i++)
            bias[i] = 0;

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

    int texture;
    int biasLocation;
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

        shaderShadow = glGetUniformLocation(programId, "shaderShadow");
        glUniform1f(shaderShadow, 0);

//        texture = glGetUniformLocation(programId, "text");





        float[] lightView = new float[16];
        float[] lightProjection = new float[16];

//        float eyeX = 2;
//        float eyeY = 1;
        float eyeX = 8;
        float eyeY = -6;
        float eyeZ = 6.8f;

        // точка направления камеры
        float centerX = 0;
        float centerY = 0;
        float centerZ = z1;

        // up-вектор
        float upX = 0;
        float upY = 1;
        float upZ = 0;

        Matrix.setLookAtM(lightView, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);

        float near = 0f;
        float far = 30.0f;

        Matrix.orthoM(lightProjection, 0, k_left * 1.5f, k_right * 1.5f, k_bottom * 1.5f, k_top * 1.5f, near, far);
//        Matrix.frustumM(lightProjection, 0, k_left / 2.5f, k_right / 2.5f, k_bottom / 2.5f, k_top / 2.5f, this.near, far);

        Matrix.multiplyMM(lightView, 0, lightProjection, 0, lightView, 0);
        int viewProjLightLoc = glGetUniformLocation(programId, "matLightSpace");
        glUniformMatrix4fv(viewProjLightLoc, 1, false, lightView, 0);

        float[] rt = {k_right, k_top, z1, 1};
        Matrix.multiplyMV(rt, 0, lightView, 0, rt, 0);
        float[] rb = {k_right, k_bottom, z1, 1};
        Matrix.multiplyMV(rb, 0, lightView, 0, rb, 0);
        float[] lt = {k_left, k_top, z1, 1};
        Matrix.multiplyMV(lt, 0, lightView, 0, lt, 0);
        float[] lb = {k_left, k_bottom, z1, 1};
        Matrix.multiplyMV(lb, 0, lightView, 0, lb, 0);

        lt[0] = (lt[0] + 1) / (2);
        lb[0] = (lb[0] + 1) / (2);
        rt[0] = (rt[0] + 1) / (2);
        rb[0] = (rb[0] + 1) / (2);

        lt[1] = (lt[1] + 1) / (2);
        lb[1] = (lb[1] + 1) / (2);
        rt[1] = (rt[1] + 1) / (2);
        rb[1] = (rb[1] + 1) / (2);

        bias[8] = lt[0];
        bias[9] = lt[1];

        bias[10] = lb[0];
        bias[11] = lb[1];

        bias[12] = rt[0];
        bias[13] = rt[1];

        bias[14] = lb[0];
        bias[15] = lb[1];

        bias[16] = rb[0];
        bias[17] = rb[1];

        bias[18] = rt[0];
        bias[19] = rt[1];

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, k_left + 2 * a - 0.04f, k_top, a + 0.1f);
        Matrix.scaleM(mScaleMatrix, 0, 7, 0.06f, 4.4f);
        rt = new float[]{0, -a, 0, 1};
        rb = new float[]{0, -a, z1 - tmp_z, 1};
        lt = new float[]{a, -a, 0, 1};
        lb = new float[]{a, -a, z1 - tmp_z, 1};

        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mScaleMatrix, 0);
        Matrix.multiplyMV(lb, 0, mModelMatrix, 0, lb, 0);
        Matrix.multiplyMV(lb, 0, lightView, 0, lb, 0);
        Matrix.multiplyMV(lt, 0, mModelMatrix, 0, lt, 0);
        Matrix.multiplyMV(lt, 0, lightView, 0, lt, 0);
        Matrix.multiplyMV(rb, 0, mModelMatrix, 0, rb, 0);
        Matrix.multiplyMV(rb, 0, lightView, 0, rb, 0);
        Matrix.multiplyMV(rt, 0, mModelMatrix, 0, rt, 0);
        Matrix.multiplyMV(rt, 0, lightView, 0, rt, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);

        lt[0] = (lt[0] + 1) / (2);
        lb[0] = (lb[0] + 1) / (2);
        rt[0] = (rt[0] + 1) / (2);
        rb[0] = (rb[0] + 1) / (2);

        lt[1] = (lt[1] + 1) / (2);
        lb[1] = (lb[1] + 1) / (2);
        rt[1] = (rt[1] + 1) / (2);
        rb[1] = (rb[1] + 1) / (2);

        bias[80] = rt[0];
        bias[81] = rt[1];

        bias[82] = rb[0];
        bias[83] = rb[1];

        bias[84] = lt[0];
        bias[85] = lt[1];

        bias[86] = lb[0];
        bias[87] = lb[1];

        bias[88] = lt[0];
        bias[89] = lt[1];

        bias[90] = rb[0];
        bias[91] = rb[1];

        float[] tcontrol = new float[control.length + 1];
        float[] tNewControl = new float[control.length + 1];
        for (int i = 0; i < control.length; i++)
            tcontrol[i] = control[i];
        tcontrol[control.length] = 0;

        float[] four = new float[4];
        float[] four0 = new float[4];
        for (int i = 0; i < control.length; i += 3) {
            for (int j = 0; j < 3; j++)
                four[j] = tcontrol[i + j];
            four[3] = 1;
            Matrix.multiplyMV(four0, 0, lightView, 0, four, 0);
            for (int j = 0; j < 3; j++)
                tcontrol[i + j] = four0[j];
        }
        for (int i = 0; i < control.length; i++)
            tcontrol[i] = (tcontrol[i] + 1) / 2;
        Log.d("f", tcontrol[0] + " " + tcontrol[1]+ " " + tcontrol[3] + " " + tcontrol[4] + " " + tcontrol[6] + " " + tcontrol[7]);
        Log.d("f", tcontrol[9] + " " + tcontrol[10]+ " " + tcontrol[12] + " " + tcontrol[13] + " " + tcontrol[15] + " " + tcontrol[16] + " " + tcontrol[18] + tcontrol[19]);

        int k = 0;
        for (int i = 164; i < len_control * 2 + 164; i++){
            if ((k + 1) % 3 == 0)
                k++;
            bias[i] = tcontrol[k];
            k++;
        }
        Log.d("f", len_control * 2 + " " + k);

        vertexBias = ByteBuffer
                .allocateDirect(bias.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBias.put(bias);

        //bias
        biasLocation = glGetAttribLocation(programId, "a_Bias");
        vertexBias.position(0);
        glVertexAttribPointer(biasLocation, 2, GL_FLOAT,
                false, 8, vertexBias);
        glEnableVertexAttribArray(biasLocation);
    }

    private float[] camera = new float[3];

    float[] invertView = new float[16];
    float eyeX;
    float eyeY;
    float eyeZ;
    private void createViewMatrix() {
        // точка положения камеры
        eyeX = 0;//0
        eyeY = -0.321f;//-0.321f
        eyeZ = 4.8f;//3.45f
        camera[0] = eyeX;
        camera[1] = eyeY;
        camera[2] = eyeZ;

        // точка направления камеры
        float centerX = 0;
        float centerY = 0.4f;//0.041f;//0.041f
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
//        Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    private void bindMatrix() {
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotateMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mScaleMatrix, 0);
        glUniformMatrix4fv(model, 1, false, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);
    }

    private boolean check_alpha(double[] alpha, int j) {
        for (int i = 0; i < j; i++)
            if (alpha[i] != 0)
                return (true);
        return (false);
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
        float[] snake_pos = {
                snake_nach[0] + snake_m[0],
                snake_nach[1] + snake_m[1] + (k_top - k_bottom),
                snake_nach[2] + snake_m[2],
        };
        float kof;
        if (i0 == 1)
            snake_pos[1] -= 2 * (k_top - k_bottom);
        for (int i = i0; i < 15 + i0; i++) {
            Matrix.rotateM(mRotateMatrix, 0, rot, 0, 0, 1);
            Matrix.translateM(mModelMatrix, 0, k_left + a * i + x, k, z2);//+x, чтобы копировать вправо/влево
            kof = (snake_pos[2] - z2) / (snake_pos[1] - k);
            if (Snake.run_t[i] && i0 == 0)
                alpha_t[i] = 90 + (Math.atan(kof) * 180 / Math.PI);
            else if (Snake.run_b[i] && i0 == 1)
                alpha_b[i] = -(40 + 23 + (Math.atan(kof) * 180 / Math.PI));
            else if (Snake.close_t[i] && i0 == 0)
                alpha_t[i] -= 3;
            else if (Snake.close_b[i] && i0 == 1)
                alpha_b[i] -= 3;
            if (alpha_t[i] <= 0) {
                alpha_t[i] = 0;
                Snake.close_t[i] = false;
            }
            if (alpha_b[i] <= 0) {
                alpha_b[i] = 0;
                Snake.close_b[i] = false;
            }
            if (i0 == 0) {
                Matrix.rotateM(mRotateMatrix, 0, Math.round(alpha_t[i]), 1, 0, 0);
                Matrix.rotateM(mRotateMatrix, 0, Math.round(-alpha_b[i + 1]), 1, 0, 0);
            } else if (i0 == 1) {
                Matrix.rotateM(mRotateMatrix, 0, Math.round(alpha_b[i]), 1, 0, 0);
                Matrix.rotateM(mRotateMatrix, 0, Math.round(-alpha_t[i - 1]), 1, 0, 0);
            }
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 70, 6);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mRotateMatrix, 0);
        }
    }

    private void draw_wall_hor(int i0, float k, int rot, float y) {
        float[] snake_pos = {
                snake_nach[0] + snake_m[0] + (k_right - k_left),
                snake_nach[1] + snake_m[1],
                snake_nach[2] + snake_m[2],
        };
        float kof;
        if (i0 == 0)
            snake_pos[0] -= 2 * (k_right - k_left);
        for (int i = i0; i < 29 + i0; i++) {
            Matrix.rotateM(mRotateMatrix, 0, rot, 0, 0, 1);
            Matrix.translateM(mModelMatrix, 0, k, k_bottom + a * i + y, z2);//+y, чтобы копировать вверх/вниз
            kof = (snake_pos[2] - z2) / (snake_pos[0] - k);
            if (i0 == 1)
                kof = (snake_pos[2] - z2) / (snake_pos[0] + a - k);
            if (Snake.run_l[i] && i0 == 0)
                alpha_l[i] = 90 - (Math.atan(kof) * 180 / Math.PI);
            else if (Snake.run_r[i] && i0 == 1)
                alpha_r[i] = 90 + (Math.atan(kof) * 180 / Math.PI);
            else if (Snake.close_l[i] && i0 == 0)
                alpha_l[i] -= 3;
            else if (Snake.close_r[i] && i0 == 1)
                alpha_r[i] -= 3;
            if (alpha_l[i] <= 0) {
                alpha_l[i] = 0;
                Snake.close_l[i] = false;
            }
            if (alpha_r[i] <= 0) {
                alpha_r[i] = 0;
                Snake.close_r[i] = false;
            }
            if (i0 == 0) {
                Matrix.rotateM(mRotateMatrix, 0, Math.round(alpha_l[i]), 0, 1, 0);
                Matrix.rotateM(mRotateMatrix, 0, Math.round(-alpha_r[i + 1]), 0, 1, 0);
            } else if (i0 == 1) {
                Matrix.rotateM(mRotateMatrix, 0, Math.round(alpha_r[i]), 0, 1, 0);
                Matrix.rotateM(mRotateMatrix, 0, Math.round(-alpha_l[i - 1]), 0, 1, 0);
            }
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 76, 6);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mRotateMatrix, 0);
        }
    }

    private void setka_main() {
        if (check_alpha(alpha_t, 16) || check_alpha(alpha_b, 16)) {
            draw_setka(0, k_top - k_bottom);
            draw_setka(0, k_bottom - k_top);
            y = true;
            if (alpha_t[0] != 0 || alpha_t[1] != 0 || alpha_t[2] != 0 || alpha_b[1] != 0 || alpha_b[2] != 0 || alpha_b[3] != 0) {
                draw_setka(k_left - k_right, k_top - k_bottom);
                draw_setka(k_left - k_right, k_bottom - k_top);
                left_top = true;
                left_bottom = true;
            }
            if (alpha_t[14] != 0 || alpha_t[13] != 0 || alpha_t[12] != 0 || alpha_b[15] != 0 || alpha_b[14] != 0 || alpha_b[13] != 0) {
                draw_setka(k_right - k_left, k_top - k_bottom);
                draw_setka(k_right - k_left, k_bottom - k_top);
                right_bottom = true;
                right_top = true;
            }
        }
        if (check_alpha(alpha_l, 30) || check_alpha(alpha_r, 30)) {
            draw_setka(k_right - k_left, 0);
            draw_setka(k_left - k_right, 0);
            x = true;
            if (alpha_l[0] != 0 || alpha_l[1] != 0 || alpha_l[2] != 0 || alpha_r[1] != 0 || alpha_r[2] != 0 || alpha_r[3] != 0) {
                if (!left_bottom) {
                    left_bottom = true;
                    draw_setka(k_left - k_right, k_bottom - k_top);
                }
                if (!right_bottom) {
                    right_bottom = true;
                    draw_setka(k_right - k_left, k_bottom - k_top);
                }
            }
            if (alpha_l[28] != 0 || alpha_l[27] != 0 || alpha_l[26] != 0 || alpha_r[29] != 0 || alpha_r[28] != 0 || alpha_r[27] != 0) {
                if (!left_top) {
                    left_top = true;
                    draw_setka(k_left - k_right, k_top - k_bottom);
                }
                if (!right_top) {
                    right_top = true;
                    draw_setka(k_right - k_left, k_top - k_bottom);
                }
            }
        }
    }

    private void floar_main() {
        glUniform1f(shaderShadow, 2);
        glDrawArrays(GL_TRIANGLES,4, 6);
        glUniform1f(shaderShadow, 0);

//        glDrawArrays(GL_TRIANGLES, 10, 24);//ВКЛЮЧИТЬ
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
        glUniform4f(new_Color, 0, 7f / 90f, 60f / 90f, 1);

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

    private void snake_main(int check) {
        for (int i = 0; i < col - check; i++) {
            if (check == 0 && i < col - 2 && anim_eat[i + 2]) {
//                glUniform4f(new_Color, 0.9f, 0.9f, 0, 1);
                Matrix.scaleM(mScaleMatrix, 0, 1, 1, 1.3f);
                Matrix.translateM(mModelMatrix, 0, 0, 0, 0.15f);
            } else if (check == 0 && i < col - 1 && anim_eat[i + 1]) {
//                glUniform4f(new_Color, 0.8f, 0.8f, 0, 1);
                Matrix.scaleM(mScaleMatrix, 0, 1, 1, 1.6f);
                Matrix.translateM(mModelMatrix, 0, 0, 0, 0.3f);
            } else if (check == 0 && anim_eat[i]) {
//                glUniform4f(new_Color, 0.7f, 0.7f, 0, 1);
                Matrix.scaleM(mScaleMatrix, 0, 1, 1, 1.9f);
                Matrix.translateM(mModelMatrix, 0, 0, 0, 0.45f);
            } else if (check == 0 && i > 0 && anim_eat[i - 1]) {
//                glUniform4f(new_Color, 0.8f, 0.8f, 0, 1);
                Matrix.scaleM(mScaleMatrix, 0, 1, 1, 1.6f);
                Matrix.translateM(mModelMatrix, 0, 0, 0, 0.3f);
            } else if (check == 0 && i > 1 && anim_eat[i - 2]) {
//                glUniform4f(new_Color, 0.9f, 0.9f, 0, 1);
                Matrix.scaleM(mScaleMatrix, 0, 1, 1, 1.3f);
                Matrix.translateM(mModelMatrix, 0, 0, 0, 0.15f);
            }
            Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
            if (check == 0)
                Matrix.translateM(mModelMatrix, 0, snake_m[i * 3], snake_m[i * 3 + 1], snake_m[i * 3 + 2]);
            else
                Matrix.translateM(mModelMatrix, 0, snake_before_m[i * 3], snake_before_m[i * 3 + 1], snake_before_m[i * 3 + 2]);
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
            glUniform4f(new_Color, 0f, 0f, 0, 0);
        }
    }

    public static final float scale_food0 = 0.05f;//0.03125f;
    public static float scale_food = scale_food0;
    private void food_main() {
        Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
        Matrix.translateM(mModelMatrix, 0, Snake.food[0], Snake.food[1], Snake.food[2]);

        Matrix.scaleM(mScaleMatrix, 0, 1, 1, scale_food);
        Matrix.translateM(mModelMatrix, 0, 0, 0, (scale_food - 1) * a);
        bindMatrix();

        if (scale_food < 1)
            scale_food += 0.05f;

        glUniform4f(new_Color, 127f / 255f, 130f / 255f, 157f / 255f, 1);

        glDrawArrays(GL_TRIANGLES, 34, 30);
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
        bindMatrix();

        glUniform4f(new_Color, 0, 0, 0, 0);
    }

    private void anim_next() {
        for (int i = col - 1; i >= 0; i--) {
            if (anim_eat[i]) {
                anim_eat[i] = false;
                if (i < col - 1)
                    anim_eat[i + 1] = true;
            }
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
                if (depth[i][5] > 0)
                    down[5] = true;
            }
            else if (ost == 1) {
                up[2] = true;
                up[3] = true;
                if (depth[i][0] > 0)
                    down[0] = true;
                if (depth[i][1] > 0)
                    down[1] = true;
                if (depth[i][4] > 0)
                    down[4] = true;
                if (depth[i][5] > 0)
                    down[5] = true;
                if (depth[i][6] > 0)
                    down[6] = true;
            }
            else if (ost == 2){
                up[1] = true;
                up[2] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                if (depth[i][0] > 0)
                    down[0] = true;
                if (depth[i][3] > 0)
                    down[3] = true;
            }
            else if (ost == 3){
                up[2] = true;
                up[3] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                if (depth[i][0] > 0)
                    down[0] = true;
                if (depth[i][1] > 0)
                    down[1] = true;
            }
            else if (ost == 4){
                up[0] = true;
                up[2] = true;
                up[3] = true;
                up[5] = true;
                if (depth[i][1] > 0)
                    down[1] = true;
                if (depth[i][4] > 0)
                    down[4] = true;
                if (depth[i][6] > 0)
                    down[6] = true;
            }
            else if (ost == 5){
                up[0] = true;
                up[3] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                if (depth[i][1] > 0)
                    down[1] = true;
                if (depth[i][2] > 0)
                    down[2] = true;
            }
            else if (ost == 6){
                up[0] = true;
                up[1] = true;
                up[3] = true;
                up[4] = true;
                up[5] = true;
                up[6] = true;
                if (depth[i][2] > 0)
                    down[2] = true;
            }
            else if (ost == 7){
                up[2] = true;
                up[3] = true;
                up[4] = true;
                if (depth[i][0] > 0)
                    down[0] = true;
                if (depth[i][1] > 0)
                    down[1] = true;
                if (depth[i][5] > 0)
                    down[5] = true;
                if (depth[i][6] > 0)
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
                if (depth[i][1] > 0)
                    down[1] = true;
            }
            digit(ost, i, up, down);
            col /= 10;
        }
    }

    private void text()
    {
        digit_main(col);

        glUniform4f(new_Color, 0, 7f / 90f, 60f / 90f, 1);

        Matrix.translateM(mModelMatrix, 0, k_left + 2 * a - 0.04f, k_top, a + 0.1f);
        Matrix.scaleM(mScaleMatrix, 0, 7, 0.06f, 4.4f);
        bindMatrix();
        glUniform1f(shaderShadow, 2);
        glDrawArrays(GL_TRIANGLES, 34, 36);
        glUniform1f(shaderShadow, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();

        glUniform4f(new_Color, 0, 0, 0, 0);
    }

    public static double[] alpha_l = new double[30];
    public static double[] alpha_r = new double[30];
    public static double[] alpha_t = new double[16];
    public static double[] alpha_b = new double[16];

    boolean x;
    boolean y;
    boolean left_top;
    boolean right_top;
    boolean left_bottom;
    boolean right_bottom;
    int xz = 1;
    float[] forButtons = {0, 0, 0, 0};

    private float[] get_color(float[] color1, float[] color2, float koef)
    {
        float[] color = new float[3];
        color[0] = (1 - koef) * color1[0] + koef * color2[0];
        color[1] = (1 - koef) * color1[1] + koef * color2[1];
        color[2] = (1 - koef) * color1[2] + koef * color2[2];
        return color;
    }

    private void colorButtons(int i)
    {
        float koef;
        float[] color;
        float[] colorDeafault = {127f / 255f, 130f / 255f, 157f / 255f};
        float[] colorAnim = {0, 7f / 255f, 60f / 255f};
        if (forButtons[i] >= 15) {
            koef = 1 - (forButtons[i] - 15) / 15;
            color = get_color(colorDeafault, colorAnim, koef);
            glUniform4f(new_Color, color[0], color[1], color[2], 1);
            forButtons[i]--;
        }
        else {
            koef = 1 - forButtons[i] / 15;
            color = get_color(colorAnim, colorDeafault, koef);
            glUniform4f(new_Color, color[0], color[1], color[2], 1);
            if (forButtons[i] > 0)
                forButtons[i]--;
        }
    }

//    private long FPStime = System.currentTimeMillis();
//    private int FPScount = 0;
    @Override
    public void onDrawFrame(GL10 arg0) {
        if (System.currentTimeMillis() - time > 13 && start){
            Snake.move();
            time = System.currentTimeMillis();
            if (xz % 2 == 0){
                anim_next();
                xz = 0;
            }
        }
        xz++;//Для анимации

//        FPScount++;
//        if (System.currentTimeMillis() - FPStime >= 1000){
//            Log.d("FPS", FPScount + "");
//            FPScount = 0;
//            FPStime = System.currentTimeMillis();
//        }

        glUniform1f(shaderShadow, 1);
        glViewport(0, 0, width_context, height_context);

        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO.get(0));
        glClear(GL_DEPTH_BUFFER_BIT);

        snake_main(0);
        snake_main(1);

        food_main();

        digit_main(col);

        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
//
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//        glActiveTexture(GL_TEXTURE0);////////хз, надо ли
        glBindTexture(GL_TEXTURE_2D, depthMap.get(0));
        glUniform1i(texture, 0);

        glUniform1f(shaderShadow, 0);
        glViewport(0, 0, width_context, height_context);




        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glLineWidth(3.5f);

        Matrix.setIdentityM(mModelMatrix, 0);
        x = false;
        y = false;
        left_top = false;
        right_top = false;
        left_bottom = false;
        right_bottom = false;
//        draw_setka(0, 0);
        //////Проверка, где отрисовывать сетку и все остальное
//        setka_main();

        floar_main();

        wall_main();

        snake_main(0);
        snake_main(1);

        food_main();

        text();


        glUniform1f(shaderShadow, 2);
        if (fig == 0) {
            colorButtons(0);
            glDrawArrays(GL_TRIANGLES, 82, len_control / 2);
            colorButtons(1);
            glDrawArrays(GL_TRIANGLES, 82 + len_control / 2, len_control / 2);
        }
        else if (fig == 1){
            colorButtons(0);
            glDrawArrays(GL_TRIANGLES, 82, len_control / 4);
            colorButtons(1);
            glDrawArrays(GL_TRIANGLES, 82 + len_control / 4, len_control / 4);
            colorButtons(2);
            glDrawArrays(GL_TRIANGLES, 82 + len_control / 2, len_control / 4);
            colorButtons(3);
            glDrawArrays(GL_TRIANGLES, 82 + len_control / 4 * 3, len_control / 4);
        }
        glUniform1f(shaderShadow, 0);
        glUniform4f(new_Color, 0, 0, 0, 0);
    }

    private float[] vec(float z, float[] touch)
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()== MotionEvent.ACTION_DOWN){
            if (event.getX() > width_context / 4 * 3 && event.getY() < height_context / 4) {
                if (start)
                    start = false;
                else
                    start = true;
                return super.onTouchEvent(event);
            }
            float[] touch = new float[4];
            touch[0] = event.getX() / (width_context / (right - left)) - right;
            touch[1] = event.getY() / (height_context / (top - bottom)) - top;
            touch[1] *= -1;
            touch[2] = -near;
            touch[3] = 1;
            Matrix.multiplyMV(touch, 0, invertView, 0, touch, 0);

            float[] end = vec(tmp_z - a, touch);
            if (fig == 0) {
                if (end[0] > centerX) {
                    if (Snake.direction == "up")
                        Snake.direction_0 = "right";
                    else if (Snake.direction == "right")
                        Snake.direction_0 = "down";
                    else if (Snake.direction == "down")
                        Snake.direction_0 = "left";
                    else if (Snake.direction == "left")
                        Snake.direction_0 = "up";
                    forButtons[1] = 30;
                }
                else if (end[0] <= centerX) {
                    if (Snake.direction == "up")
                        Snake.direction_0 = "left";
                    else if (Snake.direction == "left")
                        Snake.direction_0 = "down";
                    else if (Snake.direction == "down")
                        Snake.direction_0 = "right";
                    else if (Snake.direction == "right")
                        Snake.direction_0 = "up";
                    forButtons[0] = 30;
                }
            }
            else if (fig == 1){
                float d0 = (end[0] - centerX) * ((centerY + 1) - centerY) - (end[1] - centerY) * ((centerX + 1) - centerX);
                float d1 = (end[0] - centerX) * ((centerY + 1) - centerY) - (end[1] - centerY) * ((centerX - 1) - centerX);
                if ((d0 > 0 && d1 > 0) || (d1 == 0 && d0 < 0)){
                    if (Snake.direction != "left")
                        Snake.direction_0 = "right";
                    forButtons[1] = 30;
                }
                else if ((d0 < 0 && d1 < 0) || (d0 == 0 && d1 <=0)){
                    if (Snake.direction != "right")
                        Snake.direction_0 = "left";
                    forButtons[3] = 30;
                }
                else if ((d0 > 0 && d1 < 0) || (d1 == 0 && d0 > 0)){
                    if (Snake.direction != "up")
                        Snake.direction_0 = "down";
                    forButtons[2] = 30;
                }
                else if ((d0 < 0 && d1 > 0) || (d0 == 0 && d1 > 0)){
                    if (Snake.direction != "down")
                        Snake.direction_0 = "up";
                    forButtons[0] = 30;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
