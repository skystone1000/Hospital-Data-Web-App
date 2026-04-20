package com.example.patientrecords.ui.base

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import com.example.patientrecords.MainActivity
import com.example.patientrecords.R
import com.example.patientrecords.databinding.ActivityBaseBinding
import com.example.patientrecords.ui.addpatient.AddPatientActivity
import com.example.patientrecords.ui.dashboard.DashboardActivity
import com.example.patientrecords.ui.login.LoginActivity
import com.example.patientrecords.ui.settings.SettingsActivity
import com.example.patientrecords.ui.viewallpatient.ViewAllPatientsActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var baseBinding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        super.setContentView(baseBinding.root)
        applyWindowInsets()
    }

    protected fun setChildContentView(childView: View) {
        baseBinding.baseContainer.addView(childView)
    }

    protected fun initToolbarWithDrawer() {
        toolbar = baseBinding.toolbar
        drawerLayout = baseBinding.drawerLayout
        navView = baseBinding.navView

        setSupportActionBar(toolbar)
        val contentColor = ContextCompat.getColor(this, R.color.toolbar_content_color)
        val toggleDrawable = DrawerArrowDrawable(this).apply {
            color = contentColor
        }
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle.drawerArrowDrawable = toggleDrawable
        toolbar.setTitleTextColor(contentColor)
        toolbar.setNavigationIcon(R.drawable.baseline_menu_24_white)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toolbar.navigationIcon?.setTint(contentColor)

        // Navigation Drawer width — capped at 50% of screen width to avoid covering full portrait screens
        val layoutParams = navView.layoutParams
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        layoutParams.width = (screenWidth * 0.50).toInt()
        navView.layoutParams = layoutParams

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navigateTo(MainActivity::class.java)
            R.id.nav_add_patient -> navigateTo(AddPatientActivity::class.java)
            R.id.nav_view_patients -> navigateTo(ViewAllPatientsActivity::class.java)
            R.id.nav_dashboard -> navigateTo(DashboardActivity::class.java)
            R.id.nav_settings -> navigateTo(SettingsActivity::class.java)
            R.id.nav_logout -> {
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Skip relaunch if already on this screen; bring existing instance forward otherwise
    private fun navigateTo(destination: Class<out Activity>) {
        if (this::class.java == destination) return
        startActivity(Intent(this, destination).apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        })
    }

    // Close drawer on back press if open; otherwise propagate to system
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Route hamburger icon tap through toggle; other menu items fall through to super
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

    // Apply status-bar (top) and nav-bar (bottom) insets.
    // The toolbar absorbs the status-bar height by growing via paddingTop, so its
    // colorPrimary background fills the status-bar area and title/icons sit below it.
    // baseContainer gets paddingBottom equal to the nav-bar height so the last item
    // in any scrollable child is never hidden behind the system navigation bar.
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(baseBinding.root) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            baseBinding.toolbar.updatePadding(top = statusBarHeight)
            baseBinding.baseContainer.updatePadding(bottom = navBarHeight)
            insets
        }
        ViewCompat.requestApplyInsets(baseBinding.root)
    }

    fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
