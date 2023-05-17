package com.example.floatingmascot;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class GifMap {

    GifMap(){
        gifMap.put(CHARGING,100);
        gifMap.put(UNCHARGING,0);
        gifMap.put(START,0);
        gifMap.put(COLD,60);
        gifMap.put(HOT,60);
        gifMap.put(LIGHT,80);
        gifMap.put(DARK,80);
        gifMap.put(SLEEP,90);
        gifMap.put(LUNCH,90);
        gifMap.put(RAIN,85);
        //gifMap.put(CHARGING,0);
    }

    int CHARGING = R.drawable.hd_jolteon;
    int UNCHARGING = R.drawable.hd_eevee;
    int START = R.drawable.hd_eevee;
    int COLD = R.drawable.hd_glaceon;
    int HOT = R.drawable.hd_flareon;
    int LIGHT = R.drawable.hd_sylveon;
    int DARK = R.drawable.hd_umbreon;
    int SLEEP = R.drawable.hd_espeon;

    int RAIN = R.drawable.hd_vaporeon;
    int LUNCH = R.drawable.hd_leafeon;
    int CHARGING_OV = R.drawable.electricity;

    boolean hasPriority(int actualGif,int newGif){
        if(gifMap.get(newGif)>=gifMap.get(actualGif))
            return true;
        return false;
    }

    Map<Integer,Integer> gifMap = new HashMap<>();



}
