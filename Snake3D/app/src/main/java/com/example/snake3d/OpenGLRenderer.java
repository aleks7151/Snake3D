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
    public static int[] chb_dir = new int[435];


    public static float[] snake_nach = new float[3];

    private long time = System.currentTimeMillis();
    public static boolean start = false;

//    float[] color_floar = {0.85f, 0.85f, 0.85f};
//    float[] color_snake = {1, 1, 1};
//    float[] color_food = {0.75f, 0.75f, 0.75f};

    float[] color_floar = {168f / 255f, 36f / 255f, 147f / 255f};
    float[] color_snake = {253f / 255f, 240f / 255f, 6f / 255f};
    float[] color_food =  {1f / 255f, 169f / 255f, 99f / 255f};

//    float[] color_floar = {23f / 255f, 49f / 255f, 130f / 255f};
//    float[] color_snake = {3f / 255f, 104f / 255f, 179f / 255f};
//    float[] color_food =  {89f / 255f, 198f / 255f, 199f / 255f};

    float[] color_buttons = {color_snake[0], color_snake[1], color_snake[2]};
    float[] color_digits = {color_food[0], color_food[1], color_food[2]};

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
                    "varying vec4 v_proj;" +
                    "void main() {" +
                    "   v_shaderShadow = shaderShadow;" +
                    "   vec4 vvv = m_model * a_Position;" +
                    "   if (shaderShadow == 0.0){" +
                    "       v_Color = a_Color;" +
                    "       vec3 n_Normal = normalize(a_Normal);" +
                    "       v_Normal = mat3(m_model) * n_Normal;" +
                    "       v_proj = matLightSpace * vec4(vvv.xyz, 1.0);" +
                    "       v_Position = vvv.xyz;" +
                    "       gl_Position = u_Matrix * vvv;" +
//                    "       gl_Position = matLightSpace * vvv;" +
                    "   }" +
                    "   else" +
                    "       gl_Position = matLightSpace * vvv;" +
                    "   gl_PointSize = 5.0;" +
                    "}";


    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform mat4 u_Matrix;" +
                    "uniform mat4 matLightSpace;" +
                    "uniform sampler2D text;" +
                    "uniform vec4 new_Color;" +
                    "uniform vec3 u_LightPos;" +
                    "uniform vec3 u_Camera;" +
                    "varying float v_shaderShadow;" +
                    "varying vec4 v_Color;" +
                    "varying vec3 v_Position;" +
                    "varying vec3 v_Normal;" +
                    "varying vec4 v_proj;" +
                    "void main() {" +
                    "   if (v_shaderShadow == 0.0){" +
                    "   vec3 n_Normal = normalize(v_Normal);" +
                    "   vec3 lightvector = normalize(vec3(8, -3, 8.8));"+//(2.0, -0.721, 6.8));" +//(u_LightPos - v_Position);" +
                    "   vec3 lookvector = normalize(u_Camera - v_Position);" +
                    "   float ambient = 0.55;" +
                    "   float k_diffuse = 1.5;" +
                    "   float k_specular= 0.3;" +
                    "   float distance = length(u_LightPos - v_Position);" +
                    "   float diffuse = k_diffuse * max(dot(n_Normal, lightvector), 0.0);" +
                    "   diffuse = diffuse * (1.0 / ((0.07 * distance * distance)));" +
                    "   vec3 reflectvector = reflect(-lightvector, n_Normal);" +
                    "   float specular = k_specular * pow(max(dot(lookvector,reflectvector),0.0), 40.0 );" +
                    "   vec2 textSize = 1.0 / vec2(1024, 2048);" +
//                    "   vec4 proj = matLightSpace * vec4(v_Position.xyz, 1.0);" +8 -6 6.8
                    "   vec4 proj = vec4((v_proj.xyz / v_proj.w), 1.0);" +
                    "   proj = (proj + 1.0) / 2.0;" +
                    "   float shadow = 0.0;" +
                    "   for (int x = -1; x <= 1; ++x){" +
                    "       for (int y = -1; y <= 1; ++y){" +
                    "          float r = texture2D(text, vec2(proj.x, proj.y) + vec2(x, y) * textSize).r;" +
                    "          if (proj.z - r > 0.001)" +
                    "               shadow += 0.5;" +
                    "       }" +
                    "   }" +
                    "   shadow /= 9.0;" +
//                    "          float r = texture2D(text, vec2(proj.x, proj.y)).r;" +
//                    "          if (proj.z - r > 0.001)" +
//                    "               shadow += 0.5;" +
                    "   if (new_Color == vec4(0, 0, 0, 0))" +
                    "       gl_FragColor = (specular + diffuse + ambient) * v_Color * (1.0 - shadow);" +
                    "   else" +
                    "       gl_FragColor = (specular + diffuse + ambient) * new_Color * (1.0 - shadow);" +
                    "   }" +
                    "else" +
                    "   gl_FragColor = vec4(v_Position, 1.0);" +
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
        for (int i = 0; i < alpha_l.length; i++) {
            alpha_l[i] = 0;
            alpha_l[i] = 0;
            if (i < alpha_t.length) {
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
                k_left * 1.2f, k_top * 1.2f, z1,
                k_left * 1.2f, k_bottom * 1.2f, z1,
                k_right * 1.2f, k_top * 1.2f, z1,

                k_left * 1.2f, k_bottom * 1.2f, z1,
                k_right * 1.2f, k_bottom * 1.2f, z1,
                k_right * 1.2f, k_top * 1.2f, z1,
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

    private void prepareData() {
        nul_alpha();
        nul_run_close();
        z1 = -2;
        float z1_setka = z1 + 0.01f;

        z3 = 0.5f;

        float mnoz0 = 2.5f;
        float mnoz1 = 2.5f;

        k_right = right * mnoz0 + 0.5f;
        k_left = left * mnoz0 - 0.5f;
        k_top = top * mnoz1 + 0.5f;
        k_bottom = -k_top;//bottom * mnoz1;

        a = (k_right - k_left) / 18f;
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
            chb_dir[i] = -1;
        }

        Snake.food();

        float[] vertices_0 = init_vertices_0();

        float [] control = readControl();
        len_control = control.length / 3;

        float[] vertices = new float[vertices_0.length + ch_setka + snake_ch + 2 * 2 * 3 * 3 + control.length + /*Прямые вместо кнопок*/12];
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

        if (fig == 0) {
            vertices[a_s] = centerX;
            a_s++;
            vertices[a_s] = k_bottom;
            a_s++;
            vertices[a_s] = control[control.length - 1];
            a_s++;
            vertices[a_s] = centerX;
            a_s++;
            vertices[a_s] = k_top;
            a_s++;
            vertices[a_s] = control[control.length - 1];
        }
        else{
            vertices[a_s] = centerX + k_right;
            a_s++;
            vertices[a_s] = centerY + k_top;
            a_s++;
            vertices[a_s] = control[control.length - 1];
            a_s++;
            vertices[a_s] = centerX - k_right;
            a_s++;
            vertices[a_s] = centerY - k_top;
            a_s++;
            vertices[a_s] = control[control.length - 1];
            a_s++;
            vertices[a_s] = centerX + k_right;
            a_s++;
            vertices[a_s] = centerY - k_top;
            a_s++;
            vertices[a_s] = control[control.length - 1];
            a_s++;
            vertices[a_s] = centerX - k_right;
            a_s++;
            vertices[a_s] = centerY + k_top;
            a_s++;
            vertices[a_s] = control[control.length - 1];
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

        for (int i = vertices.length - control.length - 12; i < vertices.length; i += 3){
            vertices_normal[i] = 0;
            vertices_normal[i + 1] = 0;
            vertices_normal[i + 2] = 1;
        }

        for (int i = 0; i < ch_setka; i += 3) {
            vertices_normal[i] = vertices_normal[ch_setka];
            vertices_normal[i + 1] = vertices_normal[ch_setka + 1];
            vertices_normal[i + 2] = vertices_normal[ch_setka + 2];
        }

        float[] vertices_color_0 = {
                color_floar[0], color_floar[1], color_floar[2],
                color_floar[0], color_floar[1], color_floar[2],
                color_floar[0], color_floar[1], color_floar[2],
                color_floar[0], color_floar[1], color_floar[2],
                color_floar[0], color_floar[1], color_floar[2],
                color_floar[0], color_floar[1], color_floar[2],
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
            vertices_color[i] = color_snake[0];
            vertices_color[i + 1] = color_snake[1];
            vertices_color[i + 2] = color_snake[2];
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

    int texture;
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
        glUniform3f(uLightLocation, 0, 0, 2.8f);//0, 0, 0.8

        model = glGetUniformLocation(programId, "m_model");

        int uCameraLocation = glGetUniformLocation(programId, "u_Camera");
        glUniform3f(uCameraLocation, camera[0], camera[1], camera[2]);

        shaderShadow = glGetUniformLocation(programId, "shaderShadow");
        glUniform1f(shaderShadow, 0);

        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0);


        float[] lightView = new float[16];
        float[] lightProjection = new float[16];

        float eyeX = 8;
        float eyeY = -6;
        float eyeZ = 6.8f;
//        float eyeX = k_left * 1.8f;
//        float eyeY = k_top * 0.4f;
//        float eyeZ = 1;

        // точка направления камеры
        float centerX = 0;
        float centerY = 0;
        float centerZ = z1;
//        float centerX = k_right * 0.5f;
//        float centerY = k_top;
//        float centerZ = (tmp_z - a);
        Log.d("f", "x " + eyeX + " y " + eyeY);

        Matrix.setLookAtM(lightView, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, 0, 1, 0);

        float near = 0f;
        float far = 30.0f;

        Matrix.orthoM(lightProjection, 0, k_left * 1.5f, k_right * 1.5f, k_bottom * 1.5f, k_top * 1.5f, near, far);
//        Matrix.frustumM(lightProjection, 0, k_left / 4.2f, k_right / 4.2f, k_bottom / 4.2f, k_top / 4.2f, 2, far);

        Matrix.multiplyMM(lightView, 0, lightProjection, 0, lightView, 0);
        int viewProjLightLoc = glGetUniformLocation(programId, "matLightSpace");
        glUniformMatrix4fv(viewProjLightLoc, 1, false, lightView, 0);
    }

    private float[] camera = new float[3];

    float[] invertView = new float[16];
    float eyeX;
    float eyeY;
    float eyeZ;
    private void createViewMatrix() {
        // точка положения камеры
        eyeX = -0.1f;//0
        eyeY = 0.9f;//-0.321f;//-0.321f
        eyeZ = 4.3f;//4.8f;//3.45f
        camera[0] = eyeX;
        camera[1] = eyeY;
        camera[2] = eyeZ;

        // точка направления камеры
        float centerX = -0.1f;
        float centerY = 0.9f;//0.4f;//0.041f;//0.041f
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

    float[] temp = new float[16];

    private void bindMatrix() {
        Matrix.multiplyMM(temp, 0, mModelMatrix, 0, mRotateMatrix, 0);
        Matrix.multiplyMM(temp, 0, temp, 0, mScaleMatrix, 0);
        glUniformMatrix4fv(model, 1, false, temp, 0);
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
        for (int i = i0; i < alpha_t.length - 1 + i0; i++) {
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
        for (int i = i0; i < alpha_l.length - 1 + i0; i++) {
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

    private void floar_main() {
        glDrawArrays(GL_TRIANGLES,4, 6);

        glUniform4f(new_Color, color_floar[0], color_floar[1], color_floar[2], 1);
        glDrawArrays(GL_TRIANGLES, 10, 24);//ВКЛЮЧИТЬ
        glUniform4f(new_Color, 0, 0, 0, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();
    }

    private void wall_main() {
        glUniform4f(new_Color, color_floar[0], color_floar[1], color_floar[2], 1);

        draw_wall_vert(0, k_top, 0, 0);
        draw_wall_vert(1, k_bottom, 180, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        draw_wall_hor(0, k_left, 0, 0);
        draw_wall_hor(1, k_right, 180, 0);


        Matrix.setIdentityM(mRotateMatrix, 0);
        glUniform4f(new_Color, 0, 0, 0, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();
    }

    private void snake_change()
    {
        for (int i = 0; i < col; i++){
            if (chb_dir[i] == 0) {
                if (i != col - 1){
                    Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
                    Matrix.translateM(mModelMatrix, 0, snake_m[i * 3] - a, snake_m[i * 3 + 1], snake_m[i * 3 + 2]);
                    bindMatrix();

                    glDrawArrays(GL_TRIANGLES, 34, 36);

                    Matrix.setIdentityM(mModelMatrix, 0);
                }
                Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
                Matrix.translateM(mModelMatrix, 0, snake_m[i * 3] + k_right - k_left, snake_m[i * 3 + 1], snake_m[i * 3 + 2]);
                bindMatrix();

                glDrawArrays(GL_TRIANGLES, 34, 36);

                Matrix.setIdentityM(mModelMatrix, 0);
                bindMatrix();
            }
            else if (chb_dir[i] == 1){
                if (i != col - 1){
                    Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
                    Matrix.translateM(mModelMatrix, 0, snake_m[i * 3] + a, snake_m[i * 3 + 1], snake_m[i * 3 + 2]);
                    bindMatrix();

                    glDrawArrays(GL_TRIANGLES, 34, 36);

                    Matrix.setIdentityM(mModelMatrix, 0);
                }
                Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
                Matrix.translateM(mModelMatrix, 0, snake_m[i * 3] - (k_right - k_left), snake_m[i * 3 + 1], snake_m[i * 3 + 2]);
                bindMatrix();

                glDrawArrays(GL_TRIANGLES, 34, 36);

                Matrix.setIdentityM(mModelMatrix, 0);
                bindMatrix();
            }
            else if (chb_dir[i] == 2){
                if (i != col - 1){
                    Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
                    Matrix.translateM(mModelMatrix, 0, snake_m[i * 3], snake_m[i * 3 + 1] - a, snake_m[i * 3 + 2]);
                    bindMatrix();

                    glDrawArrays(GL_TRIANGLES, 34, 36);

                    Matrix.setIdentityM(mModelMatrix, 0);
                }
                Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
                Matrix.translateM(mModelMatrix, 0, snake_m[i * 3], snake_m[i * 3 + 1] + (k_top - k_bottom), snake_m[i * 3 + 2]);
                bindMatrix();

                glDrawArrays(GL_TRIANGLES, 34, 36);

                Matrix.setIdentityM(mModelMatrix, 0);
                bindMatrix();
            }
            else if (chb_dir[i] == 3){
                if (i != col - 1){
                    Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
                    Matrix.translateM(mModelMatrix, 0, snake_m[i * 3], snake_m[i * 3 + 1] + a, snake_m[i * 3 + 2]);
                    bindMatrix();

                    glDrawArrays(GL_TRIANGLES, 34, 36);

                    Matrix.setIdentityM(mModelMatrix, 0);
                }
                Matrix.translateM(mModelMatrix, 0, k_left + a * 7, k_top - a * 8, tmp_z);
                Matrix.translateM(mModelMatrix, 0, snake_m[i * 3], snake_m[i * 3 + 1] - (k_top - k_bottom), snake_m[i * 3 + 2]);
                bindMatrix();

                glDrawArrays(GL_TRIANGLES, 34, 36);

                Matrix.setIdentityM(mModelMatrix, 0);
                bindMatrix();
            }
        }
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

        glUniform4f(new_Color, color_food[0], color_food[1], color_food[2], 1);

        glDrawArrays(GL_TRIANGLES, 34, 30);

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
        glUniform4f(new_Color, color_digits[0], color_digits[1], color_digits[2], 1);

        if (down[0] || dig == 0 || dig == 4 || dig == 5 || dig == 6 || dig == 8 || dig == 9) {
            if (up[0] && depth[number][0] < 0.6f)
                depth[number][0] += big;
            else if (down[0] && depth[number][0] > 0)
                depth[number][0] -= big;

            Matrix.translateM(mModelMatrix, 0, 0, 0, -a * 2);////////////////

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

            Matrix.translateM(mModelMatrix, 0, 0, 0, -a * 2);////////////////////

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

            Matrix.translateM(mModelMatrix, 0, 0, 0, -a * 2);/////////////////

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

            Matrix.translateM(mModelMatrix, 0, 0, 0, -a * 2);////////////

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

            Matrix.translateM(mModelMatrix, 0, 0, 0, -a * 2);/////////

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

            Matrix.translateM(mModelMatrix, 0, 0, 0, -a * 2);////////

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

            Matrix.translateM(mModelMatrix, 0, 0, 0, -a * 2);/////////

            Matrix.rotateM(mRotateMatrix, 0, -90, 0, 1, 0);
            Matrix.translateM(mModelMatrix, 0, k_left + 2 * a + num * 2.3f * a, k_top, -4 * a + a);
            Matrix.scaleM(mScaleMatrix, 0, 0.5f, depth[number][6], 2f);
            bindMatrix();

            glDrawArrays(GL_TRIANGLES, 34, 36);

            Matrix.setIdentityM(mRotateMatrix, 0);
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
        }
        Matrix.setIdentityM(mModelMatrix, 0);

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

        glUniform4f(new_Color, color_floar[0], color_floar[1], color_floar[2], 1);

        Matrix.translateM(mModelMatrix, 0, k_left + 2 * a - 0.04f, k_top, a + 0.1f);
        Matrix.translateM(mModelMatrix, 0, 0, 0, -2 * a);
        Matrix.scaleM(mScaleMatrix, 0, 7, 0.06f, 4.4f);
        bindMatrix();
        glDrawArrays(GL_TRIANGLES, 34, 36);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        bindMatrix();

        glUniform4f(new_Color, 0, 0, 0, 0);
    }

    public static double[] alpha_l = new double[33];
    public static double[] alpha_r = new double[33];
    public static double[] alpha_t = new double[19];
    public static double[] alpha_b = new double[19];

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
        float[] colorDeafault = {color_buttons[0], color_buttons[1], color_buttons[2]};
//        float[] colorAnim = {color_snake[0], color_snake[1], color_snake[2]};
        float[] colorAnim = {color_floar[0], color_floar[1], color_floar[2]};
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

        snake_main(0);///тени
        snake_main(1);///тени
        snake_change();

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
//        draw_setka(0, 0);

        floar_main();
        snake_main(0);
        snake_main(1);
        snake_change();
        wall_main();

        food_main();

        text();

        if (fig == 0) {
            colorButtons(0);
            glDrawArrays(GL_LINES, 82, len_control / 2);
//            glDrawArrays(GL_LINES, 82 + len_control, 2);
            colorButtons(1);
            glDrawArrays(GL_LINES, 82 + len_control / 2, len_control / 2);
        }
        else if (fig == 1){
            colorButtons(0);
//            glDrawArrays(GL_LINES, 82 + len_control, 4);
            glDrawArrays(GL_LINES, 82, len_control / 4);
            colorButtons(1);
            glDrawArrays(GL_LINES, 82 + len_control / 4, len_control / 4);
            colorButtons(2);
            glDrawArrays(GL_LINES, 82 + len_control / 2, len_control / 4);
            colorButtons(3);
            glDrawArrays(GL_LINES, 82 + len_control / 4 * 3, len_control / 4);
        }
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
