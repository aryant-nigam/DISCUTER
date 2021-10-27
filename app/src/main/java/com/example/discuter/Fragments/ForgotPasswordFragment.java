package com.example.discuter.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discuter.R;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class ForgotPasswordFragment extends Fragment {

    private View ForgotPasswordView;
    private TextView forget ,password;
    private TextInputEditText emailForgetPassword, oldForgetPassword;
    private Button changePassword;
    private String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
    private Animation translateNegx,translateX,popup;
    private FirebaseAuth auth;
    private ProgressBar load;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ForgotPasswordView= inflater.inflate(R.layout.fragment_forgot_password, container, false);
        return ForgotPasswordView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseElements();

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckNetworkAvailibility.isConnectionAvailable(ForgotPasswordView.getContext()))
                {
                    load.setVisibility(View.VISIBLE);
                    if (validate()) {
                        auth.createUserWithEmailAndPassword(emailForgetPassword.getText().toString(),oldForgetPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(ForgotPasswordView.getContext(), "Account does not exists !", Toast.LENGTH_SHORT).show();
                                    auth.getCurrentUser().delete();
                                    load.setVisibility(View.GONE);
                                    emailForgetPassword.setText("");
                                    oldForgetPassword.setText("");
                                }
                                else
                                {
                                    auth.sendPasswordResetEmail(emailForgetPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            load.setVisibility(View.GONE);
                                            Toast.makeText(ForgotPasswordView.getContext(), "Password reset mail was send to : "+emailForgetPassword.getText().toString(), Toast.LENGTH_SHORT).show();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.login_signup_holder,new LoginFragment());
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
                else
                {
                    load.setVisibility(View.GONE);
                    Snackbar snackbar = Snackbar.make(view, "NETWORK UNAVAILABLE", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    snackbar.setBackgroundTint(Color.parseColor("#93999C"));
                    snackbar.setTextColor(Color.parseColor("#222D32"));
                    snackbar.setActionTextColor(Color.parseColor("#171F24"));
                    snackbar.show();
                }
            }
        });

    }

    private boolean validate() {
        if(emailForgetPassword.getText().toString().trim().equals("")|| !Pattern.matches(regex,emailForgetPassword.getText().toString()))
        {
            emailForgetPassword.setText("");
            Toast.makeText(ForgotPasswordView.getContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(oldForgetPassword.getText().toString().trim().equals(""))
        {
            oldForgetPassword.setText("");
            Toast.makeText(ForgotPasswordView.getContext(), "Kindly guess the old password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initialiseElements() {
        forget=ForgotPasswordView.findViewById(R.id.change);
        password=ForgotPasswordView.findViewById(R.id.password);
        changePassword=ForgotPasswordView.findViewById(R.id.changePassword);

        translateNegx= AnimationUtils.loadAnimation(ForgotPasswordView.getContext(),R.anim.translate_neg_x);
        translateX=AnimationUtils.loadAnimation(ForgotPasswordView.getContext(),R.anim.translate_x);
        forget.startAnimation(translateX);
        password.startAnimation(translateNegx);

        emailForgetPassword=ForgotPasswordView.findViewById(R.id.userEmailForgotPassword);
        oldForgetPassword=ForgotPasswordView.findViewById(R.id.userOldPasswordForgotPassword);
        changePassword=ForgotPasswordView.findViewById(R.id.changePassword);
        load=ForgotPasswordView.findViewById(R.id.load4);

        auth=FirebaseAuth.getInstance();

    }
}