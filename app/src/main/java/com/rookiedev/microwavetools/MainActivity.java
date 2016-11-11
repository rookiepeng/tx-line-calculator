package com.rookiedev.microwavetools;

import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rookiedev.microwavetools.fragments.CMLINFragment;
import com.rookiedev.microwavetools.fragments.COAXFragment;
import com.rookiedev.microwavetools.fragments.CPWFragment;
import com.rookiedev.microwavetools.fragments.CSLINFragment;
import com.rookiedev.microwavetools.fragments.MLINFragment;
import com.rookiedev.microwavetools.fragments.SLINFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String SHARED_PREFS_NAME = "com.rookiedev.microwavetools_preferences";
    private static final String PREFS_POSITION = "POSITION";
    private static final String PREFS_ISFIRSTRUN = "ISFIRSTRUN";
    public static final String PREFS_MLIN = "MLIN";
    private int pos;
    private DrawerLayout drawer;
    private Fragment fragment=null;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager = getSupportFragmentManager();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (fragment!=null){
                    getSupportActionBar().setTitle(navigationView.getMenu().getItem(pos).getTitle());



                    //fragmentManager.beginTransaction().replace(R.id.content_frame, fragment);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                    transaction.replace(R.id.content_frame,fragment);
                    //transaction.addToBackStack(null);
                    transaction.commit();

                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        readSharedPref();

        navigationView.getMenu().getItem(pos).setChecked(true);
        getSupportActionBar().setTitle(navigationView.getMenu().getItem(pos).getTitle());
        initFragment(pos);
        if (isFirstRun()) {
            drawer.openDrawer(GravityCompat.START);
        }

        /** Look up the AdView as a resource and load a request. */
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // All emulators
                .addTestDevice("015d172c791c0215") // my test device
                .addTestDevice("04afa117002e7ebc") // my test device
                .build();
        adView.loadAd(adRequest);
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
        getMenuInflater().inflate(R.menu.action_menu, menu);
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.menu_preference:
                intent.setClass(this, preferences.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            //case R.id.menu_about:
            //    intent.setClass(this, about.class);
            //    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //    startActivity(intent);
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        //mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREFS_POSITION, String.valueOf(pos));
        editor.apply();
        // saveSharedPref();
    }

    private void readSharedPref() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);// get the parameters from the Shared
        pos = Integer.parseInt(prefs.getString(PREFS_POSITION, "0"));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //fragmentManager.beginTransaction().remove(fragment).commit();
        if (id == R.id.nav_mlin) {
            fragment = new MLINFragment();
            pos=0;
        } else if (id == R.id.nav_cmlin) {
            fragment = new CMLINFragment();
            pos=1;
        } else if (id == R.id.nav_slin) {
            fragment = new SLINFragment();
            pos=2;
        } else if (id == R.id.nav_cslin) {
            fragment = new CSLINFragment();
            pos=3;
        } else if (id == R.id.nav_cpwg) {
            fragment = new CPWFragment();
            pos=4;
        } else if (id == R.id.nav_coax) {
            fragment = new COAXFragment();
            pos=5;
        } else {
            fragment = new MLINFragment();
        }
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void initFragment(int item){
        if (item==0) {
            fragment = new MLINFragment();
            pos=0;
        } else if (item==1) {
            fragment = new CMLINFragment();
            pos=1;
        } else if (item==2) {
            fragment = new SLINFragment();
            pos=2;
        } else if (item==3) {
            fragment = new CSLINFragment();
            pos=3;
        } else if (item==4) {
            fragment = new CPWFragment();
            pos=4;
        } else if (item==5) {
            fragment = new COAXFragment();
            pos=5;
        } else {
            fragment = new MLINFragment();
        }
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE);
        if (prefs.getString(PREFS_ISFIRSTRUN, "true").equals("true")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREFS_ISFIRSTRUN, "false");
            editor.apply();
            return true;
        } else {
            return false;
        }
    }
}
