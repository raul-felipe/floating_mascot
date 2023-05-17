package com.example.floatingmascot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager


class PowerConnectionReceiver(private val floatingService: FloatingService) : BroadcastReceiver() {

    private var gifMap : GifMap = GifMap()

    override fun onReceive(context: Context, intent: Intent) {

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        print(status)
        if(floatingService.isShowing){
            //is charging
            if (status == BatteryManager.BATTERY_STATUS_CHARGING && gifMap.hasPriority(floatingService.getGif(),gifMap.CHARGING)) {
                floatingService.updateGif(gifMap.CHARGING)
                floatingService.updateOverlayGif(gifMap.CHARGING_OV)
            }
            if(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING && floatingService.getGif()==gifMap.CHARGING) {
                floatingService.updateGif(gifMap.UNCHARGING)
                floatingService.updateOverlayGif(android.R.color.transparent)
            }
        }

    }
}