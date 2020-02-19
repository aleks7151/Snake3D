package com.example.snake3d;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        checkGraphic = findViewById(R.id.checkGraphic);
        graphic = findViewById(R.id.graphic);
        language = findViewById(R.id.language);
        checkLanguage0 = findViewById(R.id.russian);
        checkLanguage1 = findViewById(R.id.english);
        sound = findViewById(R.id.sound);
        checkSound = findViewById(R.id.checkSound);
    }

    Button graphic, language, sound;
    CheckBox checkGraphic, checkLanguage0, checkLanguage1;
    SeekBar checkSound;
    boolean lev0 = false, lev1 = false, lev2 = false;
    boolean change0 = false, change1 = false, change2;

    public void graphic (View v)
    {
        if (!lev0 && !change0 && !change1 && !change2) {
            if (lev1)
                language(language);
            if (lev2)
                sound(sound);
            change0 = true;
            ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredWidth(), graphic.getWidth() / 2);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = graphic.getLayoutParams();
                    layoutParams.width = val;
                    graphic.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(200);
            anim.start();

            ValueAnimator anim2 = ValueAnimator.ofFloat(1, 1.2f, 1);
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    graphic.setScaleY(val);
                    graphic.setScaleX(1 / val);
                }
            });
            anim2.setStartDelay(120);
            anim2.setDuration(200);
            anim2.start();

            ValueAnimator anim1 = ValueAnimator.ofFloat(0, 1);
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    checkGraphic.setAlpha(val);
                }
            });
            anim1.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    change0 = false;
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim1.setDuration(360);
            anim1.start();

            lev0 = true;
        }
        else if (!change0 && !change1 && !change2){
            change0 = true;
            ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredWidth(), graphic.getWidth() * 2);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = graphic.getLayoutParams();
                    layoutParams.width = val;
                    graphic.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(200);
            anim.start();

            ValueAnimator anim2 = ValueAnimator.ofFloat(1, 0.9f, 1);
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    graphic.setScaleY(val);
                    graphic.setScaleX(1 / val);
                }
            });
            anim2.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    change0 = false;
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim2.setStartDelay(120);
            anim2.setDuration(200);
            anim2.start();

            ValueAnimator anim1 = ValueAnimator.ofFloat(1, 0);
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    checkGraphic.setAlpha(val);
                }
            });
            anim1.setDuration(100);
            anim1.start();

            lev0 = false;
        }
    }

    public void language (View v)
    {
        if (!lev1 && !change1 && !change0 && !change2) {
            if (lev0)
                graphic(graphic);
            if (lev2)
                sound(sound);
            change1 = true;
            ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredWidth(), language.getWidth() / 2);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = language.getLayoutParams();
                    layoutParams.width = val;
                    language.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(200);
            anim.start();

            ValueAnimator anim2 = ValueAnimator.ofFloat(1, 1.2f, 1);
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    language.setScaleY(val);
                    language.setScaleX(1 / val);
                }
            });
            anim2.setStartDelay(120);
            anim2.setDuration(200);
            anim2.start();

            ValueAnimator anim1 = ValueAnimator.ofFloat(0, 1);
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    checkLanguage0.setAlpha(val);
                    checkLanguage1.setAlpha(val);
                }
            });
            anim1.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    change1 = false;
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim1.setDuration(360);
            anim1.start();

            lev1 = true;
        }
        else if (!change1 && !change0 && !change2){
            change1 = true;
            ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredWidth(), language.getWidth() * 2);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = language.getLayoutParams();
                    layoutParams.width = val;
                    language.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(200);
            anim.start();

            ValueAnimator anim2 = ValueAnimator.ofFloat(1, 0.9f, 1);
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    language.setScaleY(val);
                    language.setScaleX(1 / val);
                }
            });
            anim2.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    change1 = false;
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim2.setStartDelay(120);
            anim2.setDuration(200);
            anim2.start();

            ValueAnimator anim1 = ValueAnimator.ofFloat(1, 0);
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    checkLanguage0.setAlpha(val);
                    checkLanguage1.setAlpha(val);
                }
            });
            anim1.setDuration(100);
            anim1.start();

            lev1 = false;
        }
    }

    public void sound(View v)
    {
        if (!lev2 && !change0 && !change1 && !change2) {
            if (lev1)
                language(language);
            if (lev0)
                graphic(graphic);
            change2 = true;
            ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredWidth(), sound.getWidth() / 2);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = sound.getLayoutParams();
                    layoutParams.width = val;
                    sound.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(200);
            anim.start();

            ValueAnimator anim2 = ValueAnimator.ofFloat(1, 1.2f, 1);
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    sound.setScaleY(val);
                    sound.setScaleX(1 / val);
                }
            });
            anim2.setStartDelay(120);
            anim2.setDuration(200);
            anim2.start();

            ValueAnimator anim1 = ValueAnimator.ofFloat(0, 1);
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    checkSound.setAlpha(val);
                }
            });
            anim1.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    change2 = false;
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim1.setDuration(360);
            anim1.start();

            lev2 = true;
        }
        else if (!change0 && !change1 && !change2){
            change2 = true;
            ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredWidth(), sound.getWidth() * 2);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = sound.getLayoutParams();
                    layoutParams.width = val;
                    sound.setLayoutParams(layoutParams);
                }
            });
            anim.setDuration(200);
            anim.start();

            ValueAnimator anim2 = ValueAnimator.ofFloat(1, 0.9f, 1);
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    sound.setScaleY(val);
                    sound.setScaleX(1 / val);
                }
            });
            anim2.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    change2 = false;
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            anim2.setStartDelay(120);
            anim2.setDuration(200);
            anim2.start();

            ValueAnimator anim1 = ValueAnimator.ofFloat(1, 0);
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    checkSound.setAlpha(val);
                }
            });
            anim1.setDuration(100);
            anim1.start();

            lev2 = false;
        }
    }

    public void control(View v)
    {
        Intent intent = new Intent(".ControlActivity");
        startActivity(intent);
    }

    public void russian(View v)
    {
        if (checkLanguage0.isChecked() && checkLanguage1.isChecked())
            checkLanguage1.setChecked(false);
        else if (!checkLanguage0.isChecked() && !checkLanguage1.isChecked())
            checkLanguage1.setChecked(true);
    }

    public void english(View v)
    {
        if (checkLanguage0.isChecked() && checkLanguage1.isChecked())
            checkLanguage0.setChecked(false);
        else if (!checkLanguage0.isChecked() && !checkLanguage1.isChecked())
            checkLanguage0.setChecked(true);
    }
}
