package com.example.patientrecords.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import com.example.patientrecords.R
import com.example.patientrecords.databinding.ActivityBaseBinding
import com.example.patientrecords.ui.addpatient.AddPatientActivity
import com.example.patientrecords.ui.viewallpatient.ViewAllPatientsActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.DrawerLayoutUtils
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity(@LayoutRes private val layoutResId: Int) : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)

        applyTopPaddingToRoot()
    }

    protected fun initToolbarWithDrawer() {
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)

        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            onDrawerItemSelected(menuItem)
            drawerLayout.closeDrawers()
            true
        }
    }

    protected fun onDrawerItemSelected(item: MenuItem) {
        // Child activities can override this
        when (item.itemId) {
            R.id.nav_home -> {
                // Already in home, no action needed
            }
            R.id.nav_add_patient -> {
                startActivity(Intent(this, AddPatientActivity::class.java))
            }
            R.id.nav_view_patients -> {
                startActivity(Intent(this, ViewAllPatientsActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already in home, no action needed
            }
            R.id.nav_add_patient -> {
                startActivity(Intent(this, AddPatientActivity::class.java))
            }
            R.id.nav_view_patients -> {
                startActivity(Intent(this, ViewAllPatientsActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Handle back press for drawer
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Handle toolbar toggle click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun applyTopPaddingToRoot() {
        // Set Notification bar padding
        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top
            view.updatePadding(top = statusBarHeight)
            // view.setPadding(0, statusBarHeight, 0, 0)
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
