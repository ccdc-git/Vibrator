package com.ccdc.vibrator;

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.FileNotFoundException

class NotificationListener : NotificationListenerService() {
    companion object {
        const val TAG = "MyNotificationListener"
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn != null) {
            Log.d(TAG, """onNotificationRemoved ~
                |packageName : ${sbn.packageName}
                |notification : ${sbn.notification}
                |id : ${sbn.id}""".trimMargin())
            if(sbn.notification.channelId == "phone_incoming_call"){
                val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vib.cancel()
                Log.v("vib","canceled ")
            }
        }
    }


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if(sbn != null) {
            val notification = sbn.notification
            val extras = sbn.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE)
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
            val smallIcon = notification.smallIcon
            val largeIcon = notification.getLargeIcon()

            val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if(notification.channelId == "phone_incoming_call"){
                phoneRing(vib)
            }


/*Notification(channel=phone_incoming_call pri=2 contentView=null vibrate=null sound=null defaults=0x0 flags=0xea color=0xff2a56c6 category=call actions=2 vis=PRIVATE publicVersion=Notification(channel=null pri=0 contentView=null vibrate=null sound=null defaults=0x0 flags=0x0 color=0xff2a56c6 vis=PRIVATE))*/
            Log.d(TAG,"""onNotificationPosted ~
                |packageName : ${sbn.packageName}
                |id : ${sbn.id}
                |postTime : ${sbn.postTime}
                |title : $title
                |text : $text
                |subText : $subText
                |Notification ${sbn.notification}
                |Bundle : ${sbn.notification.extras}
                |sbn : $sbn
            """.trimMargin())

        }
    }
    fun phoneRing(vib : Vibrator){
        try {
            val fIS = openFileInput("phone_calling")
            val lines = fIS.reader().readLines()
            if (lines.size != 2) return
            val timings = lines[0].split("\\").map { str -> str.toLong() }
            val amps = lines[1].split("\\").map{ str -> str.toInt()}
            vib.vibrate(VibrationEffect.createWaveform(timings.toLongArray(),amps.toIntArray(),0))
            Log.d("vibrated","""okey
$timings
$amps
                """.trimIndent())
        }catch (e : FileNotFoundException){

        }
    }
}
