package com.rookiedev.microwavetools;

import android.content.DialogInterface;
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

import com.google.android.gms.ads.MobileAds;
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

//import static com.rookiedev.microwavetools.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;

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

    private boolean isAdFree = true;
    private boolean isChecked = true;

//    private BillingClientLifecycle billingClientLifecycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Reset?");
                alertDialog.setMessage("Do you want to reset all the values?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dialog.dismiss();
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
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
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
//                case Constants.PositionMlin:
                default:
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentMlin.setArguments(bundle);
                    transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentMlin);
                    transaction.commit();
                    break;
                case Constants.PositionCmlin:
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentCmlin.setArguments(bundle);
                    transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentCmlin);
                    transaction.commit();
                    break;
                case Constants.PositionSlin:
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentSlin.setArguments(bundle);
                    transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentSlin);
                    transaction.commit();
                    break;
                case Constants.PositionCslin:
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    fragmentCslin.setArguments(bundle);
                    transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentCslin);
                    transaction.commit();
                    break;
                case Constants.PositionCpw:
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW);
                    fragmentCpw.setArguments(bundle);
                    transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentCpw);
                    transaction.commit();
                    break;
                case Constants.PositionGcpw:
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
                    bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW);
                    fragmentGcpw.setArguments(bundle);
                    transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                    transaction.replace(R.id.content_frame, fragmentGcpw);
                    transaction.commit();
                    break;
                case Constants.PositionCoax:
                    bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
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

        /* *****************************************************/
//        billingClientLifecycle = ((SubApp) getApplication()).getBillingClientLifecycle();
//        getLifecycle().addObserver(billingClientLifecycle);
//
//        // Register purchases when they change.
//        billingClientLifecycle.purchaseUpdateEvent.observe(this, new Observer<List<Purchase>>() {
//            @Override
//            public void onChanged(List<Purchase> purchases) {
//                if (purchases != null) {
//                    registerPurchases(purchases);
//                }
//            }
//        });

        // Create and initialize BillingManager which talks to BillingLibrary
//        mBillingManager = new BillingManager(this, mUpdateListener);

        if (isFirstRun()) {
            drawer.openDrawer(GravityCompat.START);
        }

        MobileAds.initialize(this, "ca-app-pub-6523245757541965~3260298534");
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
        switch (item.getItemId()) {
        case R.id.menu_ad:
            onAdfreeButtonClicked();
            return true;
        case R.id.menu_help:
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[] { "rookie.dev@gmail.com" });
            i.putExtra(Intent.EXTRA_SUBJECT, "");
            i.putExtra(Intent.EXTRA_TEXT, "");
            try {
                startActivity(Intent.createChooser(i, "Send email ..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, getResources().getString(R.string.noEmail), Toast.LENGTH_SHORT)
                        .show();
            }
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
//        case R.id.nav_mlin:
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
//        case Constants.PositionMlin:
        default:
            fragmentMlin = new MlinFragment();
            pos = Constants.PositionMlin;
            imageResource = R.drawable.vt_mlin;
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentMlin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentMlin).commit();
            break;
        case Constants.PositionCmlin:
            fragmentCmlin = new CmlinFragment();
            pos = Constants.PositionCmlin;
            imageResource = R.drawable.vt_cmlin;
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentCmlin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCmlin).commit();
            break;
        case Constants.PositionSlin:
            fragmentSlin = new SlinFragment();
            pos = Constants.PositionSlin;
            imageResource = R.drawable.vt_slin;
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentSlin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentSlin).commit();
            break;
        case Constants.PositionCslin:
            fragmentCslin = new CslinFragment();
            pos = Constants.PositionCslin;
            imageResource = R.drawable.vt_cslin;
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentCslin.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCslin).commit();
            break;
        case Constants.PositionCpw:
            bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW);
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentCpw = new CpwFragment();
            fragmentCpw.setArguments(bundle);
            pos = Constants.PositionCpw;
            imageResource = R.drawable.vt_cpw;
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCpw).commit();
            break;
        case Constants.PositionGcpw:
            bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW);
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
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
            bundle.putBoolean(Constants.IS_AD_FREE, !(isChecked && (!isAdFree)));
            fragmentCoax.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentCoax).commit();
            break;
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
//        if (mBillingManager != null
//                && mBillingManager.getBillingClientResponseCode() == BillingClient.BillingResponse.OK) {
//            mBillingManager.queryPurchases();
//        }
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
//        if (mBillingManager != null) {
//            mBillingManager.destroy();
//        }
        super.onDestroy();
    }

    // User clicked the "Ad free" button
    public void onAdfreeButtonClicked() {
//        if (mBillingManager != null
//                && mBillingManager.getBillingClientResponseCode() > BILLING_MANAGER_NOT_INITIALIZED) {
//            mBillingManager.initiatePurchaseFlow(BillingConstants.SKU_ADFREE, BillingClient.SkuType.INAPP);
//        }
    }

    /*
     * Register SKUs and purchase tokens with the server.
     */
//    private void registerPurchases(List<Purchase> purchaseList) {
//        for (Purchase purchase : purchaseList) {
//            String sku = purchase.getSku();
//            String purchaseToken = purchase.getPurchaseToken();
////            Log.d(TAG, "Register purchase with sku: " + sku + ", token: " + purchaseToken);
////            subscriptionViewModel.registerSubscription(sku, purchaseToken);
//        }
//    }

    /*
     * Handler to billing updates
     */
//    private class UpdateListener implements BillingManager.BillingUpdatesListener {
//        @Override
//        public void onBillingClientSetupFinished() {
//
//        }
//
//        @Override
//        public void onConsumeFinished(String token, @BillingClient.BillingResponse int result) {
//
//        }
//
//        @Override
//        public void onPurchasesUpdated(List<com.android.billingclient.api.Purchase> purchases) {
//            isChecked = true;
//            if (purchases.isEmpty()) {
//                switch (pos) {
//                case Constants.PositionMlin:
//                    fragmentMlin.addAdFragment();
//                    break;
//                case Constants.PositionCmlin:
//                    fragmentCmlin.addAdFragment();
//                    break;
//                case Constants.PositionSlin:
//                    fragmentSlin.addAdFragment();
//                    break;
//                case Constants.PositionCslin:
//                    fragmentCslin.addAdFragment();
//                    break;
//                case Constants.PositionCpw:
//                    fragmentCpw.addAdFragment();
//                    break;
//                case Constants.PositionGcpw:
//                    fragmentGcpw.addAdFragment();
//                    break;
//                case Constants.PositionCoax:
//                    fragmentCoax.addAdFragment();
//                    break;
//                default:
//                    break;
//                }
//
//            }
//            for (com.android.billingclient.api.Purchase purchase : purchases) {
//                if (purchase.getSku().equals(BillingConstants.SKU_ADFREE)) {
//                    isAdFree = true;
//                    switch (pos) {
//                    case Constants.PositionMlin:
//                        fragmentMlin.removeAdFragment();
//                        break;
//                    case Constants.PositionCmlin:
//                        fragmentCmlin.removeAdFragment();
//                        break;
//                    case Constants.PositionSlin:
//                        fragmentSlin.removeAdFragment();
//                        break;
//                    case Constants.PositionCslin:
//                        fragmentCslin.removeAdFragment();
//                        break;
//                    case Constants.PositionCpw:
//                        fragmentCpw.removeAdFragment();
//                        break;
//                    case Constants.PositionGcpw:
//                        fragmentGcpw.removeAdFragment();
//                        break;
//                    case Constants.PositionCoax:
//                        fragmentCoax.removeAdFragment();
//                        break;
//                    default:
//                        break;
//                    }
//                    invalidateOptionsMenu();
//                }
//            }
//        }
//    }

//    @Override
//    public BillingManager getBillingManager() {
//        return null;
//    }

}
