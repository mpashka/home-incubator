package org.mpashka.hass.charger.tuya

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.mpashka.hass.charger.tuya.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            // Start battery monitoring work
            BatteryMonitorManager.startMonitoring(this)
            Snackbar.make(view, "Battery monitoring started", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // Initialize Tuya IoT and start battery monitoring
        initializeTuyaAndStartMonitoring()
    }
    
    private fun initializeTuyaAndStartMonitoring() {
        lifecycleScope.launch {
            // Initialize Tuya IoT integration
            val tuyaInitialized = BatteryMonitorManager.initializeTuya(this@MainActivity)
            
            if (tuyaInitialized) {
                Snackbar.make(
                    binding.root,
                    "Tuya IoT integration initialized successfully",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    binding.root,
                    "Failed to initialize Tuya IoT integration",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            
            // Start battery monitoring work
            startBatteryMonitoring()
        }
    }
    
    private fun startBatteryMonitoring() {
        BatteryMonitorManager.startMonitoring(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cleanup Tuya IoT resources
        BatteryMonitorManager.cleanupTuya()
    }
}