package com.ccdc.vibrator

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock.sleep
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myDataset = mutableListOf<OneShot>()
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val myVibrator  = MyVibrator(vib,myDataset)


        //my_recycler_view
        val myRVC = MyRecyclerViewController(this,findViewById(R.id.my_recycler_view),myDataset)

        //EditText_for_test


        //Button_for_test
        Button_for_test.setOnClickListener {
            when (val inpText = EditText_for_test.text.toString()) {
                "remove" -> {
                    if(!myRVC.removeItemAt(myRVC.size - 1)) {  //실패하면 메시지 띄우기
                        val toast = Toast.makeText(this, "index 없음", Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
                "remove all" -> {
                    if(!myRVC.removeItemAll()) {  //실패하면 메시지 띄우기
                        val toast = Toast.makeText(this, "index 없음", Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
                "do" -> {
                    myVibrator.vibrate()
                }
                "cancel" -> {
                    myVibrator.cancel()
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE))
                    }else{
                        vib.vibrate(longArrayOf(0, 500), -1)
                    }

                    try {
                        if (inpText != "") { //텍스트가 비면 Color.parseColor(inpText)에서 java.lang.StringIndexOutOfBoundsException
                            val colorForAdd = Color.parseColor(inpText)
                            if (!myRVC.addItemAt(myRVC.size, OneShot(inpText,1000, Switch_for_test.isChecked))) {
                                Toast.makeText(this, "index 없음", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        Toast.makeText(this, "색없음", Toast.LENGTH_LONG).show()
                    }
                }
            }
            EditText_for_test.setText("")
        }
        //Button_for_test _ end

    }



}
