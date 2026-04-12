package com.example.patientrecords.ui.base

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
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
        applyTopPaddingToRoot()
    }

    protected fun setChildContentView(childView: View) {
        baseBinding.baseContainer.addView(childView)
    }

    protected fun initToolbarWithDrawer() {
        toolbar = baseBinding.toolbar
        drawerLayout = baseBinding.drawerLayout
        navView = baseBinding.navView

        // Toolbar and Toggle — white arrow drawable matches the app's theme colour
        setSupportActionBar(toolbar)
        val toggleDrawable = DrawerArrowDrawable(this).apply {
            color = ContextCompat.getColor(this@BaseActivity, R.color.white)
        }
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle.drawerArrowDrawable = toggleDrawable
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.setNavigationIcon(R.drawable.baseline_menu_24_white)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))

        // Navigation Drawer width — capped at 50% of screen width to avoid covering full portrait screens
        val layoutParams = navView.layoutParams
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        layoutParams.width = (screenWidth * 0.50).toInt()
        navView.layoutParams = layoutParams

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.nav_add_patient -> startActivity(Intent(this, AddPatientActivity::class.java))
            R.id.nav_view_patients -> startActivity(Intent(this, ViewAllPatientsActivity::class.java))
            R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_logout -> {
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
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

    // Set notification bar padding — offsets content below the transparent status bar
    private fun applyTopPaddingToRoot() {
        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top
            view.updatePadding(top = statusBarHeight)
            insets
        }
    }

    fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
