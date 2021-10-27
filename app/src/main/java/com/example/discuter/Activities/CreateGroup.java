package com.example.discuter.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.example.discuter.ModelClass.Groups;
import com.example.discuter.R;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroup extends AppCompatActivity {
    private Animation popup;
    TextInputEditText et_groupName,et_groupDescription;
    CircleImageView civ_groupProfile;
    Button btn_createGroup;
    private CardView form;
    Uri groupProfileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initialise();



        civ_groupProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent=new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"Pick your Profile Image"),10);
            }
        });

        btn_createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckNetworkAvailibility.isConnectionAvailable(getApplicationContext()))
                {
                    if(validate())
                    {
                        Calendar calendar=Calendar.getInstance();
                        if(groupProfileUri!=null)
                        {

                            Groups groups =new Groups(
                                    et_groupName.getText().toString().trim(),
                                    et_groupDescription.getText().toString().trim(),
                                    "",
                                    null,
                                    String.valueOf(calendar.getTimeInMillis()),
                                    ""
                            );

                            Intent intent=new Intent(getApplicationContext(),SelectGroupMembers.class);
                            intent.putExtra("GroupData", groups);
                            intent.putExtra("groupProfileUri",groupProfileUri);
                            intent.putExtra("from","CGA");
                            startActivity(intent);
                        }
                        else
                        {
                            String defaultUri="https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/DefaultGroupProfile.jpg?alt=media&token=3125349c-c46b-4941-b772-54eb8c29e853";
                            Groups groups =new Groups(
                                    et_groupName.getText().toString().trim(),
                                    et_groupDescription.getText().toString().trim(),
                                    defaultUri,
                                    null,
                                    String.valueOf(calendar.getTimeInMillis()),
                                    ""
                            );

                            Intent intent=new Intent(getApplicationContext(),SelectGroupMembers.class);
                            intent.putExtra("GroupData", groups);
                            intent.putExtra("from","CGA");
                            startActivity(intent);
                        }

                    }
                }
                else
                {
                    Snackbar snackbar=Snackbar.make(v,"Network Unavailable !!",Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
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

    private boolean validate() {
        if(et_groupName.getText().toString().trim().equals(""))
        {
            Toast.makeText(getApplicationContext(), "Specify group name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(hasSpecialCharacters(et_groupName.getText().toString().trim()))
        {
            Toast.makeText(getApplicationContext(), "Only Letters, Numbers and Spaces allowed allowed in groupname", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(et_groupDescription.getText().toString().trim().equals(""))
        {
            et_groupDescription.setText("Discuter Groups - Celebrating Togetherness ..");
            return true;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null)
        {
            groupProfileUri=data.getData();
            Picasso.get().load(groupProfileUri).into(civ_groupProfile);
        }
    }

    private void initialise() {
        form=findViewById(R.id.testa);
        popup= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.popup);
        popup.setDuration(500l);
        form.startAnimation(popup);

        et_groupName=findViewById(R.id.et_groupName);
        et_groupDescription=findViewById(R.id.et_groupDescription);
        civ_groupProfile=findViewById(R.id.civ_group_profile);
        btn_createGroup=findViewById(R.id.btn_createGroup);
    }

    private boolean hasSpecialCharacters(String groupname) {
        for(int i=0;i<groupname.length();i++)
        {
            if(!(groupname.charAt(i)>=48&&groupname.charAt(i)<=57||groupname.charAt(i)>=97&&groupname.charAt(i)<=122||groupname.charAt(i)>=65&&groupname.charAt(i)<=90 ||groupname.charAt(i)==' '))
            {
                return true;
            }
        }
        return false;
    }

}