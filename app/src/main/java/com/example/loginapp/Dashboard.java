package com.example.loginapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Dashboard extends AppCompatActivity {

    // firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //ActionBar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //init
        firebaseAuth = FirebaseAuth.getInstance();


        //bottom navigation
        BottomNavigationView navigationView =findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        // home fragment transaction (default, on star)
        actionBar.setTitle("Home");//change actionbar title
        HomeFragment fragment1 =new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    //handle itme clicks
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            // home fragment transaction
                            actionBar.setTitle("Home");//change actionbar title
                            HomeFragment fragment1 =new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            // proflie fragment transaction

                            actionBar.setTitle("Proflie");//change actionbar title
                            ProfileFragment fragment2 =new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            // users fragment transaction

                            actionBar.setTitle("Users");//change actionbar title
                            UsersFragment fragment3 =new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;
                    }

                    return false;
                }
            };

    private void checkUserStatus(){
        //get5 current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //user is signed iin stay here
            //set email of login user
        }
        else{
            // user not signed in, go to main acitivity
            startActivity(new Intent(Dashboard.this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    protected void onStart() {
        // cheeck on start of app
        checkUserStatus();
        super.onStart();
    }

    /*inflate option menu*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflating menu
        getMenuInflater().inflate(R.menu.menu_main , menu);
        return super.onCreateOptionsMenu(menu);
    }
    /* handsle menu item clicks*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
