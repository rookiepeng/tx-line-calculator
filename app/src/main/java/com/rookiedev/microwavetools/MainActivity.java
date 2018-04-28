package com.rookiedev.microwavetools;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rookiedev.microwavetools.fragments.CmlinFragment;
import com.rookiedev.microwavetools.fragments.CoaxFragment;
import com.rookiedev.microwavetools.fragments.CpwFragment;
import com.rookiedev.microwavetools.fragments.CslinFragment;
import com.rookiedev.microwavetools.fragments.MlinFragment;
import com.rookiedev.microwavetools.fragments.SlinFragment;
import com.rookiedev.microwavetools.libs.Constant;
import com.rookiedev.microwavetools.util.IabBroadcastReceiver;
import com.rookiedev.microwavetools.util.IabHelper;
import com.rookiedev.microwavetools.util.IabResult;
import com.rookiedev.microwavetools.util.Inventory;
import com.rookiedev.microwavetools.util.Purchase;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IabBroadcastReceiver.IabBroadcastListener {

    private int pos;
    private DrawerLayout drawer;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Fragment fragment = null;
    //private Fragment adFragment = null;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private FragmentManager fragmentManager;

    // The helper object
    IabHelper mHelper;
    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;
    private final static String SKU_ADFREE = "com.rookiedev.rfline.adfree.v1";
    private boolean isAdFree = false;
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            //Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) {
                return;
            }

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                //setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                //setWaitScreen(false);
                //adFragment = new AdFragment();
                //fragmentManager.beginTransaction().replace(R.id.ad_frame, adFragment).commit();
                return;
            }

            //Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_ADFREE)) {
                // bought 1/4 tank of gas. So consume it.
                //Log.d(TAG, "Purchase is gas. Starting gas consumption.");
                isAdFree = true;
                //if (adFragment != null) {
                //    fragmentManager.beginTransaction().remove(adFragment).commit();
                //}
                invalidateOptionsMenu();
            }
        }
    };

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            //Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) {
                return;
            }

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                //adFragment = new AdFragment();
                //fragmentManager.beginTransaction().replace(R.id.ad_frame, adFragment).commit();
                return;
            }

            //Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_ADFREE);
            isAdFree = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            if (isAdFree) {
                invalidateOptionsMenu();
            } else {
                //adFragment = new AdFragment();
                //fragmentManager.beginTransaction().replace(R.id.ad_frame, adFragment).commit();
            }
            //Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

            // First find out which subscription is auto renewing
        }
    };

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        mCollapsingToolbarLayout=(CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        fragmentManager = getSupportFragmentManager();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                if (fragment != null) {
                    mCollapsingToolbarLayout.setTitle(navigationView.getMenu().getItem(pos).getTitle());

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
                    transaction.replace(R.id.content_frame, fragment);
                    // transaction.addToBackStack(null);
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
        mCollapsingToolbarLayout.setTitle(navigationView.getMenu().getItem(pos).getTitle());
        initFragment(pos);

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs5JW1lae1XD7lDALy5eBi3G7X06jPSNVaOrdDp1ACV3vmt0LPF4nnSGDjkMxwhyCge+u8r4trnwJoOANgqxyDxq9icRXHGoZaZnjgHtUwGfZQYlwFIczpRbNkOSFe4/hiyWRDh9f4s/oSyoHO/yWtSrLHplabMQtg+CxU4IAC6Xym3gn8laPDUV6M/Fjsrv3t9ntKJIBGhX0S7ogrWTuLJU9hGTLIcPIR2WFtALYyX/AqlGKFk3KzYZ2hvoSfKnOPnFdswJYacr8aY7Y+vWG4Qz9LgPEM3iA15Lm7PxBd9r/VtcMn75cnuhMbAHKR8YEEjHk2gla4PaofedgwUstawIDAQAB";

        // Create the helper, passing it our context and the public key to verify signatures with
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        //Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null)
                    return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });

        if (isFirstRun()) {
            drawer.openDrawer(GravityCompat.START);
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
        getMenuInflater().inflate(R.menu.action_menu, menu);
        //getMenuInflater().inflate(R.menu.main, menu);
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
        SharedPreferences prefs = getSharedPreferences(Constant.SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Constant.PREFS_POSITION, String.valueOf(pos));
        editor.apply();
        // saveSharedPref();
    }

    private void readSharedPref() {
        SharedPreferences prefs = getSharedPreferences(Constant.SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
        pos = Integer.parseInt(prefs.getString(Constant.PREFS_POSITION, "0"));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //fragmentManager.beginTransaction().remove(fragment).commit();
        if (id == R.id.nav_mlin) {
            fragment = new MlinFragment();
            pos = 0;
        } else if (id == R.id.nav_cmlin) {
            fragment = new CmlinFragment();
            pos = 1;
        } else if (id == R.id.nav_slin) {
            fragment = new SlinFragment();
            pos = 2;
        } else if (id == R.id.nav_cslin) {
            fragment = new CslinFragment();
            pos = 3;
        } else if (id == R.id.nav_cpwg) {
            fragment = new CpwFragment();
            pos = 4;
        } else if (id == R.id.nav_coax) {
            fragment = new CoaxFragment();
            pos = 5;
        } else {
            fragment = new MlinFragment();
        }
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void initFragment(int item) {
        if (item == 0) {
            fragment = new MlinFragment();
            pos = 0;
        } else if (item == 1) {
            fragment = new CmlinFragment();
            pos = 1;
        } else if (item == 2) {
            fragment = new SlinFragment();
            pos = 2;
        } else if (item == 3) {
            fragment = new CslinFragment();
            pos = 3;
        } else if (item == 4) {
            fragment = new CpwFragment();
            pos = 4;
        } else if (item == 5) {
            fragment = new CoaxFragment();
            pos = 5;
        } else {
            fragment = new MlinFragment();
        }
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(Constant.SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE);
        if (prefs.getString(Constant.PREFS_ISFIRSTRUN, "true").equals("true")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constant.PREFS_ISFIRSTRUN, "false");
            editor.apply();
            return true;
        } else {
            return false;
        }
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        //Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    void complain(String message) {
        //Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        //Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    // User clicked the "Buy Gas" button
    public void onAdfreeButtonClicked() {
        //Log.d(TAG, "Buy gas button clicked.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, SKU_ADFREE, RC_REQUEST, mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
            //setWaitScreen(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null)
            return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            //Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

}
