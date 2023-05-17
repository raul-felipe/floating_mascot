package com.example.floatingmascot

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices


class LocationManager(private val activity: AppCompatActivity) {

    private lateinit var weatherManager: WeatherManager

    @SuppressLint("MissingPermission")
    fun getLocationAndWeatherInformation( highTemperature: Int, lowTemperature: Int, floatingService: FloatingService){
        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->

            weatherManager = WeatherManager("${location?.latitude},${location?.longitude}",activity)
            weatherManager.getData(highTemperature,lowTemperature,floatingService)
        }
    }

    fun hasLocationParmission() :Boolean{
        try {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),101)
            }
            else return true
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) return true
            return false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}