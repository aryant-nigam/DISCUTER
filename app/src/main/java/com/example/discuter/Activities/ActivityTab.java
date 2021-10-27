package com.example.discuter.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.discuter.Adapters.PageAdapter;
import com.example.discuter.ModelClass.User;
import com.example.discuter.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityTab extends AppCompatActivity {

    private TabLayout tl_tabLayout;
    private TabItem ti_chats,ti_groups,ti_status;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;

    private ImageView menu;
    private CircleImageView profileDisplay;
    private TextView usernameDisplay;
    private Dialog profileDialog;
    private FirebaseAuth authenticate;
    private LinearLayout toolbar;
    private User currentUser=new User();


    @Override
    public void onBackPressed() {
       // super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        //----------------------------------------------------INITIALISING ELEMENTS-------------------------------------------------
        initialise();

        //----------------------------------------------------SETTING PAGE ADAPTER--------------------------------------------------
        pageAdapter=new PageAdapter(getSupportFragmentManager(),tl_tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);

        //---------------------------------------------------SETTING LISTENER TO TAB LAYOUT-&-VIEW PAGER----------------------------------------
        tl_tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition()==0 ||tab.getPosition()==1 ||tab.getPosition()==2)
                    pageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tl_tabLayout));

        //----------------------------------------------------FETCHING CURRENT USER DATA-------------------------------------------------------
        DatabaseReference USERS= FirebaseDatabase.getInstance().getReference().child("User");
        USERS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot u:snapshot.getChildren())
                {
                    User user=u.getValue(User.class);
                    if(authenticate.getUid().equals(user.getUid())&&!user.getProfileLink().equals("")){
                        currentUser = u.getValue(User.class);
                        usernameDisplay.setText(currentUser.getUsername());
                        Picasso.get().load(currentUser.getProfileLink()).into(profileDisplay);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });


        //---------------------------------------------------SETTING UP MENU-------------------------------------------------------------------

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu utilityMenu=new PopupMenu(ActivityTab.this,menu);
                try {
                    Field[] fields = utilityMenu.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(utilityMenu);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                utilityMenu.getMenuInflater().inflate(R.menu.utilities,utilityMenu.getMenu());

                utilityMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.ProfileSettingsUtility:
                                profileSettings();
                                break;
                            case R.id.ChangePasswordUtility:
                                changePassword();
                                break;
                            case R.id.CreateGroupUtility:
                                createGroup();
                                break;
                            case R.id.LogOutUtility:
                                logOut();
                        }
                        return true;
                    }
                });
                utilityMenu.show();
            }
        });

        //------------------------------------------------------SETTING LISTENER TO DISPLAY CURRENT USER PROFILE-------------------------
        profileDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDialog=new Dialog(ActivityTab.this);
                profileDialog.setContentView(R.layout.show_profile_dialog);
                profileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                ImageView back=profileDialog.findViewById(R.id.backButton);
                ImageView profile=profileDialog.findViewById(R.id.userProfileImage);
                TextView uname=profileDialog.findViewById(R.id.usernameShowProfileActivity);

                Picasso.get().load(currentUser.getProfileLink()).into(profile);
                uname.setText(currentUser.getUsername());

                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profileDialog.dismiss();
                    }
                });

                profileDialog.show();

            }
        });

    }

    //----------------------------------------------------------DEFINING MENU UTILITIES---------------------------------------------
    private void profileSettings() {
        Intent intent= new Intent(ActivityTab.this, UpdateUserInfoActivity.class);
        startActivity(intent);
    }

    private void changePassword() {
        startActivity(new Intent(getApplicationContext(),TempHolderActivity.class));
    }

    private void createGroup() {
        startActivity(new Intent(ActivityTab.this, CreateGroup.class));
    }

    private void logOut() {
        Dialog logoutDialog=new Dialog(ActivityTab.this);
        logoutDialog.setContentView(R.layout.logout_dialog);
        logoutDialog.setCancelable(false);
        logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView yes=logoutDialog.findViewById(R.id.ok2);
        TextView no=logoutDialog.findViewById(R.id.no);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ActivityTab.this, LoginSignup.class));
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog.dismiss();
            }
        });

        logoutDialog.show();
    }

    //--------------------------------------------------------INITIALISING FUNCTION DEFINITION---------------------------------------
    private void initialise() {
        tl_tabLayout=findViewById(R.id.tl_tablayout);

        ti_chats=findViewById(R.id.ti_chats);
        ti_groups=findViewById(R.id.ti_groups);
        ti_status=findViewById(R.id.ti_moods);

        toolbar=findViewById(R.id.toolbar);
        viewPager=findViewById(R.id.vp_viewpager);

        authenticate = FirebaseAuth.getInstance();
        menu = findViewById(R.id.menu);
        profileDisplay = findViewById(R.id.profileDisplay_homeActivity);
        usernameDisplay = findViewById(R.id.usernameDisplay_homeActivity);

        registerForContextMenu(menu);


    }
}