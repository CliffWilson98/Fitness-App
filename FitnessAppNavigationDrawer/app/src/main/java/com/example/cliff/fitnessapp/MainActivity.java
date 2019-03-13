package com.example.cliff.fitnessapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //a string needed to manage the intent in the onCreate method
    public static final String MY_WORKOUT_FRAGMENT = "MyWorkouts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //If the intent contains an extra then that means there is a fragment to be loaded
        if (getIntent().getExtras() != null)
        {
            try
            {
                String intentFragment = getIntent().getExtras().getString("fragmentToLoad");
                if (intentFragment.equals(MY_WORKOUT_FRAGMENT))
                {
                    fragmentTransactionManager(new MyWorkoutsFragment());
                }
            }catch (NullPointerException e)
            {
                //If there is a null pointer exception then that means the profile
                //fragment should just be loaded since it is the default fragment
                fragmentTransactionManager(new ProfileFragment());
            }
        }
        else
        {
            //If there was no intent then the profile fragment can be loaded
            fragmentTransactionManager(new ProfileFragment());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile)
        {
            fragmentTransactionManager(new ProfileFragment());
        }
        else if (id == R.id.nav_create_workouts)
        {
            fragmentTransactionManager(new CreateWorkoutFragment());
        }
        else if (id == R.id.nav_my_workouts)
        {
            fragmentTransactionManager(new MyWorkoutsFragment());
        }

        else if (id == R.id.calendar) {
            fragmentTransactionManager(new CalendarFragment());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fragmentTransactionManager(Fragment fragmentToSwap)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_placeholder, fragmentToSwap);
        ft.commit();
    }
}
