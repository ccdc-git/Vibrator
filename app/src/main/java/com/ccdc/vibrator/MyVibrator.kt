package com.ccdc.vibrator

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast

class MyVibrator(val vibrator: Vibrator, val mDataset : MutableList<OneShot>) {
    lateinit var durationArrayAll : LongArray
    lateinit var amplitudeArrayAll : IntArray
    val bps = 50
    fun vibrate(){
        if(mDataset.isEmpty()){
            return
        }
        //dataset에 맞춰서 setVibrationEffect List _완
        durationArrayAll = LongArray(0) {0}
        amplitudeArrayAll = IntArray(0) {0}
        for (shot in mDataset){
            durationArrayAll = durationArrayAll.plus(makeDurationArray(shot))
            amplitudeArrayAll = amplitudeArrayAll.plus(makeAmplitudeArray(shot))
        }
        vibrator.vibrate(VibrationEffect.createWaveform(durationArrayAll,amplitudeArrayAll,-1))

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
    private fun makeDurationArray(shot: OneShot): LongArray {
        return when (shot.codeName) {
            "red", "blue" -> if(shot.isStaccato) LongArray(this.bps*2){ i -> if(i.rem(2) == 1) 0 else shot.duration / this.bps } else longArrayOf(shot.duration,0)
            "green", "black" -> LongArray(this.bps*2){ i -> if(i.rem(2) == 1) 0 else shot.duration / this.bps }
            else -> LongArray(0){0}
        }
    }
    private fun makeAmplitudeArray(shot: OneShot): IntArray{
        return if(shot.isStaccato){
            when (shot.codeName) {
                "red" -> IntArray(bps * 2) { i -> if (i.rem(4) == 0) 50  else 0 }
                "blue" -> IntArray(bps * 2) { i -> if (i.rem(4) == 0) 255  else 0 }
                "green" -> IntArray(bps * 2) { i -> if (i.rem(4) == 0) (i) * 255 / bps / 2  else 0 }
                "black" -> IntArray(bps * 2) { i -> if (i.rem(4) == 0) (bps * 2 - i) * 255 / bps / 2  else 0 }
                else -> IntArray(0) { 0 }

            }
        }
        else {
             when (shot.codeName) {
                "red" -> intArrayOf(50, 0)
                "blue" -> intArrayOf(255, 0)
                "green" -> IntArray(bps * 2) { i -> if (i.rem(2) == 1) 0 else (i) * 255 / bps / 2 }
                "black" -> IntArray(bps * 2) { i -> if (i.rem(2) == 1) 0 else (bps * 2 - i) * 255 / bps / 2 }
                else -> IntArray(0) { 0 }
            }
        }
    }

}