package com.example.discuter.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.biometric.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discuter.Activities.ActivityTab;
import com.example.discuter.Activities.LoginSignup;
import com.example.discuter.Activities.ProfileAndStatusActivity;
import com.example.discuter.ModelClass.User;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.example.discuter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private View loginView;
    private TextView welcome,back,forgotPassword;
    private Animation translateX,translateNegX;
    private FirebaseAuth authenticate;
    private TextInputEditText userEmailLogin, userPasswordLogin;
    private Button loginButton;
    private ProgressBar load;
    private String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
    private Dialog otpDialog,savePasswordDialog;
    private SharedPreferences sharedPreferences;
    private Executor executor;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        loginView= inflater.inflate(R.layout.fragment_login, container, false);
        return loginView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseElements();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email= userEmailLogin.getText().toString();
                String password= userPasswordLogin.getText().toString();

                if(CheckNetworkAvailibility.isConnectionAvailable(loginView.getContext()))
                {
                    if (validate(email,password)) {
                        load.setVisibility(View.VISIBLE);
                        authenticate.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    checkAccountType();
                                } else {
                                    load.setVisibility(View.GONE);
                                    Toast.makeText(loginView.getContext(), "Incorrect Credentials", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
                else
                {
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

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(loginView.getContext(), "forgotPassword", Toast.LENGTH_SHORT).show();
                LoginSignup.flag=4;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.login_signup_holder,new ForgotPasswordFragment()).commit();

                LoginSignup.bottomTagLeft.setText("Hey Amigo !");
                LoginSignup.bottomTagRight.setText("Login Here . .");
            }
        });

    }

    private void checkAccountType() {
        DatabaseReference USERNODE = FirebaseDatabase.getInstance().getReference().child("User").child(authenticate.getUid());
        USERNODE.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    User user=snapshot.getValue(User.class);
                    if(user.getStatus().equals("")&&authenticate.getCurrentUser().isEmailVerified()) {
                        Intent intent=new Intent(loginView.getContext(), ProfileAndStatusActivity.class);
                        intent.putExtra("UserData",user);
                        load.setVisibility(View.GONE);
                        startActivity(intent);
                    }
                    else if(!authenticate.getCurrentUser().isEmailVerified())
                    {
                        load.setVisibility(View.GONE);
                            showOTPDialog();
                    }
                    else if(!user.getProfileLink().equals("")&&authenticate.getCurrentUser().isEmailVerified())
                    {
                        //Toast.makeText(loginView.getContext(), "old user", Toast.LENGTH_SHORT).show();

                        load.setVisibility(View.GONE);
                        sharedPreferences = getActivity().getSharedPreferences("UserAuthenticationData", Context.MODE_PRIVATE);
                        String saved_email=sharedPreferences.getString("Email","");
                        String saved_password=sharedPreferences.getString("Password","");
                        if(user.getFingerprint_auth_enabled()){
                            BiometricPrompt biometricPrompt = new BiometricPrompt(
                                    (FragmentActivity) loginView.getContext(),
                                    executor,
                                    new BiometricPrompt.AuthenticationCallback() {
                                        @Override
                                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                                            super.onAuthenticationError(errorCode, errString);
                                            Toast.makeText(loginView.getContext(), "Error Ocurred ! Try again .", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                                            super.onAuthenticationSucceeded(result);
                                            if (saved_email.equals("") && saved_password.equals("") || !saved_email.equals(userEmailLogin.getText().toString())) {
                                                showSavePasswordDialog();
                                            } else
                                                startActivity(new Intent(loginView.getContext(), ActivityTab.class));
                                        }

                                        @Override
                                        public void onAuthenticationFailed() {
                                            super.onAuthenticationFailed();
                                            Toast.makeText(loginView.getContext(), "Fingerprint authentication failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("LOGIN")
                                    .setSubtitle("Fingerprint Required ")
                                    .setDescription("Scan  your fingerprint to continue")
                                    .setNegativeButtonText("cancel")
                                    .build();

                            biometricPrompt.authenticate(promptInfo);
                        }
                        else
                        {
                            if (saved_email.equals("") && saved_password.equals("") || !saved_email.equals(userEmailLogin.getText().toString())) {
                                showSavePasswordDialog();
                            } else
                                startActivity(new Intent(loginView.getContext(), ActivityTab.class));
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showSavePasswordDialog() {
        savePasswordDialog.setContentView(R.layout.save_password_dialog);
        savePasswordDialog.setCancelable(false);
        otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView skipSavePassword=savePasswordDialog.findViewById(R.id.skipSavePassword);
        TextView savePassword=savePasswordDialog.findViewById(R.id.savePassword);

        skipSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(loginView.getContext(),ActivityTab.class));
            }
        });

        savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getActivity().getSharedPreferences("UserAuthenticationData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("Email", userEmailLogin.getText().toString().trim());
                editor.putString("Password", userPasswordLogin.getText().toString().trim());

                editor.apply();

                startActivity(new Intent(loginView.getContext(),ActivityTab.class));
                }
        });
        savePasswordDialog.show();
    }

    private void showOTPDialog()
    {
        otpDialog.setContentView(R.layout.otp_dialog_layout);
        otpDialog.setCancelable(false);
        otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView Ok = otpDialog.findViewById(R.id.VerificationOk);
        Button getLink = otpDialog.findViewById(R.id.getLink);
        TextView linkMessage=otpDialog.findViewById(R.id.linkMessage);
        linkMessage.setText("You have not verified the email ");

        getLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate.getCurrentUser().sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(loginView.getContext(),"Mail has ben sent to : "+authenticate.getCurrentUser().getEmail().toString(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(loginView.getContext(),"Try Again !!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               otpDialog.dismiss();
            }
        });
        otpDialog.show();
    }


    private boolean validate(String email,String password) {
        if(email.trim().equals(""))
        {
            Toast.makeText(loginView.getContext(), "Kindly enter the email to proceed", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(password.trim().equals(""))
        {
            Toast.makeText(loginView.getContext(), "Kindly enter the password to proceed", Toast.LENGTH_SHORT).show();
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    private void initialiseElements() {
        welcome=loginView.findViewById(R.id.welcome);
        back=loginView.findViewById(R.id.back);
        translateNegX= AnimationUtils.loadAnimation(loginView.getContext(),R.anim.translate_neg_x);
        translateX=AnimationUtils.loadAnimation(loginView.getContext(),R.anim.translate_x);
        translateNegX.setStartOffset(100);
        translateX.setStartOffset(100);
        welcome.startAnimation(translateX);
        back.startAnimation(translateNegX);

        userEmailLogin =loginView.findViewById(R.id.userEmailLogin);
        userPasswordLogin =loginView.findViewById(R.id.userPasswordLogin);
        loginButton=loginView.findViewById(R.id.loginButton);
        authenticate=FirebaseAuth.getInstance();

        load=loginView.findViewById(R.id.load2);
        forgotPassword=loginView.findViewById(R.id.forgotPassword);

        otpDialog=new Dialog(loginView.getContext());
        savePasswordDialog=new Dialog(loginView.getContext());
        executor= ContextCompat.getMainExecutor(loginView.getContext());
    }
}