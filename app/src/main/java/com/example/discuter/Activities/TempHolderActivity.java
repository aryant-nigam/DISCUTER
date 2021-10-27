package com.example.discuter.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.discuter.Fragments.ForgotPasswordFragment;
import com.example.discuter.R;

public class TempHolderActivity extends AppCompatActivity {

    private FrameLayout change_password_holder;
    private Animation popup;
    private RelativeLayout rl_tempholder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_holder);
        popup= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.popup_topright);
        change_password_holder=findViewById(R.id.change_password_holder);
        rl_tempholder=findViewById(R.id.rl_tempholder);

        popup.setDuration(800);
        change_password_holder.startAnimation(popup);
        getSupportFragmentManager().beginTransaction().replace(R.id.change_password_holder,new ForgotPasswordFragment()).commit();

    }
}