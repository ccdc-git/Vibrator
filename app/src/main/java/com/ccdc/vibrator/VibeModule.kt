package com.ccdc.vibrator

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class VibeModule(
    var context: Context,
    var codeName : String,
    var duration : Long) {
    lateinit var vibrationEffect: VibrationEffect

    constructor(context: Context, codeName: String) : this(context, codeName, 0)

    fun vibrate(){
        val vib = this.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib.hasVibrator()
    }


    fun setVibrationEffect(){
        /*
            1. "PIANO"       -> soft
            2. "FORTE"       -> loud
            3. "CRESCENDO"   -> growing
            4. "DECRESCENDO" -> decreasing
            ''sc'' -> staccato  ex) scPIANO -> soft staccato
         */


    }
}