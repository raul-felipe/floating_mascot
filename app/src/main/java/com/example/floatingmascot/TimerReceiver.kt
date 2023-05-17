package com.example.floatingmascot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import java.util.Calendar

class TimerReceiver(var context: Context,private val floatingService: FloatingService,
                    var startLunchTime : Int,var endLunchTime : Int, var startSleepTime : Int,var endSleepTime : Int): BroadcastReceiver() {

    private var gifMap:GifMap = GifMap()

    fun updateTime(startLunchTime : Int,endLunchTime : Int, startSleepTime : Int,endSleepTime : Int){
        this.startLunchTime = startLunchTime
        this.endLunchTime = endLunchTime
        this.startSleepTime = startSleepTime
        this.endSleepTime = endSleepTime
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        var actualHour : Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        //lunch time
        if (actualHour in startLunchTime until endLunchTime && gifMap.hasPriority(floatingService.getGif(),gifMap.LUNCH))
            floatingService.updateGif(gifMap.LUNCH)
        else if(floatingService.getGif()==gifMap.LUNCH)
            floatingService.updateGif(gifMap.START)
        //sleep time
        if(actualHour < 12) actualHour += 24
        if(startSleepTime <16) startSleepTime+=24
        if(endSleepTime < 16) endSleepTime+=24
        Log.d("CREATED", "$actualHour, $startSleepTime, $endSleepTime")
        if (actualHour in startSleepTime until endSleepTime && gifMap.hasPriority(floatingService.getGif(),gifMap.SLEEP))
            floatingService.updateGif(gifMap.SLEEP)
        else if(floatingService.getGif()==gifMap.SLEEP)
            floatingService.updateGif(gifMap.START)

    }
}