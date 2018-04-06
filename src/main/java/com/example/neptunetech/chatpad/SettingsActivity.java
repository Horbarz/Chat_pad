package com.example.neptunetech.chatpad;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.R.attr.filterTouchesWhenObscured;
import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;
import static android.content.RestrictionsManager.RESULT_ERROR;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDataBase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mData;
    private TextView mdisplayName;
    private TextView mStatus;
    private Button mStausbtn;
    private Button mImageBtn;
    private CircleImageView mDisplayImage;
    private ProgressDialog mStatusProgress;
    private static final int GALLARY_PICKER = 1;
    //Storage reference
    private StorageReference mImageStorage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mdisplayName = (TextView)findViewById(R.id.settings_name);
        mStatus = (TextView)findViewById(R.id.settings_status);
        mStausbtn = (Button)findViewById(R.id.updateBtn);
        mImageBtn = (Button)findViewById(R.id.ChangeimgBtn);
        mDisplayImage = (CircleImageView)findViewById(R.id.profile_image);


        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mImageStorage = FirebaseStorage.getInstance().getReference();

        //Retrive uid
        String current_uid = mCurrentUser.getUid();
        //Activate referencer to the user object
        mUserDataBase = mData.getReference().child("User").child(current_uid);
        //Retrive the data objects
        mUserDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(),dataSnapshot.getKey(),Toast.LENGTH_SHORT).show();
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mdisplayName.setText(name);
                mStatus.setText(status);
                if(image.equals("default")){
                    Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.profile).into(mDisplayImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStausbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = mStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("status_value",status_value);
                startActivity(statusIntent);
                //finish();
            }
        });
        //Image selector
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLARY_PICKER);


            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLARY_PICKER&&resultCode==RESULT_OK){

            //Source image
            Uri imageUri = data.getData();
            //Destination image
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(),"cropped"));
            //Toast.makeText(SettingsActivity.this,imageUri,Toast.LENGTH_LONG).show();
            UCrop.of(imageUri,destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(maxWidth, maxHeight)
                    .start(SettingsActivity.this);
            //Result URI to store the pics
        }
        if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK ){
            mStatusProgress = new ProgressDialog(SettingsActivity.this);
            mStatusProgress.show();
            mStatusProgress.setTitle("Uploading Image");
            mStatusProgress.setMessage("Please wait, the image is being uploaded and processed");
            mStatusProgress.setCanceledOnTouchOutside(false);
            final Uri resultUri = UCrop.getOutput(data);
            //Create a file path for the result
            final File thumb_filepath = new File(resultUri.getPath());

            String current_uid = mCurrentUser.getUid();

            Bitmap thumb_bitmap = null;
            try {
                thumb_bitmap = new Compressor(SettingsActivity.this)
                        .setMaxHeight(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filepath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            final byte[] thumb_byte = baos.toByteArray();

            //Create file path for fire base
            StorageReference filepath = mImageStorage.child("profile_images").child(current_uid+".jpg");
            final StorageReference thumb_filePath =  mImageStorage.child("profile_images").child("thumbs").child(current_uid+".jpg");
            //Store d image in the selected file path
            filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                       final String download_url = task.getResult().toString();
                        UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> thumb_task) {
                                String thumb_download_url = thumb_task.getResult().toString();
                                if(thumb_task.isSuccessful()){
                                    //To set the data together an update it as well
                                    Map update_map = new HashMap();
                                    update_map.put("image",download_url);
                                    update_map.put("thumb_image",thumb_download_url);

                                    mUserDataBase.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mStatusProgress.dismiss();
                                                Toast.makeText(SettingsActivity.this,"Uploaded Successfully",Toast.LENGTH_LONG).show();
                                            }
                                            else{
                                                Toast.makeText(SettingsActivity.this,"Error in uploading thumb_nail",Toast.LENGTH_LONG).show();
                                                mStatusProgress.dismiss();
                                            }

                                        }
                                    });
                                }
                            }
                        });


                    }else{
                        Toast.makeText(SettingsActivity.this,"Error",Toast.LENGTH_LONG).show();
                        mStatusProgress.dismiss();
                    }
                }
            });
        }
        else if(resultCode == RESULT_ERROR){
            final Throwable cropError = UCrop.getError(data);

        }
    }
//    //Generate random strings for the picture naming
//    public static String random(){
//        Random generator = new Random();
//        StringBuilder randomSB = new StringBuilder();
//        int randomLength = generator.nextInt(20);
//        char tempChar;
//        for(int i=0;i<randomLength;i++){
//            tempChar = (char)(generator.nextInt(96)+32);
//            randomSB.append(tempChar);
//        }
//        return randomSB.toString();
//    }
}
