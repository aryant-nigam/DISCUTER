package com.example.discuter.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;

import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.discuter.ModelClass.User;
import com.example.discuter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class SplashScreenActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences sharedPreferences=getSharedPreferences("UserAuthenticationData",MODE_PRIVATE);
        String saved_email=sharedPreferences.getString("Email","abc@123");
        String saved_password=sharedPreferences.getString("Password","12345");
        auth=FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(saved_email,saved_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseDatabase.getInstance().getReference().child("User").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            currentUser=snapshot.getValue(User.class);
                            if(currentUser.getFingerprint_auth_enabled())
                            {
                                Executor executor= ContextCompat.getMainExecutor(getApplicationContext()) ;
                                BiometricPrompt biometricPrompt=new BiometricPrompt(
                                        SplashScreenActivity.this,
                                        executor,
                                    new BiometricPrompt.AuthenticationCallback(){
                                    @Override
                                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                                        super.onAuthenticationError(errorCode, errString);
                                        finish();
                                        //Toast.makeText(getApplicationContext(), "Error Ocurred ! Try again ."+errString, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                                        super.onAuthenticationSucceeded(result);
                                        Intent intent = new Intent(getApplicationContext(), ActivityTab.class);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onAuthenticationFailed() {
                                        super.onAuthenticationFailed();
                                        Toast.makeText(getApplicationContext(), "Fingerprint authentication failed", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                BiometricPrompt.PromptInfo promptInfo=new BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("LOGIN")
                                        .setSubtitle("Fingerprint Required")
                                        .setDescription("Scan  your fingerprint to continue")
                                        .setNegativeButtonText("cancel")
                                        .build();

                                biometricPrompt.authenticate(promptInfo);
                            }
                            else {
                                showSplashScreen(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else
                {
                    showSplashScreen(false);
                }
            }
        });
    }
    private void showSplashScreen(Boolean directToHomeScreen)
    {
        new CountDownTimer(3200, 3200) {

            public void onTick(long millisUntilFinished) { }

            public void onFinish() {
                if(directToHomeScreen)
                {
                    Intent intent = new Intent(getApplicationContext(), ActivityTab.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Intent intent = new Intent(getApplicationContext(), LoginSignup.class);
                    startActivity(intent);
                    finish();
                }

            }
        }.start();
    }
}