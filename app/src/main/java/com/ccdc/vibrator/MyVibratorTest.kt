package com.ccdc.vibrator

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class MyVibratorTest(val vibrator: Vibrator, val mDataset : MutableList<OneShot>) {

    fun vibrate(){
        //dataset에 맞춰서 setVibrationEffect List _완
        val effectList : List<VibrationEffect> = List<VibrationEffect>(mDataset.size){
                index -> makeVibrationEffect(mDataset[index])
        }
        val vibrator = vibrator
        for (VE in effectList ){
            vibrator.vibrate(VE)
        }
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

    private fun makeVibrationEffect(shot: OneShot) : VibrationEffect {
        /*
            1. "PIANO"       -> soft          -> red
            2. "FORTE"       -> loud          -> blue
            3. "CRESCENDO"   -> growing       -> green
            4. "DECRESCENDO" -> decreasing    -> black
            ''sc'' -> staccato  ex) scPIANO -> soft staccato
            1 Shot divided for 25
         */
        val codeName = shot.codeName
        val duration = shot.duration
        val bps = 25
        return when (codeName) {
            "red" -> VibrationEffect.createWaveform(
                LongArray(bps*2){ i -> if(i.rem(2) == 0) 0 else duration / bps },
                IntArray(bps*2){ i -> if(i.rem(2) == 0) 0 else 50},
                -1)
            "blue" -> VibrationEffect.createWaveform(
                LongArray(bps*2){ i -> if(i.rem(2) == 0) 0 else duration / bps },
                IntArray(bps*2){ i -> if(i.rem(2) == 0) 0 else 255},
                -1)
            "green" -> VibrationEffect.createWaveform(
                LongArray(bps*2){ i -> if(i.rem(2) == 0) 0 else duration / bps },
                IntArray(bps*2){ i -> if(i.rem(2) == 0) 0 else (i+1)*255/bps/2},
                -1
            )
            "black" -> VibrationEffect.createWaveform(
                LongArray(bps*2){ i -> if(i.rem(2) == 0) 0 else duration / bps },
                IntArray(bps*2){ i -> if(i.rem(2) == 0) 0 else (50-i-1)*255/bps/2},
                -1
            )
            else -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        }
    }

}