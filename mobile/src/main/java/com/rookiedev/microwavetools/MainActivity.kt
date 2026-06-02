package com.rookiedev.microwavetools

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.rookiedev.microwavetools.fragments.CmlinFragment
import com.rookiedev.microwavetools.fragments.CoaxFragment
import com.rookiedev.microwavetools.fragments.CpwFragment
import com.rookiedev.microwavetools.fragments.CslinFragment
import com.rookiedev.microwavetools.fragments.MlinFragment
import com.rookiedev.microwavetools.fragments.SlinFragment
import com.rookiedev.microwavetools.libs.Constants

/**
 * Main activity for the Microwave Tools application.
 * Handles navigation and fragment transactions.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var pos = 0
    private var drawer: DrawerLayout? = null
    private var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var fragmentMlin: MlinFragment? = null
    private var fragmentCmlin: CmlinFragment? = null
    private var fragmentSlin: SlinFragment? = null
    private var fragmentCslin: CslinFragment? = null
    private var fragmentCpw: CpwFragment? = null
    private var fragmentGcpw: CpwFragment? = null
    private var fragmentCoax: CoaxFragment? = null
    private var navigationView: NavigationView? = null
    private var toggle: ActionBarDrawerToggle? = null
    private var fragmentManager: FragmentManager? = null
    private var imageModel: ImageView? = null
    private var imageResource = 0


    /**
     * Called when the activity is first created.
     * Initializes the UI components and sets up the navigation drawer.
     * 
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)

        //        setSupportActionBar(toolbar);
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@MainActivity).create()
            alertDialog.setTitle("Reset?")
            alertDialog.setMessage("Do you want to reset all the values?")
            alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "OK"
            ) { _, _ ->
                when (pos) {
                    Constants.PositionMlin -> fragmentMlin!!.resetValues()
                    Constants.PositionCmlin -> fragmentCmlin!!.resetValues()
                    Constants.PositionSlin -> fragmentSlin!!.resetValues()
                    Constants.PositionCslin -> fragmentCslin!!.resetValues()
                    Constants.PositionCpw -> fragmentCpw!!.resetValues()
                    Constants.PositionGcpw -> fragmentGcpw!!.resetValues()
                    Constants.PositionCoax -> fragmentCoax!!.resetValues()
                    else -> {}
                }
            }
            alertDialog.setButton(
                AlertDialog.BUTTON_NEGATIVE,
                "Cancel"
            ) { dialog, _ -> dialog!!.dismiss() }
            alertDialog.show()
        }

        navigationView = findViewById<NavigationView>(R.id.nav_view)
        checkNotNull(navigationView)
        navigationView!!.setNavigationItemSelectedListener(this)

        mCollapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)

        imageModel = findViewById<ImageView>(R.id.imageViewModel)

        fragmentManager = supportFragmentManager

        drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        toggle = object : ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
                val bundle = Bundle()
                val transaction: FragmentTransaction?
                when (pos) {
                    Constants.PositionCmlin -> {
                        fragmentCmlin!!.setArguments(bundle)
                        transaction = fragmentManager!!.beginTransaction()
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit)
                        transaction.replace(R.id.content_frame, fragmentCmlin!!)
                        transaction.commit()
                    }

                    Constants.PositionSlin -> {
                        fragmentSlin!!.setArguments(bundle)
                        transaction = fragmentManager!!.beginTransaction()
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit)
                        transaction.replace(R.id.content_frame, fragmentSlin!!)
                        transaction.commit()
                    }

                    Constants.PositionCslin -> {
                        fragmentCslin!!.setArguments(bundle)
                        transaction = fragmentManager!!.beginTransaction()
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit)
                        transaction.replace(R.id.content_frame, fragmentCslin!!)
                        transaction.commit()
                    }

                    Constants.PositionCpw -> {
                        bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW)
                        fragmentCpw!!.setArguments(bundle)
                        transaction = fragmentManager!!.beginTransaction()
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit)
                        transaction.replace(R.id.content_frame, fragmentCpw!!)
                        transaction.commit()
                    }

                    Constants.PositionGcpw -> {
                        bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW)
                        fragmentGcpw!!.setArguments(bundle)
                        transaction = fragmentManager!!.beginTransaction()
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit)
                        transaction.replace(R.id.content_frame, fragmentGcpw!!)
                        transaction.commit()
                    }

                    Constants.PositionCoax -> {
                        fragmentCoax!!.setArguments(bundle)
                        transaction = fragmentManager!!.beginTransaction()
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit)
                        transaction.replace(R.id.content_frame, fragmentCoax!!)
                        transaction.commit()
                    }

                    else -> {
                        fragmentMlin!!.setArguments(bundle)
                        transaction = fragmentManager!!.beginTransaction()
                        transaction.setCustomAnimations(R.anim.enter, R.anim.exit)
                        transaction.replace(R.id.content_frame, fragmentMlin!!)
                        transaction.commit()
                    }
                }

                mCollapsingToolbarLayout!!.title =
                    navigationView!!.menu.getItem(pos).title
                imageModel!!.setImageResource(imageResource)
            }
        }
        drawer!!.addDrawerListener(toggle!!)
        toggle!!.syncState()

        readSharedPref()

        navigationView!!.getMenu().getItem(pos).setChecked(true)
        initFragment(pos)
        mCollapsingToolbarLayout!!.title = navigationView!!.menu.getItem(pos).title
        imageModel!!.setImageResource(imageResource)


        if (this.isFirstRun) {
            drawer!!.openDrawer(GravityCompat.START)
        }
    }

    //    @Override
    //    public void onBackPressed() {
    //        DrawerLayout drawer = findViewById(R.id.drawer_layout);
    //        if (drawer.isDrawerOpen(GravityCompat.START)) {
    //            drawer.closeDrawer(GravityCompat.START);
    //        } else {
    //            super.onBackPressed();
    //        }
    //    }
    /**
     * Initializes the options menu.
     * 
     * @param menu The options menu in which you place your items.
     * @return true if the menu was successfully created.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_menu, menu)
        return true
    }

    /**
     * Handles item selections in the options menu.
     * 
     * @param item The menu item that was selected.
     * @return true if the item selection was handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action buttons
        if (item.getItemId() == R.id.menu_help) {
            val i = Intent(Intent.ACTION_SEND)
            i.setType("message/rfc822")
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("rookie.dev@gmail.com"))
            i.putExtra(Intent.EXTRA_SUBJECT, "")
            i.putExtra(Intent.EXTRA_TEXT, "")
            try {
                startActivity(Intent.createChooser(i, "Send email ..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, getResources().getString(R.string.noEmail), Toast.LENGTH_SHORT)
                    .show()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Called after onRestoreInstanceState(Bundle) when the activity is being re-initialized.
     * 
     * @param savedInstanceState The data most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle!!.syncState()
    }

    /**
     * Called by the system when the device configuration changes while your activity is running.
     * 
     * @param newConfig The new device configuration.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * Called when the activity is no longer visible to the user.
     * Saves the current position to shared preferences.
     */
    public override fun onStop() {
        super.onStop()
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString(Constants.PREFS_POSITION, pos.toString())
        editor.apply()
    }

    /**
     * Reads the shared preferences to get the last saved position.
     */
    private fun readSharedPref() {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        pos = prefs.getString(Constants.PREFS_POSITION, "0")!!.toInt()
    }

    /**
     * Handles navigation item selections.
     * 
     * @param item The selected menu item.
     * @return true if the item selection was handled.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val itemId = item.getItemId()
        if (itemId == R.id.nav_cmlin) {
            if (fragmentCmlin == null) {
                fragmentCmlin = CmlinFragment()
            }
            pos = Constants.PositionCmlin
            imageResource = R.drawable.vt_cmlin
        } else if (itemId == R.id.nav_slin) {
            if (fragmentSlin == null) {
                fragmentSlin = SlinFragment()
            }
            pos = Constants.PositionSlin
            imageResource = R.drawable.vt_slin
        } else if (itemId == R.id.nav_cslin) {
            if (fragmentCslin == null) {
                fragmentCslin = CslinFragment()
            }
            pos = Constants.PositionCslin
            imageResource = R.drawable.vt_cslin
        } else if (itemId == R.id.nav_cpw) {
            if (fragmentCpw == null) {
                val bundle = Bundle()
                bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW)
                fragmentCpw = CpwFragment()
                fragmentCpw!!.setArguments(bundle)
            }
            pos = Constants.PositionCpw
            imageResource = R.drawable.vt_cpw
        } else if (itemId == R.id.nav_gcpw) {
            if (fragmentGcpw == null) {
                val bundle = Bundle()
                bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW)
                fragmentGcpw = CpwFragment()
                fragmentGcpw!!.setArguments(bundle)
            }
            pos = Constants.PositionGcpw
            imageResource = R.drawable.vt_cpwg
        } else if (itemId == R.id.nav_coax) {
            if (fragmentCoax == null) {
                fragmentCoax = CoaxFragment()
            }
            pos = Constants.PositionCoax
            imageResource = R.drawable.vt_coax
        } else {
            if (fragmentMlin == null) {
                fragmentMlin = MlinFragment()
            }
            pos = Constants.PositionMlin
            imageResource = R.drawable.vt_mlin
        }

        drawer!!.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Initializes the fragment based on the selected item.
     * 
     * @param item The selected item position.
     */
    private fun initFragment(item: Int) {
        val bundle = Bundle()
        when (item) {
            Constants.PositionCmlin -> {
                fragmentCmlin = CmlinFragment()
                pos = Constants.PositionCmlin
                imageResource = R.drawable.vt_cmlin
                fragmentCmlin!!.setArguments(bundle)
                fragmentManager!!.beginTransaction().replace(R.id.content_frame, fragmentCmlin!!)
                    .commit()
            }

            Constants.PositionSlin -> {
                fragmentSlin = SlinFragment()
                pos = Constants.PositionSlin
                imageResource = R.drawable.vt_slin
                fragmentSlin!!.setArguments(bundle)
                fragmentManager!!.beginTransaction().replace(R.id.content_frame, fragmentSlin!!)
                    .commit()
            }

            Constants.PositionCslin -> {
                fragmentCslin = CslinFragment()
                pos = Constants.PositionCslin
                imageResource = R.drawable.vt_cslin
                fragmentCslin!!.setArguments(bundle)
                fragmentManager!!.beginTransaction().replace(R.id.content_frame, fragmentCslin!!)
                    .commit()
            }

            Constants.PositionCpw -> {
                bundle.putString(Constants.PARAMS_CPW, Constants.UNGROUNDED_CPW)
                fragmentCpw = CpwFragment()
                fragmentCpw!!.setArguments(bundle)
                pos = Constants.PositionCpw
                imageResource = R.drawable.vt_cpw
                fragmentManager!!.beginTransaction().replace(R.id.content_frame, fragmentCpw!!)
                    .commit()
            }

            Constants.PositionGcpw -> {
                bundle.putString(Constants.PARAMS_CPW, Constants.GROUNDED_CPW)
                fragmentGcpw = CpwFragment()
                fragmentGcpw!!.setArguments(bundle)
                pos = Constants.PositionGcpw
                imageResource = R.drawable.vt_cpwg
                fragmentManager!!.beginTransaction().replace(R.id.content_frame, fragmentGcpw!!)
                    .commit()
            }

            Constants.PositionCoax -> {
                fragmentCoax = CoaxFragment()
                pos = Constants.PositionCoax
                imageResource = R.drawable.vt_coax
                fragmentCoax!!.setArguments(bundle)
                fragmentManager!!.beginTransaction().replace(R.id.content_frame, fragmentCoax!!)
                    .commit()
            }

            else -> {
                fragmentMlin = MlinFragment()
                pos = Constants.PositionMlin
                imageResource = R.drawable.vt_mlin
                fragmentMlin!!.setArguments(bundle)
                fragmentManager!!.beginTransaction().replace(R.id.content_frame, fragmentMlin!!)
                    .commit()
            }
        }
    }

    private val isFirstRun: Boolean
        /**
         * Checks if this is the first run of the application.
         * 
         * @return true if this is the first run, false otherwise.
         */
        get() {
            val prefs = getSharedPreferences(
                Constants.SHARED_PREFS_NAME,
                MODE_PRIVATE
            )
            if (prefs.getString(
                    Constants.PREFS_ISFIRSTRUN,
                    "true"
                ) == "true"
            ) {
                val editor = prefs.edit()
                editor.putString(
                    Constants.PREFS_ISFIRSTRUN,
                    "false"
                )
                editor.apply()
                return true
            } else {
                return false
            }
        }
}
