package com.example.neptunetech.chatpad;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private RecyclerView mUserslist;
    private DatabaseReference mUsersData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolBar = (Toolbar)findViewById(R.id.users_toolBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Set up firebase recycler view
        mUserslist = (RecyclerView)findViewById(R.id.users_list);
        mUserslist.setHasFixedSize(true);
        mUserslist.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //Firebase database reference to retrive what is in the database
        mUsersData = FirebaseDatabase.getInstance().getReference().child("User");


    }

    @Override
    protected void onStart() {
        super.onStart();
        //Make use of the firebase recycler view
        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.user_single_layout,
                UsersViewHolder.class,
                mUsersData


        ) {
            @Override
            //The method is used to set the values to our recycler view
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, final int position) {
                viewHolder.setDisplayName(model.getName());
                viewHolder.setDisplayStatus(model.getStatus());
                viewHolder.setUserImage(model.getThumbImage(),getApplicationContext());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        mUserslist.setAdapter(firebaseRecyclerAdapter);
    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDisplayName(String name){
            TextView userName = (TextView) mView.findViewById(R.id.user_single_name);
            userName.setText(name);
        }
        public void setDisplayStatus(String status){
            TextView userStatus = (TextView) mView.findViewById(R.id.user_single_status);
            userStatus.setText(status);
        }
        public void setUserImage(String thumb_image, Context context){
            CircleImageView userImage = (CircleImageView)mView.findViewById(R.id.user_single_image);
            //To load the image
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.profile).into(userImage);
        }
    }

}
