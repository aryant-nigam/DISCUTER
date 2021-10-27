package com.example.discuter.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.example.discuter.ModelClass.User;
import com.example.discuter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAndStatusActivity extends AppCompatActivity {

    private User user;
    private CircleImageView profile;
    private TextInputEditText status;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private Button signupButton;
    private FirebaseAuth authentication;
    private Uri imageURI;
    private String password;
    private ProgressDialog progressDialog;
    private TextView progressMessage,usernameProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_and_status);
        initialiseViews();

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent=new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"Pick your Profile Image"),10);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckNetworkAvailibility.isConnectionAvailable(getApplicationContext()))
                {
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.custom_progress_dialog_layout);
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressMessage=progressDialog.findViewById(R.id.progressMessage);
                    progressMessage.setText("Setting up your account");
                    setStatus();

                    DatabaseReference USERNODE = FirebaseDatabase.getInstance().getReference().child("User").child(authentication.getUid());
                    StorageReference STORAGE_REFERENCE = FirebaseStorage.getInstance().getReference().child("Profile Images").child(authentication.getUid());
                    if (imageURI != null) {
                        STORAGE_REFERENCE.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    STORAGE_REFERENCE.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            user.setProfileLink(uri.toString());

                                            USERNODE.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(ProfileAndStatusActivity.this, "Account created successfully !", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(getApplicationContext(), ActivityTab.class));
                                                    } else
                                                        Toast.makeText(ProfileAndStatusActivity.this, "Account couldn't be created !", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        user.setProfileLink("https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/DefaultProfile.png?alt=media&token=aadb4dc4-3f12-432f-9bac-c674028e706d");
                        user.setUid(authentication.getUid());

                        USERNODE.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileAndStatusActivity.this, "Account created successfully !", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), ActivityTab.class));
                                } else
                                    Toast.makeText(ProfileAndStatusActivity.this, "Account couldn't be created !", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else {
                    Snackbar snackbar = Snackbar.make(v, "NETWORK UNAVAILABLE", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    snackbar.setBackgroundTint(Color.parseColor("#93999C"));
                    snackbar.setTextColor(Color.parseColor("#171F24"));
                    snackbar.setActionTextColor(Color.parseColor("#171F24"));
                    snackbar.show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==10)
        {
            if(data!=null)
            {
                imageURI=data.getData();
                profile.setImageURI(imageURI);
            }
        }
    }

    private void setStatus() {
        if(TextUtils.isEmpty(status.getText().toString()))
            user.setStatus(status.getHint().toString());
        else
            user.setStatus(status.getText().toString());
    }

    private void initialiseViews() {
        profile=findViewById(R.id.profile_image_signup);
        status=findViewById(R.id.userStatusSignup);
        signupButton=findViewById(R.id.signupButton);

        authentication=FirebaseAuth.getInstance();
        user=(User)getIntent().getSerializableExtra("UserData");
        //password=getIntent().getStringExtra("Password");

        usernameProfile=findViewById(R.id.usernameProfile);
        usernameProfile.setText(user.getUsername());

        progressDialog= new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

}