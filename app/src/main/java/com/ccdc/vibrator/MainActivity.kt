package com.ccdc.vibrator

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var myRVC : MyRecyclerViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myDataset = mutableListOf<OneShot>()
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val myVibrator  = MyVibrator(vib,myDataset)


        //my_recycler_view
        myRVC = MyRecyclerViewController(this,findViewById(R.id.my_recycler_view),myDataset)

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
                "piano", "forte", "crescendo", "decrescendo" ->{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(200,VibrationEffect.DEFAULT_AMPLITUDE))
                    }else{
                        vib.vibrate(longArrayOf(0, 200), -1)
                    }
                    if (!myRVC.addItemAt(myRVC.size, OneShot(inpText,1000, Switch_staccato.isChecked))) {
                        Toast.makeText(this, "index 없음", Toast.LENGTH_LONG).show()
                    }

                }
                "text" -> Log.i("dd",myRVC.Dataset.toString())
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE))
                    }else{
                        vib.vibrate(longArrayOf(0, 500), -1)
                    }
                    Toast.makeText(this,"잘못입력",Toast.LENGTH_LONG).show()

                }
            }
            EditText_for_test.setText("")
        }
        //Button_for_test _ end

        //Buttons
        setButton(Button_piano)
        setButton(Button_forte)
        setButton(Button_crescendo)
        setButton(Button_decrescendo)

        //Switch_staccato
        Switch_staccato.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            Button_piano.setBackgroundResource(getShotId(this, OneShot(Button_piano.text.toString(),0,b)))
            Button_forte.setBackgroundResource(getShotId(this, OneShot(Button_forte.text.toString(),0,b)))
            Button_crescendo.setBackgroundResource(getShotId(this, OneShot(Button_crescendo.text.toString(),0,b)))
            Button_decrescendo.setBackgroundResource(getShotId(this, OneShot(Button_decrescendo.text.toString(),0,b)))
        }
        //playbutton
        floatingActionButton_play.setOnClickListener { _->
            myVibrator.vibrate()
        }

    }

    private fun setButton(button: Button){
        val codeName : String = button.text.toString()
        button.setBackgroundResource(getShotId(this, OneShot(codeName,0,Switch_staccato.isChecked)))
        button.setOnClickListener { _: View? ->
            myRVC.addItemAt(myRVC.size, OneShot(codeName,1000, Switch_staccato.isChecked))
        }
    }
    private fun getShotId(context: Context, shot: OneShot): Int {
        val resName = when(shot.isStaccato) {
            true ->   "${shot.codeName}_staccato"
            else -> "${shot.codeName}_normal"
        }
        return context.resources.getIdentifier(resName,"mipmap",context.packageName)
    }


}
