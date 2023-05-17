package com.example.floatingmascot

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.RangeSlider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var floatingService: FloatingService
    private lateinit var batteryReceiver : PowerConnectionReceiver
    private lateinit var timerReceiver: TimerReceiver
    private lateinit var lightManager: LightManager
    private lateinit var locationManager: LocationManager

    lateinit var showButton: Button
    lateinit var removeButton: Button
    lateinit var updateButton: Button

    lateinit var temperatureSlider: RangeSlider
    lateinit var lunchTimeSlider:RangeSlider
    lateinit var sleepTimeSlider:RangeSlider

    private val storageService = StorageService()

    private var lowTemperature : Int =0
    private var highTemperature : Int =0
    private var startLunchTime : Int =0
    private var endLunchTime : Int =0
    private var startSleepTime : Int =0
    private var endSleepTime : Int =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        showButton = findViewById<Button>(R.id.showButton)
        removeButton= findViewById<Button>(R.id.removeButton)
        updateButton = findViewById<Button>(R.id.updateButton)

        temperatureSlider = findViewById<RangeSlider>(R.id.temperature_slider)
        lunchTimeSlider = findViewById<RangeSlider>(R.id.lunch_time_slider)
        sleepTimeSlider = findViewById<RangeSlider>(R.id.sleep_time_slider)

        loadDataPreferences()

        showButton.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                displayMissingOverlayPermissionDialog()
            } else {
                startForeground()
            }
        }

        updateButton.setOnClickListener{
            startServices()
        }

        removeButton.setOnClickListener {
            floatingService.dismiss()
        }


        temperatureSlider.addOnChangeListener(RangeSlider.OnChangeListener { slider, value, fromUser ->
            lowTemperature = slider.values[0].toInt()
            highTemperature = slider.values[1].toInt()
        })
        temperatureSlider.setLabelFormatter { value ->
            "${value}ºC"
        }
        lunchTimeSlider.addOnChangeListener(RangeSlider.OnChangeListener { slider, value, fromUser ->
            startLunchTime = slider.values[0].toInt()
            endLunchTime = slider.values[1].toInt()
        })
        lunchTimeSlider.setLabelFormatter { value ->
            "${value}h"
        }
        sleepTimeSlider.addOnChangeListener(RangeSlider.OnChangeListener { slider, value, fromUser ->
            startSleepTime = slider.values[0].toInt()
            endSleepTime = slider.values[1].toInt()
        })
        sleepTimeSlider.setLabelFormatter { value ->
            if (value >=24) "${value-24}h" else "${value}h"
        }

    }

    override fun onResume() {
        super.onResume()
    }


    private fun loadDataPreferences(){
        runBlocking {
            withContext(Dispatchers.IO) {
                lowTemperature = storageService.getStorageValue(this@MainActivity,storageService.COLD_TEMPERATURE)
                highTemperature = storageService.getStorageValue(this@MainActivity,storageService.HOT_TEMPERATURE)
                temperatureSlider.setValues(lowTemperature.toFloat(),highTemperature.toFloat())

                startLunchTime = storageService.getStorageValue(this@MainActivity,storageService.START_LUNCH_TIME)
                endLunchTime = storageService.getStorageValue(this@MainActivity,storageService.END_LUNCH_TIME)
                if(startLunchTime==0&&endLunchTime==0){
                    startLunchTime=12
                    endLunchTime=13
                }
                lunchTimeSlider.setValues(startLunchTime.toFloat(),endLunchTime.toFloat())

                startSleepTime = storageService.getStorageValue(this@MainActivity,storageService.START_SLEEP_TIME)
                endSleepTime = storageService.getStorageValue(this@MainActivity,storageService.END_SLEEP_TIME)
                if(startSleepTime==0&&endSleepTime==0){
                    startSleepTime=23
                    endSleepTime=30
                }
                sleepTimeSlider.setValues(startSleepTime.toFloat(),endSleepTime.toFloat())
            }
        }
    }

    private fun startForeground(){
        if(isMyServiceRunning(FloatingService::class.java)){
            stopService(Intent(this,FloatingService::class.java))
        }
        bindService(Intent(this,FloatingService::class.java),connection, Context.BIND_AUTO_CREATE)
        startService(Intent(this,FloatingService::class.java))
    }

    var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
        }
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            floatingService = (service as FloatingService.FloatingBinder).getService()
            startServices()
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun startServices(){
        batteryReceiver = PowerConnectionReceiver(floatingService)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        timerReceiver = TimerReceiver(this,floatingService,startLunchTime,endLunchTime,startSleepTime,endSleepTime)
        registerReceiver(timerReceiver, IntentFilter(Intent.ACTION_TIME_TICK))

        lightManager = LightManager(this,floatingService)
        if(this::lightManager.isInitialized) {
            lightManager.listener()
        }

        locationManager = LocationManager(this)
        if(locationManager.hasLocationParmission())
            locationManager.getLocationAndWeatherInformation(highTemperature, lowTemperature, floatingService)
        timerReceiver.updateTime(startLunchTime,endLunchTime,startSleepTime,endSleepTime)
        runBlocking {
            withContext(Dispatchers.IO){
                storageService.storageValue(this@MainActivity,storageService.COLD_TEMPERATURE,lowTemperature as Int)
                storageService.storageValue(this@MainActivity,storageService.HOT_TEMPERATURE,highTemperature as Int)
                storageService.storageValue(this@MainActivity,storageService.START_LUNCH_TIME,startLunchTime as Int)
                storageService.storageValue(this@MainActivity,storageService.END_LUNCH_TIME,endLunchTime as Int)
                storageService.storageValue(this@MainActivity,storageService.START_SLEEP_TIME,startSleepTime as Int)
                storageService.storageValue(this@MainActivity,storageService.END_SLEEP_TIME,endSleepTime as Int)
            }
        }
    }

    private fun displayMissingOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission required")
            .setMessage("This app required overlay permission to work, please allow this access")
            .setPositiveButton("allow") { _, _ -> requestOverlayPermission() }
            .setNegativeButton("Deny") { dialog, _ -> dialog.cancel() }
            .setCancelable(true)
            .show()
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }

}