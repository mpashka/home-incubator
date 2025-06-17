package com.example.btgsmgateway

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.Menu
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.btgsmgateway.databinding.ActivityMainBinding
import java.util.Arrays

private const val LOG_TAG = "BluetoothProfile"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener(::onBarClick)
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

    val profileUUIDs = mapOf(
        "0000110d-0000-1000-8000-00805f9b34fb" to "A2DP",
        "00001108-0000-1000-8000-00805f9b34fb" to "Headset",
        "0000111e-0000-1000-8000-00805f9b34fb" to "Hands-Free",
        "00001132-0000-1000-8000-00805f9b34fb" to "MAP Message Notification Server",
        "00001133-0000-1000-8000-00805f9b34fb" to "MAP Message Access Server",
        "00001105-0000-1000-8000-00805f9b34fb" to "OBEX Object Push",
        "00001106-0000-1000-8000-00805f9b34fb" to "FTP",
        "00001124-0000-1000-8000-00805f9b34fb" to "HID (Human Interface Device)",
    )

    fun onBarClick(view: View) {
        onBarClickGPT(view)
    }

    fun onBarClickSO(view: View) {

    }

    fun onBarClickGPT(view: View) {
        /*
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show()
        */

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (BluetoothDevice.ACTION_UUID == intent.action) {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    if (uuids != null) {
                        for (parcel in uuids) {
                            val uuid = (parcel as ParcelUuid).uuid.toString()
                            val profile = profileUUIDs[uuid] ?: "Unknown: $uuid"
                            Log.d(LOG_TAG, "${device.name} поддерживает $profile")
                        }
                    } else {
                        Log.d(LOG_TAG, "UUIDs не получены для ${device.name}")
                    }
                }
            }
        }

        var context = applicationContext
        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)
        context.registerReceiver(receiver, filter)

//        var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        var bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothAdapter = bluetoothManager.adapter
        for (device in bluetoothAdapter.bondedDevices) {
//            val uuids = device.uuids.map { d -> d.uuid }
            Log.i(LOG_TAG, "Device: ${device.name}, Alias: ${device.alias}, Type: ${device.type}, " +
                    "Address: ${device.address}, UUIDs: ${Arrays.toString(device.uuids)}, " +
                    "Class: ${device.bluetoothClass}, " +
                    "State: ${device.bondState}")
            // type: 1==DEVICE_TYPE_CLASSIC
            // address: 80:61:8F:7C:13:87
            // class: 5a0204
            //  TELEPHONY = 0x400000
            //  OBJECT_TRANSFER = 0x100000
            //  CAPTURE = 0x080000
            //  NETWORKING = 0x020000
            //  PHONE = 0x0200
            //  PHONE_CELLULAR = 0x0204
            // State: 12
            //  BOND_BONDED = 12
            // Profiles:
            //  0000110a-0000-1000-8000-00805f9b34fb, 00001105-0000-1000-8000-00805f9b34fb, // ? "OBEX Object Push"
            //  00001112-0000-1000-8000-00805f9b34fb, 0000111f-0000-1000-8000-00805f9b34fb,
            //  00000000-0000-1000-8000-00805f9b34fb, 00000000-0000-1000-8000-00805f9b34fb
//            device.name

        }

//        val device: BluetoothDevice = /* ваш выбранный подключённый девайс */
//            device.fetchUuidsWithSdp() // Инициирует запрос UUID'ов

    }
}