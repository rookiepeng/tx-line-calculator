package com.rookiedev.microwavetools;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.rookiedev.microwavetools.fragments.CmlinFragment;
import com.rookiedev.microwavetools.fragments.CoaxFragment;
import com.rookiedev.microwavetools.fragments.CpwFragment;
import com.rookiedev.microwavetools.fragments.CslinFragment;
import com.rookiedev.microwavetools.fragments.MlinFragment;
import com.rookiedev.microwavetools.fragments.SlinFragment;
import com.rookiedev.microwavetools.libs.Constants;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int pos;
    private DrawerLayout drawer;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private MlinFragment fragmentMlin = null;
    private CmlinFragment fragmentCmlin = null;
    private SlinFragment fragmentSlin = null;
    private CslinFragment fragmentCslin = null;
    private CpwFragment fragmentCpw = null;
    private CpwFragment fragmentGcpw = null;
    private CoaxFragment fragmentCoax = null;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private FragmentManager fragmentManager;
    private ImageView imageModel;
    private int imageResource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Reset?");
            alertDialog.setMessage("Do you want to reset all the values?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    (dialog, which) -> {
                        switch (pos) {
                            case Constants.PositionMlin:
                                fragmentMlin.resetValues();
                                break;
                            case Constants.PositionCmlin:
                                fragmentCmlin.resetValues();
                                break;
                            case Constants.PositionSlin:
                                fragmentSlin.resetValues();
                                break;
                            case Constants.PositionCslin:
                                fragmentCslin.resetValues();
                                break;
                            case Constants.PositionCpw:
                                fragmentCpw.resetValues();
                                break;
                            case Constants.PositionGcpw:
                                fragmentGcpw.resetValues();
                                break;
                            case Constants.PositionCoax:
                                fragmentCoax.resetValues();
                                break;
                            default:
                                break;
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        });

        navigationView = findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);

        imageModel = findViewById(R.id.imageViewModel);

        fragmentManager = getSupportFragmentManager();

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Bundle bundle = new Bundle();
                FragmentTransaction transaction;
                switch (pos) {
                    default:
                        fragmentMlin.setArguments(bundle);
                        transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                        transaction.replace(R.id.content_frame, fragmentMlin);
                        transaction.commit();
                        break;
                    case Constants.PositionCmlin:
                        fragmentCmlin.setArguments(bundle);
                        transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                        transaction.replace(R.id.content_frame, fragmentCmlin);
                        transaction.commit();
                        break;
                    case Constants.PositionSlin:
                        fragmentSlin.setArguments(bundle);
                        transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                        transaction.replace(R.id.content_frame, fragmentSlin);
                        transaction.commit();
                        break;
                    case Constants.PositionCslin:
                        fragmentCslin.setArguments(bundle);
                        transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                        transaction.replace(R.id.content_frame, fragmentCslin);
                        transaction.commit();
                        break;
                    case Constants.PositionCpw:
                        bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW);
                        fragmentCpw.setArguments(bundle);
                        transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                        transaction.replace(R.id.content_frame, fragmentCpw);
                        transaction.commit();
                        break;
                    case Constants.PositionGcpw:
                        bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW);
                        fragmentGcpw.setArguments(bundle);
                        transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                        transaction.replace(R.id.content_frame, fragmentGcpw);
                        transaction.commit();
                        break;
                    case Constants.PositionCoax:
                        fragmentCoax.setArguments(bundle);
                        transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                        transaction.replace(R.id.content_frame, fragmentCoax);
                        transaction.commit();
                        break;
                }

                mCollapsingToolbarLayout.setTitle(navigationView.getMenu().getItem(pos).getTitle());
                imageModel.setImageResource(imageResource);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        readSharedPref();

        navigationView.getMenu().getItem(pos).setChecked(true);
        initFragment(pos);
        mCollapsingToolbarLayout.setTitle(navigationView.getMenu().getItem(pos).getTitle());
        imageModel.setImageResource(imageResource);


        if (isFirstRun()) {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        if (item.getItemId() == R.id.menu_help) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"rookie.dev@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "");
            i.putExtra(Intent.EXTRA_TEXT, "");
            try {
                startActivity(Intent.createChooser(i, "Send email ..."));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, getResources().getString(R.string.noEmail), Toast.LENGTH_SHORT)
                        .show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Constants.PREFS_POSITION, String.valueOf(pos));
        editor.apply();
    }

    private void readSharedPref() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE);
        pos = Integer.parseInt(prefs.getString(Constants.PREFS_POSITION, "0"));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            default:
                if (fragmentMlin == null) {
                    fragmentMlin = new MlinFragment();
                }
                pos = Constants.PositionMlin;
                imageResource = R.drawable.vt_mlin;
                break;
            case R.id.nav_cmlin:
                if (fragmentCmlin == null) {
                    fragmentCmlin = new CmlinFragment();
                }
                pos = Constants.PositionCmlin;
                imageResource = R.drawable.vt_cmlin;
                break;
            case R.id.nav_slin:
                if (fragmentSlin == null) {
                    fragmentSlin = new SlinFragment();
                }
                pos = Constants.PositionSlin;
                imageResource = R.drawable.vt_slin;
                break;
            case R.id.nav_cslin:
                if (fragmentCslin == null) {
                    fragmentCslin = new CslinFragment();
                }
                pos = Constants.PositionCslin;
                imageResource = R.drawable.vt_cslin;
                break;
            case R.id.nav_cpw:
                if (fragmentCpw == null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW);
                    fragmentCpw = new CpwFragment();
                    fragmentCpw.setArguments(bundle);
                }
                pos = Constants.PositionCpw;
                imageResource = R.drawable.vt_cpw;
                break;
            case R.id.nav_gcpw:
                if (fragmentGcpw == null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW);
                    fragmentGcpw = new CpwFragment();
                    fragmentGcpw.setArguments(bundle);
                }
                pos = Constants.PositionGcpw;
                imageResource = R.drawable.vt_cpwg;
                break;
            case R.id.nav_coax:
                if (fragmentCoax == null) {
                    fragmentCoax = new CoaxFragment();
                }
                pos = Constants.PositionCoax;
                imageResource = R.drawable.vt_coax;
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initFragment(int item) {
        Bundle bundle = new Bundle();
        switch (item) {
            default:
                fragmentMlin = new MlinFragment();
                pos = Constants.PositionMlin;
                imageResource = R.drawable.vt_mlin;
                fragmentMlin.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentMlin).commit();
                break;
            case Constants.PositionCmlin:
                fragmentCmlin = new CmlinFragment();
                pos = Constants.PositionCmlin;
                imageResource = R.drawable.vt_cmlin;
                fragmentCmlin.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCmlin).commit();
                break;
            case Constants.PositionSlin:
                fragmentSlin = new SlinFragment();
                pos = Constants.PositionSlin;
                imageResource = R.drawable.vt_slin;
                fragmentSlin.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentSlin).commit();
                break;
            case Constants.PositionCslin:
                fragmentCslin = new CslinFragment();
                pos = Constants.PositionCslin;
                imageResource = R.drawable.vt_cslin;
                fragmentCslin.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCslin).commit();
                break;
            case Constants.PositionCpw:
                bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW);
                fragmentCpw = new CpwFragment();
                fragmentCpw.setArguments(bundle);
                pos = Constants.PositionCpw;
                imageResource = R.drawable.vt_cpw;
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCpw).commit();
                break;
            case Constants.PositionGcpw:
                bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW);
                fragmentGcpw = new CpwFragment();
                fragmentGcpw.setArguments(bundle);
                pos = Constants.PositionGcpw;
                imageResource = R.drawable.vt_cpwg;
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentGcpw).commit();
                break;
            case Constants.PositionCoax:
                fragmentCoax = new CoaxFragment();
                pos = Constants.PositionCoax;
                imageResource = R.drawable.vt_coax;
                fragmentCoax.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCoax).commit();
                break;
        }
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE);
        if (prefs.getString(Constants.PREFS_ISFIRSTRUN, "true").equals("true")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.PREFS_ISFIRSTRUN, "false");
            editor.apply();
            return true;
        } else {
            return false;
        }
    }

}
