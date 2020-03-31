package com.ccdc.vibrator

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import ccdc.lib.customvibrator.CustomVibration

class MyVibrator(val vibrator: Vibrator, val mDataset : MutableList<CustomVibration>) {
    lateinit var timingsArrayAll : MutableList<Long>
    lateinit var amplitudeArrayAll : MutableList<Int>
    val bps = 50
    fun vibrate(){
        if(mDataset.isEmpty()){
            return
        }
        timingsArrayAll = mutableListOf()
        amplitudeArrayAll = mutableListOf()
        //dataset에 맞춰서 setVibrationEffect List _완
        for (shot in mDataset){
            timingsArrayAll.addAll(shot.getTimingsArray())
            amplitudeArrayAll.addAll(shot.getAmplitudesArray())
        }

        if(amplitudeArrayAll.size == 0){
            Log.d("size", "something wrong")
            return
        }
        //Log.v("durationArrayAll",timingsArrayAll.toString())
        //Log.v("amplitudeArrayAll",amplitudeArrayAll.toString())
        if(!smoothing()){
            Log.d("smoothing", "something wrong")
            return
        }
        //Log.v("durationArrayAll",timingsArrayAll.toString())
       //Log.v("amplitudeArrayAll",amplitudeArrayAll.toString())
        vibrator.vibrate(VibrationEffect.createWaveform(timingsArrayAll.toLongArray(),amplitudeArrayAll.toIntArray(),-1))

    }


    fun hasAmplitudeControl(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.vibrator.hasAmplitudeControl()
        } else {
            return false
        }
    }

    fun hasVibrator(): Boolean {
        return this.vibrator.hasVibrator()
    }

    fun cancel() {
        this.vibrator.cancel()
    }
    /*
        1. "PIANO"       -> soft          -> red
        2. "FORTE"       -> loud          -> blue
        3. "CRESCENDO"   -> growing       -> green
        4. "DECRESCENDO" -> decreasing    -> black
        ''sc'' -> staccato  ex) scPIANO -> soft staccato
        1 Shot divided for 25
     */

    private fun smoothing(): Boolean {
        if (timingsArrayAll.size != amplitudeArrayAll.size || timingsArrayAll.size < 2){
            return false
        }
        var i = 1
        while(i < amplitudeArrayAll.size){
            if(amplitudeArrayAll[i] != amplitudeArrayAll[i-1]) {
                if(amplitudeArrayAll[i] != 0 && amplitudeArrayAll[i-1]!=0){
                    amplitudeArrayAll.add(i, 0)
                    timingsArrayAll.add(i, 0)
                    i += 1
                }
            }else{
                amplitudeArrayAll.removeAt(i)
                timingsArrayAll[i-1] = timingsArrayAll[i] + timingsArrayAll[i-1]
                timingsArrayAll.removeAt(i)
                i -= 1
            }
            i += 1
        }
        return true
    }

}