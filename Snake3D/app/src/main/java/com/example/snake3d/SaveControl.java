package com.example.snake3d;

import android.content.Context;
import android.opengl.Matrix;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;

public class SaveControl {

    private float a;
    private Context context;

    public SaveControl(float a, Context context)
    {
        this.context = context;
        this.a = a;
    }

    float[] controlFirst;
    float[] controlSecond;
    public float[] getControlFirst(){
        float[] control = {
                //правая
                0, 0, 0,
                0, -3 * a, 0,

                //верх
                0, 0, 0,
                -a + a / 4, 0, 0,

                //низ
                -a + a / 4, - 2 * a - a, 0,
                0, - 2 * a - a, 0,

                //верх лево
                -a + a / 4, 0, 0,
                -a - a / 2 + a / 4, -a - a / 4, 0,

                //низ лево
                -a - a / 2 + a / 4, -a - a / 2 - a / 4, 0,
                -a + a / 4, - 3 * a, 0,

                //лево середина
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
        controlFirst = new float[control.length];
        for (int i = 0; i < control.length; i++)
            controlFirst[i] = control[i];
        return control;
    }

    public float[] getControlSecond(){
        float[] control = {
                //нижняя
                -a - a / 2, 0, 0,
                a + a / 2, 0, 0,

                //левая
                -a - a / 2, 3 * a, 0,
                -a - a / 2, 0, 0,

                //правая (право верх)
                a + a / 2, 3 * a, 0,            //используется в шейдере
                a + a / 2, 0, 0,

                //верхняя (лево верх)
                a + a / 2, 3 * a, 0,            //используется в шейдере
                -a - a / 2, 3 * a, 0,


                ///////Отрисовка этой херни сверху 2.0
                a + a / 2, 3 * a, 0,
                -a - a / 2, 0, 0,
                a + a / 2, 0, 0,

                a + a / 2, 3 * a, 0,
                -a - a / 2, 3 * a, 0,
                -a - a / 2, 0, 0,
        };
        controlSecond = new float[control.length];
        for (int i = 0; i < control.length; i++)
            controlSecond[i] = control[i];
        return control;
    }

    private float[] m0Matrix = new float[16];
    private float[] m1Model = new float[16];
    private float[] m2Rotate = new float[16];
    private float[] m3Scale = new float[16];
    private void mvBindMatrix()
    {
        Matrix.setIdentityM(m0Matrix, 0);
        Matrix.multiplyMM(m0Matrix, 0, m1Model, 0, m2Rotate, 0);
        Matrix.multiplyMM(m0Matrix, 0, m0Matrix, 0, m3Scale, 0);
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

        for (int j = 0; j < 2; j++) {
            for (int i = 8 * 3 + j * 3; i < 3 + 8 * 3 + j * 3; i++) {
                tmp[i - (8 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (8 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 1, 0, 0, oos);//лево низ
        }

        for (int j = 0; j < 2; j++) {
            for (int i = 6 * 3 + j * 3; i < 3 + 6 * 3 + j * 3; i++) {
                tmp[i - (6 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (6 * 3 + j * 3)] = controlFirst[i];
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

        for (int j = 0; j < 2; j++) {
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

        for (int j = 0; j < 2; j++) {
            for (int i = 10 * 3 + j * 3; i < 3 + 10 * 3 + j * 3; i++) {
                tmp[i - (10 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (10 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 0, 0, 0, oos);//лево середина
        }
        Matrix.translateM(m2Rotate, 0, (scale - 1) * a + ((a / 2) * (scale - 1)) / 2, 0, 0);//(scaleX - 1) * a + ((a / 2) * (scaleX - 1)) / 2

        Matrix.setIdentityM(m3Scale, 0);
        Matrix.scaleM(m3Scale, 0, scale - 1f / 3f * (scale - 1), 1, 1);
        Matrix.translateM(m2Rotate, 0, -a / 4 * (scale - 1), 0, 0);
        mvBindMatrix();

        for (int j = 0; j < 2; j++) {
            for (int i = 2 * 3 + j * 3; i < 3 + 2 * 3 + j * 3; i++) {
                tmp[i - (2 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (2 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 0, 0, 0, oos);//верхняя
        }

        Matrix.translateM(m2Rotate, 0, 0, -(scale - 1) * 3 * a, 0);
        mvBindMatrix();

        for (int j = 0; j < 2; j++) {
            for (int i = 4 * 3 + j * 3; i < 3 + 4 * 3 + j * 3; i++) {
                tmp[i - (4 * 3 + j * 3)] = controlFirst[i];
                tmp_start[i - (4 * 3 + j * 3)] = controlFirst[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, check, scale, 0, 0, 0, oos);//нижняя
        }
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
        if (chooseC == 1 && (l_top == 1 || l_down == 1)){//право верх
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
        mvBindMatrix();

        Matrix.scaleM(m3Scale, 0, 1, scale, 1);

        Matrix.translateM(m2Rotate, 0, -1.5f * a * (scale - 1), 0, 0);
        mvBindMatrix();

        for (int j = 0; j < 2; j++) {
            for (int i = 2 * 3 + j * 3; i < 3 /*length_tmp*/ + 2 * 3 + j * 3; i++) {
                tmp[i - (2 * 3 + j * 3)] = controlSecond[i];
                tmp_start[i - (2 * 3 + j * 3)] = controlSecond[i];
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

        for (int j = 0; j < 2; j++) {
            for (int i = 4 * 3 + j * 3; i < 3 /*length_tmp*/ + 4 * 3 + j * 3; i++) {
                tmp[i - (4 * 3 + j * 3)] = controlSecond[i];
                tmp_start[i - (4 * 3 + j * 3)] = controlSecond[i];
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

        for (int j = 0; j < 2; j++) {
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

        for (int j = 0; j < 2; j++) {
            for (int i = 6 * 3 + j * 3; i < 3 /*length_tmp*/ + 6 * 3 + j * 3; i++) {
                tmp[i - (6 * 3 + j * 3)] = controlSecond[i];
                tmp_start[i - (6 * 3 + j * 3)] = controlSecond[i];
            }
            tmp[3] = 1;
            tmp_start[3] = 1;
            Matrix.multiplyMV(tmp, 0, m0Matrix, 0, tmp, 0);
            shader_func(tmp_start, tmp, num, scale, 1, 0, 1, oos);//лево верх
        }
    }

    private float transFigX;
    private float transFigY;
    private float scale;
    private float tmp_z;
    public void recordControl(boolean fig0, float transFigX, float transFigY, float min, float tmp_z, float scale) {
        this.transFigX = transFigX;
        this.transFigY = transFigY;
        this.scale = scale;
        this.tmp_z = tmp_z;
        String filename = "control.snake";
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            if (fig0) {
                oos.write(String.format("0%.10f|%.10f)", transFigX, transFigY - min).getBytes());
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
}
