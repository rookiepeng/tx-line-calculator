package com.rookiedev.microwavetools;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.android.billingclient.api.BillingClient;
import com.rookiedev.microwavetools.billing.BillingManager;
import com.rookiedev.microwavetools.billing.BillingProvider;
import com.rookiedev.microwavetools.fragments.CmlinFragment;
import com.rookiedev.microwavetools.fragments.CoaxFragment;
import com.rookiedev.microwavetools.fragments.CpwFragment;
import com.rookiedev.microwavetools.fragments.CslinFragment;
import com.rookiedev.microwavetools.fragments.MlinFragment;
import com.rookiedev.microwavetools.fragments.SlinFragment;
import com.rookiedev.microwavetools.libs.Constants;

import java.util.List;

import static com.rookiedev.microwavetools.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BillingProvider {

    private final static String SKU_ADFREE = "com.rookiedev.rfline.adfree.v1";
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

    private boolean isAdFree = false;
    private boolean isChecked = false;

    private BillingManager mBillingManager;
    private final UpdateListener mUpdateListener = new UpdateListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                if (pos == 0) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentMlin.setArguments(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentMlin);
                    transaction.commit();
                } else if (pos == 1) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentCmlin.setArguments(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentCmlin);
                    transaction.commit();
                } else if (pos == 2) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentSlin.setArguments(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentSlin);
                    transaction.commit();
                } else if (pos == 3) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentCslin.setArguments(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentCslin);
                    transaction.commit();
                } else if (pos == 4) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW);
                    fragmentCpw.setArguments(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentCpw);
                    transaction.commit();
                } else if (pos == 5) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW);
                    fragmentGcpw.setArguments(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentGcpw);
                    transaction.commit();
                } else if (pos == 6) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentCoax.setArguments(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentCoax);
                    transaction.commit();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentMlin.setArguments(bundle);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentMlin);
                    transaction.commit();
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

        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this, mUpdateListener);

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

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_ad);
        if (isAdFree) {
            menuItem.setVisible(false);
        } else {
            menuItem.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        Intent intent = new Intent();
        switch (item.getItemId()) {
        case R.id.menu_ad:
            onAdfreeButtonClicked();
            return true;
        case R.id.menu_preference:
            intent.setClass(this, preferences.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
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
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE);// get
                                                                                                                    // the
                                                                                                                    // header_parameters
                                                                                                                    // from
                                                                                                                    // the
                                                                                                                    // Shared
        pos = Integer.parseInt(prefs.getString(Constants.PREFS_POSITION, "0"));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_mlin) {
            if (fragmentMlin == null) {
                fragmentMlin = new MlinFragment();
            }
            pos = 0;
            imageResource = R.drawable.vt_mlin;
        } else if (id == R.id.nav_cmlin) {
            if (fragmentCmlin == null) {
                fragmentCmlin = new CmlinFragment();
            }
            pos = 1;
            imageResource = R.drawable.vt_cmlin;
        } else if (id == R.id.nav_slin) {
            if (fragmentSlin == null) {
                fragmentSlin = new SlinFragment();
            }
            pos = 2;
            imageResource = R.drawable.vt_slin;
        } else if (id == R.id.nav_cslin) {
            if (fragmentCslin == null) {
                fragmentCslin = new CslinFragment();
            }
            pos = 3;
            imageResource = R.drawable.vt_cslin;
        } else if (id == R.id.nav_cpw) {
            if (fragmentCpw == null) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW);
                fragmentCpw = new CpwFragment();
                fragmentCpw.setArguments(bundle);
            }
            pos = 4;
            imageResource = R.drawable.vt_cpw;
        } else if (id == R.id.nav_gcpw) {
            if (fragmentGcpw == null) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW);
                fragmentGcpw = new CpwFragment();
                fragmentGcpw.setArguments(bundle);
            }
            pos = 5;
            imageResource = R.drawable.vt_cpwg;
        } else if (id == R.id.nav_coax) {
            if (fragmentCoax == null) {
                fragmentCoax = new CoaxFragment();
            }
            pos = 6;
            imageResource = R.drawable.vt_coax;
        } else {
            if (fragmentMlin == null) {
                fragmentMlin = new MlinFragment();
            }
            pos = 0;
            imageResource = R.drawable.vt_mlin;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initFragment(int item) {
        if (item == 0) {
            fragmentMlin = new MlinFragment();
            pos = 0;
            imageResource = R.drawable.vt_mlin;
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentMlin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentMlin).commit();
        } else if (item == 1) {
            fragmentCmlin = new CmlinFragment();
            pos = 1;
            imageResource = R.drawable.vt_cmlin;
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentCmlin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCmlin).commit();
        } else if (item == 2) {
            fragmentSlin = new SlinFragment();
            pos = 2;
            imageResource = R.drawable.vt_slin;
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentSlin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentSlin).commit();
        } else if (item == 3) {
            fragmentCslin = new CslinFragment();
            pos = 3;
            imageResource = R.drawable.vt_cslin;
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentCslin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCslin).commit();
        } else if (item == 4) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW);
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentCpw = new CpwFragment();
            fragmentCpw.setArguments(bundle);
            pos = 4;
            imageResource = R.drawable.vt_cpw;
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCpw).commit();
        } else if (item == 5) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW);
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentGcpw = new CpwFragment();
            fragmentGcpw.setArguments(bundle);
            pos = 5;
            imageResource = R.drawable.vt_cpwg;
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentGcpw).commit();
        } else if (item == 6) {
            fragmentCoax = new CoaxFragment();
            pos = 6;
            imageResource = R.drawable.vt_coax;
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentCoax.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCoax).commit();
        } else {
            fragmentMlin = new MlinFragment();
            pos = 0;
            imageResource = R.drawable.vt_mlin;
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentMlin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentMlin).commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Note: We query purchases in onResume() to handle purchases completed while
        // the activity
        // is inactive. For example, this can happen if the activity is destroyed during
        // the
        // purchase flow. This ensures that when the activity is resumed it reflects the
        // user's
        // current purchases.
        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode() == BillingClient.BillingResponse.OK) {
            mBillingManager.queryPurchases();
        }
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        if (mBillingManager != null) {
            mBillingManager.destroy();
        }
        super.onDestroy();
    }

    // User clicked the "Ad free" button
    public void onAdfreeButtonClicked() {
        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode() > BILLING_MANAGER_NOT_INITIALIZED) {
            mBillingManager.initiatePurchaseFlow(SKU_ADFREE, BillingClient.SkuType.INAPP);
        } else {
            isChecked = true;
            if (pos == 0) {
                fragmentMlin.addAdFragment();
            } else if (pos == 1) {
                fragmentCmlin.addAdFragment();
            } else if (pos == 2) {
                fragmentSlin.addAdFragment();
            } else if (pos == 3) {
                fragmentCslin.addAdFragment();
            } else if (pos == 4) {
                fragmentCpw.addAdFragment();
            } else if (pos == 5) {
                fragmentGcpw.addAdFragment();
            } else if (pos == 6) {
                fragmentCoax.addAdFragment();
            }
        }
    }

    /**
     * Handler to billing updates
     */
    private class UpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {

        }

        @Override
        public void onConsumeFinished(String token, @BillingClient.BillingResponse int result) {

        }

        @Override
        public void onPurchasesUpdated(List<com.android.billingclient.api.Purchase> purchases) {
            if (purchases.isEmpty()) {
                isChecked = true;
                if (pos == 0) {
                    fragmentMlin.addAdFragment();
                } else if (pos == 1) {
                    fragmentCmlin.addAdFragment();
                } else if (pos == 2) {
                    fragmentSlin.addAdFragment();
                } else if (pos == 3) {
                    fragmentCslin.addAdFragment();
                } else if (pos == 4) {
                    fragmentCpw.addAdFragment();
                } else if (pos == 5) {
                    fragmentGcpw.addAdFragment();
                } else if (pos == 6) {
                    fragmentCoax.addAdFragment();
                }
            }
            for (com.android.billingclient.api.Purchase purchase : purchases) {
                if (purchase.getSku().equals(SKU_ADFREE)) {
                    isChecked = true;
                    isAdFree = true;
                    if (pos == 0) {
                        fragmentMlin.removeAdFragment();
                    } else if (pos == 1) {
                        fragmentCmlin.removeAdFragment();
                    } else if (pos == 2) {
                        fragmentSlin.removeAdFragment();
                    } else if (pos == 3) {
                        fragmentCslin.removeAdFragment();
                    } else if (pos == 4) {
                        fragmentCpw.removeAdFragment();
                    } else if (pos == 5) {
                        fragmentGcpw.removeAdFragment();
                    } else if (pos == 6) {
                        fragmentCoax.removeAdFragment();
                    }
                    invalidateOptionsMenu();
                }
            }
        }
    }

    @Override
    public BillingManager getBillingManager() {
        return null;
    }

    @Override
    public boolean isPremiumPurchased() {
        return false;
    }

    @Override
    public boolean isGoldMonthlySubscribed() {
        return false;
    }

    @Override
    public boolean isTankFull() {
        return false;
    }

    @Override
    public boolean isGoldYearlySubscribed() {
        return false;
    }
}
