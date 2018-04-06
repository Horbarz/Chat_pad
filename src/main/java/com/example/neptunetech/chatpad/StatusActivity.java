package com.example.neptunetech.chatpad;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatusInput;
    private Button mSaveBtn;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mStatusDatabase;
    private ProgressDialog mStatusProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //Tool bar
        mToolbar=(Toolbar)findViewById(R.id.status_toolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatusInput = (TextInputLayout)findViewById(R.id.status_input);
        mSaveBtn = (Button)findViewById(R.id.save_status);

        String status_value = getIntent().getStringExtra("status_value").toString();
        mStatusInput.getEditText().setText(status_value);

        //Fire base Settings
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(current_uid);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusProgress = new ProgressDialog(StatusActivity.this);
                mStatusProgress.setTitle("Saving Changes");
                mStatusProgress.setMessage("Updating status...");
                mStatusProgress.show();
                String status = mStatusInput.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mStatusProgress.dismiss();
                            Toast.makeText(StatusActivity.this,"Updated Successfully",Toast.LENGTH_LONG).show();
                        }
                        else{
                            mStatusProgress.hide();
                            Toast.makeText(StatusActivity.this,"Update Failed,Check internet connection",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });



    }
}
