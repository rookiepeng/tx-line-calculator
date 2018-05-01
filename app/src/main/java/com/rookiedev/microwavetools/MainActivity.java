package com.rookiedev.microwavetools;

import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.crash.FirebaseCrash;
import com.rookiedev.microwavetools.fragments.CmlinFragment;
import com.rookiedev.microwavetools.fragments.CoaxFragment;
import com.rookiedev.microwavetools.fragments.CpwFragment;
import com.rookiedev.microwavetools.fragments.CslinFragment;
import com.rookiedev.microwavetools.fragments.MlinFragment;
import com.rookiedev.microwavetools.fragments.SlinFragment;
import com.rookiedev.microwavetools.libs.Constants;
import com.rookiedev.microwavetools.util.IabBroadcastReceiver;
import com.rookiedev.microwavetools.util.IabHelper;
import com.rookiedev.microwavetools.util.IabResult;
import com.rookiedev.microwavetools.util.Inventory;
import com.rookiedev.microwavetools.util.Purchase;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IabBroadcastReceiver.IabBroadcastListener {

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    private final static String SKU_ADFREE = "com.rookiedev.rfline.adfree.v1";
    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;
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
    // The helper object
    private IabHelper mHelper;
    private boolean isAdFree = false;
    private boolean isChecked = false;
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) {
                return;
            }

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                isChecked = true;
                complain("Error purchasing. Authenticity verification failed.");
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
                return;
            }

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
    };

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) {
                return;
            }

            // Is it a failure?
            if (result.isFailure()) {
                isChecked = true;
                complain("Failed to query inventory: " + result);
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
                return;
            }

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_ADFREE);
            isChecked = true;
            isAdFree = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            if (isAdFree) {
                invalidateOptionsMenu();
            } else {
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
    };

    // Verifies the developer payload of a purchase.
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }

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
        // getMenuInflater().inflate(R.menu.main, menu);
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
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, AppCompatActivity.MODE_PRIVATE);// get the header_parameters from the Shared
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

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    void complain(String message) {
        FirebaseCrash.logcat(Log.ERROR, "PURCHASE", message);
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    // User clicked the "Ad free" button
    public void onAdfreeButtonClicked() {
        if (pos == 0) {
            fragmentMlin.addAdFragment();
        }
        String payload = "";
        try {
            mHelper.launchPurchaseFlow(this, SKU_ADFREE, RC_REQUEST, mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null)
            return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            FirebaseCrash.logcat(Log.VERBOSE, "PURCHASE", "onActivityResult handled by IABUtil.");
            //Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

}
