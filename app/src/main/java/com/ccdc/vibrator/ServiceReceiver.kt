package com.ccdc.vibrator;

import android.content.BroadcastReceiver;
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.util.Log
import java.io.FileNotFoundException

class ServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        val vib = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        when(state){
            TelephonyManager.EXTRA_STATE_RINGING ->{
                //벨이 울림
                phoneRing(vib,context)
            }
            TelephonyManager.EXTRA_STATE_IDLE ->{
                //종료
                vib.cancel()
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK ->{
                //대기
                vib.cancel()
            }
            else ->{}

        }
    }
    private fun phoneRing(vib : Vibrator, context: Context?){
        try {
            if(context != null) {
                val fIS = context.openFileInput("phone_calling")
                val lines = fIS.reader().readLines()
                if (lines.size != 2) return
                val timings = lines[0].split("\\").map { str -> str.toLong() }
                val amps = lines[1].split("\\").map { str -> str.toInt() }
                vib.vibrate(VibrationEffect.createWaveform(timings.toLongArray(), amps.toIntArray(), 0))
            }
        }catch (e : FileNotFoundException){
        }
    }

}
