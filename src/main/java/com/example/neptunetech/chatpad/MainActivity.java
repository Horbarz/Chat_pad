package com.example.neptunetech.chatpad;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPager mSectionsPager;
    private TabLayout mTablayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar)findViewById(R.id.main_toolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatPad");



        //Tabs
        mViewPager = (ViewPager)findViewById(R.id.tabPager);
        mTablayout = (TabLayout)findViewById(R.id.mainTabs);

        mSectionsPager = new SectionsPager(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPager);
        mTablayout.setupWithViewPager(mViewPager);

        mAuth=FirebaseAuth.getInstance();
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            sendOnStart();
        }
    }
    private void sendOnStart(){
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                Intent settingIntent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(settingIntent);
                return true;
            case R.id.action_group:
                Intent UsersIntent = new Intent(getApplicationContext(),UsersActivity.class);
                startActivity(UsersIntent);
                return true;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                sendOnStart();
                return true;

        }
        return super.onOptionsItemSelected(item);

    }


}
