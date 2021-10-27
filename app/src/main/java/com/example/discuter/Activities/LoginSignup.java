package com.example.discuter.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.discuter.Fragments.LoginFragment;
import com.example.discuter.R;
import com.example.discuter.Fragments.SignUpFragment;

public class LoginSignup extends AppCompatActivity {
    public static TextView bottomTagLeft,bottomTagRight;
    public static int flag=0;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(flag==2||flag==4)
            replaceFragment(new LoginFragment(),"A new user ./Sign Up now?");
        if(bottomTagRight.getText().toString().equals("Sign Up now?")){
            finishAffinity();
            finish();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        initialiseViews();


        bottomTagRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomTagRight.getText().toString().equals("Login Here . ."))
                {
                    flag=1;
                    replaceFragment(new LoginFragment(),"A new user ./Sign Up now?");
                }
                else if(bottomTagRight.getText().toString().equals("Sign Up now?"))
                {
                    flag=2;
                    replaceFragment(new SignUpFragment(),"Hey Amigo !/Login Here . .");
                }
            }
        });


    }

    public void replaceFragment(Fragment fragment, String text) {
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.login_signup_holder,fragment);
        fragmentTransaction.commit();
        bottomTagLeft.setText(text.split("/")[0]);
        bottomTagRight.setText(text.split("/")[1]);
    }

    private void initialiseViews() {
        bottomTagLeft=findViewById(R.id.bottomTagLeft);
        bottomTagRight=findViewById(R.id.bottomTagRight);

        replaceFragment(new LoginFragment(),"A new user ./Sign Up now?");
    }
}