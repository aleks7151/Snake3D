package com.example.snake3d;

import android.graphics.Path;
import android.util.Log;
import android.widget.TextView;

import javax.crypto.spec.OAEPParameterSpec;

public class Snake {
    Snake(){}

    public static int pick_ch = 0;
    public static String direction = "left";
    public static String direction_0 = "up";
    public static float[] food = new float[3];
    public static boolean[] run_l = new boolean[OpenGLRenderer.alpha_l.length];
    public static boolean[] run_r = new boolean[OpenGLRenderer.alpha_r.length];
    public static boolean[] run_t = new boolean[OpenGLRenderer.alpha_t.length];
    public static boolean[] run_b = new boolean[OpenGLRenderer.alpha_b.length];

    public static boolean[] close_l = new boolean[run_l.length];
    public static boolean[] close_r = new boolean[run_r.length];
    public static boolean[] close_t = new boolean[run_t.length];
    public static boolean[] close_b = new boolean[run_b.length];

    static void move (){
        if (pick_ch == 0){
            direction = direction_0;
            for (int i = 0; i < OpenGLRenderer.col * 3; i++){
                OpenGLRenderer.snake_before_m[i] = OpenGLRenderer.snake_m[i];
            }
            for (int i = 0; i < OpenGLRenderer.col; i++){
                if (!OpenGLRenderer.change_border[i])
                    OpenGLRenderer.chb_dir[i] = -1;
                OpenGLRenderer.change_border[i] = false;
            }
        }
        for (int i = OpenGLRenderer.col * 3 - 1; i > 2; i -= 3){
            float x = OpenGLRenderer.snake_m[i - 2];
            float x1 = OpenGLRenderer.snake_before_m[i - 3 - 2];
            float y = OpenGLRenderer.snake_m[i - 1];
            float y1 = OpenGLRenderer.snake_before_m[i - 3 - 1];

            float xx = OpenGLRenderer.snake_m[i - 3 - 2];
            float yy = OpenGLRenderer.snake_m[i - 3 - 1];

            if (x - xx > OpenGLRenderer.a * 2 && y > yy - 0.001f && y < yy + 0.001f){
                OpenGLRenderer.snake_m[i - 2] = OpenGLRenderer.snake_m[i - 2] + OpenGLRenderer.speed;
            }
            else if (xx - x > OpenGLRenderer.a * 2 && y > yy - 0.001f && y < yy + 0.001f){
                OpenGLRenderer.snake_m[i - 2] = OpenGLRenderer.snake_m[i - 2] - OpenGLRenderer.speed;
            }
            else if (y - yy > OpenGLRenderer.a * 2 && x > xx - 0.001f && x < xx + 0.001f){
                OpenGLRenderer.snake_m[i - 1] = OpenGLRenderer.snake_m[i - 1] + OpenGLRenderer.speed;
            }
            else if (yy - y > OpenGLRenderer.a * 2 && x > xx - 0.001f && x < xx + 0.001f){
                OpenGLRenderer.snake_m[i - 1] = OpenGLRenderer.snake_m[i - 1] - OpenGLRenderer.speed;
            }

            else if (x > x1) {
                OpenGLRenderer.snake_m[i - 2] = OpenGLRenderer.snake_m[i - 2] - OpenGLRenderer.speed;
            }
            else if (x < x1) {
                OpenGLRenderer.snake_m[i - 2] = OpenGLRenderer.snake_m[i - 2] + OpenGLRenderer.speed;
            }
            else if (y > y1) {
                OpenGLRenderer.snake_m[i - 1] = OpenGLRenderer.snake_m[i - 1] - OpenGLRenderer.speed;
            }
            else if (y < y1) {
                OpenGLRenderer.snake_m[i - 1] = OpenGLRenderer.snake_m[i - 1] + OpenGLRenderer.speed;
            }
            border((i + 1) / 3 - 1, OpenGLRenderer.snake_m[i - 2], OpenGLRenderer.snake_m[i - 1], i);
        }

        if (direction == "left"){
            OpenGLRenderer.snake_m[0] = OpenGLRenderer.snake_m[0] - OpenGLRenderer.speed;
        }
        else if (direction == "right"){
            OpenGLRenderer.snake_m[0] = OpenGLRenderer.snake_m[0] + OpenGLRenderer.speed;
        }
        else if (direction == "up"){
            OpenGLRenderer.snake_m[1] = OpenGLRenderer.snake_m[1] + OpenGLRenderer.speed;
        }
        else if (direction == "down"){
            OpenGLRenderer.snake_m[1] = OpenGLRenderer.snake_m[1] - OpenGLRenderer.speed;
        }
        border(0, OpenGLRenderer.snake_m[0], OpenGLRenderer.snake_m[1], 2);

        pick_ch++;
        if (pick_ch == OpenGLRenderer.b){
            for (int i = 3; i < OpenGLRenderer.col * 3; i += 3){
                float x = OpenGLRenderer.snake_m[i];
                float y = OpenGLRenderer.snake_m[i + 1];
                if (x > OpenGLRenderer.snake_m[0] - 0.001f && x < OpenGLRenderer.snake_m[0] + 0.001f &&
                        y > OpenGLRenderer.snake_m[1] - 0.001f && y < OpenGLRenderer.snake_m[1] + 0.001f){
                    OpenGLRenderer.start= false;
                    break;
                }
            }
            if (food[0] > OpenGLRenderer.snake_m[0] - 0.01f && food[0] < OpenGLRenderer.snake_m[0] + 0.01f &&
                    food[1] > OpenGLRenderer.snake_m[1] - 0.01f && food[1] < OpenGLRenderer.snake_m[1] + 0.01f){
                OpenGLRenderer.snake_m[OpenGLRenderer.col * 3] = OpenGLRenderer.snake_before_m[OpenGLRenderer.col * 3 - 1 - 2];
                OpenGLRenderer.snake_m[OpenGLRenderer.col * 3 + 1] = OpenGLRenderer.snake_before_m[OpenGLRenderer.col * 3 - 1 - 1];
                OpenGLRenderer.snake_m[OpenGLRenderer.col * 3 + 2] = 0;
                OpenGLRenderer.anim_eat[OpenGLRenderer.col] = false;
                OpenGLRenderer.change_border[OpenGLRenderer.col] = false;
                OpenGLRenderer.col++;
                OpenGLRenderer.change_digit = true;
                OpenGLRenderer.scale_food = OpenGLRenderer.scale_food0;
                for (int i = 0; i < OpenGLRenderer.col; i++){
                    if (!OpenGLRenderer.anim_eat[i]){
                        OpenGLRenderer.anim_eat[i] = true;
                        break;
                    }
                }
                food();
            }
            pick_ch = 0;
        }
    }
    static void border (int i1, float x, float y, int i){
        if (!OpenGLRenderer.change_border[i1]) {
            float[] pos = {
                    OpenGLRenderer.snake_nach[0] + OpenGLRenderer.snake_m[i - 2],
                    OpenGLRenderer.snake_nach[1] + OpenGLRenderer.snake_m[i - 1],
            };
            if (pos[0] > OpenGLRenderer.k_right - OpenGLRenderer.a + 0.01f) {
                OpenGLRenderer.snake_m[i - 2] = - OpenGLRenderer.a * 8 + OpenGLRenderer.speed;
                OpenGLRenderer.change_border[i1] = true;
                OpenGLRenderer.chb_dir[i1] = 0;
                if (i1 == 0)
                    run_r[Math.round((pos[1] - OpenGLRenderer.k_bottom) / OpenGLRenderer.a)] = true;
                else if (i1 == 1)
                    run_r[Math.round((pos[1] - OpenGLRenderer.k_bottom) / OpenGLRenderer.a)] = false;
                else if (i1 == OpenGLRenderer.col - 1)
                    close_r[Math.round((pos[1] - OpenGLRenderer.k_bottom) / OpenGLRenderer.a)] = true;
            }
            else if (pos[0] < OpenGLRenderer.k_left - 0.01f) {
                OpenGLRenderer.snake_m[i - 2] = OpenGLRenderer.a * 11 - OpenGLRenderer.speed;
                OpenGLRenderer.change_border[i1] = true;
                OpenGLRenderer.chb_dir[i1] = 1;
                if (i1 == 0)
                    run_l[Math.round((pos[1] - OpenGLRenderer.k_bottom) / OpenGLRenderer.a) - 1] = true;
                else if (i1 == 1)
                    run_l[Math.round((pos[1] - OpenGLRenderer.k_bottom) / OpenGLRenderer.a) - 1] = false;
                else if (i1 == OpenGLRenderer.col - 1)
                    close_l[Math.round((pos[1] - OpenGLRenderer.k_bottom) / OpenGLRenderer.a) - 1] = true;
            }
            else if (pos[1] > OpenGLRenderer.k_top + 0.01f){
                OpenGLRenderer.snake_m[i - 1] = - OpenGLRenderer.a * 24 + OpenGLRenderer.speed;
                OpenGLRenderer.change_border[i1] = true;
                OpenGLRenderer.chb_dir[i1] = 2;
                if (i1 == 0)
                    run_t[Math.round((pos[0] - OpenGLRenderer.k_left) / OpenGLRenderer.a)] = true;
                else if (i1 == 1)
                    run_t[Math.round((pos[0] - OpenGLRenderer.k_left) / OpenGLRenderer.a)] = false;
                else if (i1 == OpenGLRenderer.col - 1)
                    close_t[Math.round((pos[0] - OpenGLRenderer.k_left) / OpenGLRenderer.a)] = true;
            }
            else if (pos[1] < OpenGLRenderer.k_bottom + OpenGLRenderer.a - 0.01f){
                OpenGLRenderer.snake_m[i - 1] = OpenGLRenderer.a * 9 - OpenGLRenderer.speed;
                OpenGLRenderer.change_border[i1] = true;
                OpenGLRenderer.chb_dir[i1] = 3;
                if (i1 == 0)
                    run_b[Math.round((pos[0] - OpenGLRenderer.k_left) / OpenGLRenderer.a) + 1] = true;
                else if (i1 == 1)
                    run_b[Math.round((pos[0] - OpenGLRenderer.k_left) / OpenGLRenderer.a) + 1] = false;
                else if (i1 == OpenGLRenderer.col - 1)
                    close_b[Math.round((pos[0] - OpenGLRenderer.k_left) / OpenGLRenderer.a) + 1] = true;
            }
        }
    }
    static void food (){
        food[0] = OpenGLRenderer.a * (int) (-7 + Math.random() * 14);
        food[1] = OpenGLRenderer.a * (int) (-17 + Math.random() * 25);
        food[2] = 0;
        for (int i = 0; i < OpenGLRenderer.col * 3; i += 3){
            if (food[0] > OpenGLRenderer.snake_m[i] - 0.01f && food[0] < OpenGLRenderer.snake_m[i] + 0.01f &&
                    food[1] > OpenGLRenderer.snake_m[i + 1] - 0.01f && food[1] < OpenGLRenderer.snake_m[i + 1] + 0.01f){
                food();
                break;
            }
        }
    }
}

