package com.example.discuter.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.HashSet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.discuter.Activities.LoginSignup;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.example.discuter.R;
import com.example.discuter.ModelClass.User;
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

import java.util.Set;
import java.util.regex.Pattern;

public class SignUpFragment extends Fragment {
    private View signupView;
    private TextView sign,up;
    private Animation translateX,translateNegX;
    private TextInputEditText usernameSignUp,userEmailSignUp,userPasswordSignUp,userConfirmPasswordSignUp;
    private Button nextButton;
    private Dialog otpDialog,passwordDialog;
    private String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
    private ProgressBar load;
    FirebaseAuth auth;
    Boolean verificationStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        signupView=inflater.inflate(R.layout.fragment_sign_up, container, false);
        //Toast.makeText(signupView.getContext(),"Hi",Toast.LENGTH_LONG);
        return signupView;
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseElements();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckNetworkAvailibility.isConnectionAvailable(signupView.getContext()))
                {
                    if (validate()) {
                        load.setVisibility(View.VISIBLE);
                        checkUsernameAvailibility(usernameSignUp.getText().toString().trim());
                       // createUser();
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
                    load.setVisibility(View.GONE);
                }
            }
        });

    }


    private void createUser() {
        if (CheckNetworkAvailibility.isConnectionAvailable(signupView.getContext()))
        {
            auth=FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword
                    (
                            userEmailSignUp.getText().toString(),
                            userPasswordSignUp.getText().toString()
                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        DatabaseReference USERNODE = FirebaseDatabase.getInstance().getReference().child("User").child(auth.getUid());
                        User user = new User(
                                usernameSignUp.getText().toString(),
                                userEmailSignUp.getText().toString(),
                                auth.getUid(),
                                "",
                                "",
                                null,
                                "",
                                false
                        );
                        USERNODE.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                load.setVisibility(View.GONE);
                                showOTPDialog();
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        auth.getCurrentUser().delete();
                                        load.setVisibility(View.GONE);
                                        Toast.makeText(signupView.getContext(), "Failed Try Again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        load.setVisibility(View.GONE);
                        Snackbar snackbar=Snackbar.make(getView(),"    Account Already Exists ! !",Snackbar.LENGTH_LONG).setAction("LOGIN", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LoginSignup.bottomTagLeft.setText("A new user .");
                                LoginSignup.bottomTagRight.setText("Sign Up now?");

                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.login_signup_holder, new LoginFragment()).commit();
                            }
                        });
                        snackbar.setActionTextColor(Color.parseColor("#93999C"));
                        snackbar.setTextColor(Color.parseColor("#BEC2C3"));
                        snackbar.setBackgroundTint(Color.parseColor("#222D32"));
                        load.setVisibility(View.GONE);
                        snackbar.show();
                    }
                }
            });
        }
        else
        {
            Snackbar snackbar = Snackbar.make(getView(), "NETWORK UNAVAILABLE", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            snackbar.setBackgroundTint(Color.parseColor("#93999C"));
            snackbar.setTextColor(Color.parseColor("#222D32"));
            snackbar.setActionTextColor(Color.parseColor("#171F24"));
            snackbar.show();
            load.setVisibility(View.GONE);
        }
    }



    private void initialiseElements() {
        Toast.makeText(signupView.getContext(),"Hi",Toast.LENGTH_LONG);

        sign=signupView.findViewById(R.id.sign);
        up=signupView.findViewById(R.id.up);
        translateNegX= AnimationUtils.loadAnimation(signupView.getContext(),R.anim.translate_neg_x);
        translateX=AnimationUtils.loadAnimation(signupView.getContext(),R.anim.translate_x);
        sign.startAnimation(translateX);
        up.startAnimation(translateNegX);

        usernameSignUp =signupView.findViewById(R.id.usernameSignUp);
        userEmailSignUp=signupView.findViewById(R.id.emailSignup);
        userPasswordSignUp=signupView.findViewById(R.id.passwordSignup);
        userConfirmPasswordSignUp=signupView.findViewById(R.id.confirmPasswordSignup);
        nextButton =signupView.findViewById(R.id.nextButton);

        passwordDialog=new Dialog((signupView.getContext()));
        otpDialog=new Dialog(signupView.getContext());

        load=signupView.findViewById(R.id.load3);

    }

    private void showOTPDialog()
    {
        otpDialog.setContentView(R.layout.otp_dialog_layout);
        otpDialog.setCancelable(false);
        otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView Ok = otpDialog.findViewById(R.id.VerificationOk);
        Button getLink = otpDialog.findViewById(R.id.getLink);
        TextView linkMessage=otpDialog.findViewById(R.id.linkMessage);
        linkMessage.setText("Kindly verify your email to login");

        getLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.getCurrentUser().sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void unused) {
                        Toast.makeText(signupView.getContext(),"Mail has ben sent to : "+auth.getCurrentUser().getEmail().toString(), Toast.LENGTH_SHORT).show();
                   }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(signupView.getContext(),"Try Again !!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otpDialog.dismiss();
                startActivity(new Intent(signupView.getContext(),LoginSignup.class));
            }
        });
        otpDialog.show();
    }

    private boolean validate() {
        if(usernameSignUp.getText().toString().trim().equals("")||hasSpecialCharacters(usernameSignUp.getText().toString().trim()))
        {
            Toast.makeText(signupView.getContext(),"Invalid Username : only Characters, Numbers and Period allowed ",Toast.LENGTH_LONG).show();
            return false;
        }
        else if(usernameSignUp.getText().toString().length()<6)
        {
            Toast.makeText(signupView.getContext(),"Invalid Username : username must be 6 or more charecters long",Toast.LENGTH_LONG).show();
            return false;
        }
        else if(userEmailSignUp.getText().toString().trim().equals("")
                ||!userEmailSignUp.getText().toString().trim().
                  substring(userEmailSignUp.getText().toString().trim().length()-9,userEmailSignUp.getText().toString().trim().length()).equals("gmail.com")
                ||!Pattern.matches(regex,userEmailSignUp.getText().toString().trim()))
        {
            Toast.makeText(signupView.getContext(),"Invalid Email",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(userPasswordSignUp.getText().toString().trim().equals(""))
        {
            Toast.makeText(signupView.getContext(),"Set the password to proceed",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!isStrong(userPasswordSignUp.getText().toString().trim()))
        {
            passwordDialog.setContentView(R.layout.password_info_dialog);
            passwordDialog.setCancelable(false);
            passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView ok=passwordDialog.findViewById(R.id.ok1);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passwordDialog.dismiss();
                }
            });

            passwordDialog.show();
            userPasswordSignUp.setText("");
            userConfirmPasswordSignUp.setText("");
            return false;
        }
        else if(!(userPasswordSignUp.getText().toString().equals(userConfirmPasswordSignUp.getText().toString())))
        {
            Toast.makeText(signupView.getContext(),"Passwords Do Not Match",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkUsernameAvailibility(String username) {
        DatabaseReference USERS=FirebaseDatabase.getInstance().getReference().child("User");
        USERS.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user ;
                for(DataSnapshot u:snapshot.getChildren())
                {
                    user=u.getValue(User.class);
                    if(user!=null &&user.getUsername().equals(username))
                    {
                        Toast.makeText(signupView.getContext(), "Username unavailable", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                createUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    };

    private boolean isStrong(String password) {
        int n = password.length();
        boolean hasLower = false, hasUpper = false,
                hasDigit = false, specialChar = false;
        Set<Character> set = new HashSet<Character>(
                Arrays.asList('!', '@', '#', '$', '%', '^', '&',
                        '*', '-', '+'));
        for (char i : password.toCharArray())
        {
            if (Character.isLowerCase(i))
                hasLower = true;
            if (Character.isUpperCase(i))
                hasUpper = true;
            if (Character.isDigit(i))
                hasDigit = true;
            if (set.contains(i))
                specialChar = true;
        }
        if (hasDigit && hasLower && hasUpper && specialChar && (n >= 8))
            return true;

        return false;
    }

    private boolean hasSpecialCharacters(String username) {
        for(int i=0;i<username.length();i++)
        {
            if(!(username.charAt(i)>=48&&username.charAt(i)<=57||username.charAt(i)>=97&&username.charAt(i)<=122||username.charAt(i)>=65&&username.charAt(i)<=90||username.charAt(i)=='.'||username.charAt(i)==' '))
            {
                return true;
            }
        }
        return false;


    }


}


