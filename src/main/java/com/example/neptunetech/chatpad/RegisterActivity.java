package com.example.neptunetech.chatpad;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference mUserDataBase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mData;
    private TextInputLayout mRegName;
    private TextInputLayout mRegEmail;
    private TextInputLayout mRegPass;
    private Button mRegBtn;
    private Toolbar mToolBar;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Firebase Database and authentication
        mAuth = FirebaseAuth.getInstance();

        mData = FirebaseDatabase.getInstance();



        //Progress Dialog
        mProgress = new ProgressDialog(RegisterActivity.this);

        //ToolBar
        mToolBar =(Toolbar) findViewById(R.id.register_toolBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mRegName = (TextInputLayout) findViewById(R.id.displayName);
        mRegEmail = (TextInputLayout) findViewById(R.id.displayEmail);
        mRegPass = (TextInputLayout) findViewById(R.id.displayPassword);
        mRegBtn = (Button) findViewById(R.id.reg_create);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mRegName.getEditText().getText().toString();
                String email = mRegEmail.getEditText().getText().toString();
                String password = mRegPass.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){

                    mProgress.show();
                    mProgress.setTitle("Registering User");
                    mProgress.setMessage("Seat back and enjoy, registration in progress");
                    mProgress.setCanceledOnTouchOutside(false);
                    registerUser(display_name,email,password);
                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(RegisterActivity.this);
                    alert.setTitle("Empty Fields");
                    alert.setMessage("Pls complete the form");
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();

                }

            }
        });
    }

    private void registerUser(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mCurrentUser = mAuth.getCurrentUser();
                            String currentUID = mCurrentUser.getUid();
                            mUserDataBase = mData.getReference().child("User").child(currentUID);

                            //upload the data to d database
                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("name",display_name);
                            userMap.put("status","Hi, there am using ChatPad App");
                            userMap.put("image", "default");
                            userMap.put("thumb_image","default");

                            mUserDataBase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mProgress.dismiss();
                                        Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }

                                }
                            });


                        }
                        else{
                            mProgress.hide();
                            Toast.makeText(RegisterActivity.this,"You are not logged in,Pls confirm details",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
