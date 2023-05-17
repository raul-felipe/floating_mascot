package com.example.floatingmascot

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class LightManager (private val activity: AppCompatActivity, private val floatingService: FloatingService) :
    SensorEventListener {

    private var sensorManager: SensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var light: Sensor? = null
    private var gifMap : GifMap = GifMap()

    init {
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event!!.values[0]>300 && gifMap.hasPriority(floatingService.getGif(),gifMap.LIGHT))
            floatingService.updateGif(gifMap.LIGHT)
        else if(event!!.values[0]<5 && gifMap.hasPriority(floatingService.getGif(),gifMap.DARK))
            floatingService.updateGif(gifMap.DARK)
        else if(floatingService.isSameBackgroud(gifMap.LIGHT)||floatingService.isSameBackgroud(gifMap.DARK))
            floatingService.updateGif(gifMap.START)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("Create", accuracy.toString())
    }

    fun listener () : Boolean {
        return sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
    }

}