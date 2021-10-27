package com.example.discuter.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import androidx.biometric.BiometricManager;

import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discuter.ModelClass.User;
import com.example.discuter.R;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateUserInfoActivity extends AppCompatActivity {

    private User currentUser;
    private TextInputEditText updateUsername,updateStatus;
    private CircleImageView updateProfile;
    private Button saveChanges;
    private ImageView camera,delete;
    private Uri selectedImageUri;
    private ProgressBar load;
    private FirebaseAuth auth;
    private Switch sw_fingerprint_enabled;
    private LinearLayout fingerprint_auth_optionHolder;
    private ImageView iv_infoButton;
    private  int scannerAvailable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);

        fingerprint_auth_optionHolder=findViewById(R.id.fingerprint_auth_optionHolder);
        checkAuthPossiblility();

        initialiseViews();
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent=new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"Pick your Profile Image"),11);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!currentUser.getProfileLink().equals("https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/DefaultProfile.png?alt=media&token=aadb4dc4-3f12-432f-9bac-c674028e706d"))
                    FirebaseStorage.getInstance().getReference().child("Profile Images").child(currentUser.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            selectedImageUri=Uri.parse("https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/DefaultProfile.png?alt=media&token=aadb4dc4-3f12-432f-9bac-c674028e706d");
                            currentUser.setProfileLink("https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/DefaultProfile.png?alt=media&token=aadb4dc4-3f12-432f-9bac-c674028e706d");
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/DefaultProfile.png?alt=media&token=aadb4dc4-3f12-432f-9bac-c674028e706d")
                                    .into(updateProfile);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed to remove the profile ", Toast.LENGTH_SHORT).show();
                        }
                    });


            }
        });

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updateStatus.getText().toString().isEmpty()) {
                    updateStatus.setText("Hey Amigo ! I am using Discuter .");
                }

                if(CheckNetworkAvailibility.isConnectionAvailable(UpdateUserInfoActivity.this))
                {
                   //Checks if user has enabled the biometrics.
                   if(sw_fingerprint_enabled.isChecked()  && scannerAvailable==1) {
                       Toast.makeText(getApplicationContext(), "You have enabled fingerprint authentication", Toast.LENGTH_SHORT).show();
                       currentUser.setFingerprint_auth_enabled(true);
                   }
                    if(!sw_fingerprint_enabled.isChecked()  && scannerAvailable==1) {
                        Toast.makeText(getApplicationContext(), "You have disabled fingerprint authentication", Toast.LENGTH_SHORT).show();
                        currentUser.setFingerprint_auth_enabled(false);
                    }
                   else if(sw_fingerprint_enabled.isEnabled()  && scannerAvailable==2) {
                       currentUser.setFingerprint_auth_enabled(false);
                       Toast.makeText(getApplicationContext(), "Fingerprints unfound !! Kindly check device security settings and then try again", Toast.LENGTH_LONG).show();
                   }
                   else if(sw_fingerprint_enabled.isEnabled()  && scannerAvailable==4) {
                       currentUser.setFingerprint_auth_enabled(false);
                       Toast.makeText(getApplicationContext(), "Biometric unavaiable!! Try again later", Toast.LENGTH_LONG).show();
                   }

                   //checks if only data has been chaged or profile is also changed and then generated the respective action
                   if(selectedImageUri.toString().equals(currentUser.getProfileLink())) {
                        load.setVisibility(View.VISIBLE);
                        updateUserData();
                    }
                    else {
                        load.setVisibility(View.VISIBLE);
                        updateUserProfile();
                    }

                }
                else {
                    Snackbar snackbar = Snackbar.make(v, "NETWORK UNAVAILABLE", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { }});
                    snackbar.setBackgroundTint(Color.parseColor("#93999C"));
                    snackbar.setTextColor(Color.parseColor("#222D32"));
                    snackbar.setActionTextColor(Color.parseColor("#171F24"));
                    snackbar.show();
                }
            }
        });

        iv_infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog fingerprintAuthDialog=new Dialog(UpdateUserInfoActivity.this);
                fingerprintAuthDialog.setContentView(R.layout.fingerprint_auth_dialog);
                fingerprintAuthDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                fingerprintAuthDialog.setCancelable(false);
                TextView ok=fingerprintAuthDialog.findViewById(R.id.ok2);

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fingerprintAuthDialog.dismiss();
                    }
                });
                fingerprintAuthDialog.show();


            }
        });

    }

    private void checkAuthPossiblility() {
        BiometricManager biometricManager=BiometricManager.from(this);
        switch (biometricManager.canAuthenticate())
        {
            case BiometricManager.BIOMETRIC_SUCCESS:
                scannerAvailable=1;
                fingerprint_auth_optionHolder.setVisibility(View.VISIBLE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                scannerAvailable=2;
                fingerprint_auth_optionHolder.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                scannerAvailable=3;
                fingerprint_auth_optionHolder.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                scannerAvailable=4;
                fingerprint_auth_optionHolder.setVisibility(View.GONE);
                break;
        }
    }

    private void updateUserProfile() {
        StorageReference IMAGE= FirebaseStorage.getInstance().getReference().child("Profile Images").child(currentUser.getUid());
        if(validate())
        {
            IMAGE.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(UpdateUserInfoActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        IMAGE.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                currentUser.setProfileLink(uri.toString());
                                updateUserData();
                            }
                        });
                    } else {
                        Toast.makeText(UpdateUserInfoActivity.this, "Profile updation failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateUserData() {
        if(validate())
        {
            currentUser.setStatus(updateStatus.getText().toString());
            currentUser.setUsername(updateUsername.getText().toString());
            FirebaseDatabase.getInstance().getReference().child("User").child(currentUser.getUid())
                    .setValue(currentUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(UpdateUserInfoActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                    load.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UpdateUserInfoActivity.this, "Updation Failed", Toast.LENGTH_SHORT).show();
                    load.setVisibility(View.GONE);
                }
            });
        }

    }


    private boolean validate() {
        if(updateUsername.getText().toString().trim().equals("")||hasSpecialCharacters(updateUsername.getText().toString().trim()))
        {
            Toast.makeText(UpdateUserInfoActivity.this,"Invalid Username : only Lowercase Letters, Numbers and Period allowed ",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==11)
        {
            if(data!=null)
            {
                selectedImageUri =data.getData();
                updateProfile.setImageURI(selectedImageUri);
            }
        }
    }

    private void initialiseViews() {

        updateProfile=findViewById(R.id.civ_updateProfile);
        updateUsername=findViewById(R.id.til_updateUsername);
        updateStatus=findViewById(R.id.til_updateStatus);
        sw_fingerprint_enabled=findViewById(R.id.sw_fingerprint_enabled);

        auth=FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().getReference().child("User").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser=snapshot.getValue(User.class);
                Picasso.get().load(currentUser.getProfileLink()).into(updateProfile);
                updateUsername.setText(currentUser.getUsername());
                updateStatus.setText(currentUser.getStatus());
                selectedImageUri=Uri.parse(currentUser.getProfileLink());
                if(currentUser.getFingerprint_auth_enabled())
                    sw_fingerprint_enabled.setChecked(true);
                else
                    sw_fingerprint_enabled.setChecked(false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        saveChanges=findViewById(R.id.btn_saveChanges);
        camera=findViewById(R.id.iv_camera1);
        delete=findViewById(R.id.iv_delete1);
        iv_infoButton=findViewById(R.id.iv_infoButton);


        load=findViewById(R.id.load1);
    }

    private boolean hasSpecialCharacters(String username) {
        for(int i=0;i<username.length();i++)
        {
            if(!(username.charAt(i)>=49&&username.charAt(i)<=57||username.charAt(i)>=97&&username.charAt(i)<=122||username.charAt(i)!='.'))
            {
                return true;
            }
        }
        return false;

    }
}