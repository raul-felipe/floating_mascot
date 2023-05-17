package com.example.floatingmascot

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class TemperatureManager(private val activity: AppCompatActivity, private val floatingService: FloatingService, private val lowTemperature :Int, private val highTemperature:Int) : SensorEventListener {

    private var sensorManager: SensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var temperature: Sensor? = null
    private var gifMap : GifMap = GifMap()

    init {
        temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event!!.values[0]>highTemperature && !floatingService.isSameBackgroud(gifMap.HOT))
            floatingService.updateGif(gifMap.HOT)
        else if(event!!.values[0]<=lowTemperature && !floatingService.isSameBackgroud(gifMap.COLD))
            floatingService.updateGif(gifMap.COLD)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("Create", accuracy.toString())
    }

    fun listener () : Boolean {
        return sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL)
    }

}