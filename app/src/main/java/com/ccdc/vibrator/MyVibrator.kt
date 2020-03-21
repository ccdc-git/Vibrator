package com.ccdc.vibrator

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast

class MyVibrator(val vibrator: Vibrator, val mDataset : MutableList<OneShot>) {
    lateinit var durationArrayAll : MutableList<Long>
    lateinit var amplitudeArrayAll : MutableList<Int>
    val bps = 50
    fun vibrate(){
        if(mDataset.isEmpty()){
            return
        }
        durationArrayAll = mutableListOf()
        amplitudeArrayAll = mutableListOf()
        //dataset에 맞춰서 setVibrationEffect List _완
        for (shot in mDataset){
            durationArrayAll.addAll(makeDurationArray(shot))
            amplitudeArrayAll.addAll(makeAmplitudeArray(shot))
        }

        if(amplitudeArrayAll.size == 0){
            Log.d("size", "something wrong")
            return
        }
        Log.v("durationArrayAll",durationArrayAll.toString())
        Log.v("amplitudeArrayAll",amplitudeArrayAll.toString())
        if(!smoothing()){
            Log.d("smoothing", "something wrong")
            return
        }
        Log.v("durationArrayAll",durationArrayAll.toString())
        Log.v("amplitudeArrayAll",amplitudeArrayAll.toString())
        vibrator.vibrate(VibrationEffect.createWaveform(durationArrayAll.toLongArray(),amplitudeArrayAll.toIntArray(),-1))

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
    private fun makeDurationArray(shot: OneShot): List<Long> {
        return List<Long>(bps) {(shot.duration / bps).toLong()}
    }
    private fun makeAmplitudeArray(shot: OneShot): List<Int>{
        return if(shot.isStaccato){
            when (shot.codeName) {
                "piano" -> List<Int>(bps) { i -> if (i.rem(2) == 0) 30  else 0 }
                "forte" -> List<Int>(bps) { i -> if (i.rem(2) == 0) 255  else 0 }
                "crescendo" -> List<Int>(bps) { i -> if (i.rem(2) == 0) (i+1) * 255 / (bps+1)   else 0 }
                "decrescendo" -> List<Int>(bps) { i -> if (i.rem(2) == 0) (bps  - i) * 255 / bps  else 0 }
                else -> List<Int>(0) { 0 }

            }
        }
        else {
             when (shot.codeName) {
                "piano" -> List<Int>(bps){30}
                "forte" -> List<Int>(bps){255}
                "crescendo" -> List<Int>(bps) { i -> (i+1) * 255 / (bps+1)  }
                "decrescendo" -> List<Int>(bps) { i ->(bps  - i) * 255 / bps }
                else -> List<Int>(0) { 0 }
            }
        }
    }

    private fun smoothing(): Boolean {
        if (durationArrayAll.size != amplitudeArrayAll.size || durationArrayAll.size < 2){
            return false
        }
        var i = 1
        while(i < amplitudeArrayAll.size){
            if(amplitudeArrayAll[i] != amplitudeArrayAll[i-1]) {
                if(amplitudeArrayAll[i] != 0 && amplitudeArrayAll[i-1]!=0){
                    amplitudeArrayAll.add(i, 0)
                    durationArrayAll.add(i, 0)
                    i += 1
                }
            }else{
                amplitudeArrayAll.removeAt(i)
                durationArrayAll[i-1] = durationArrayAll[i] + durationArrayAll[i-1]
                durationArrayAll.removeAt(i)
                i -= 1
            }
            i += 1
        }
        return true
    }

}