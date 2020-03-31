package com.ccdc.vibrator

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import ccdc.lib.customvibrator.CustomVibration
import fragments.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_staccato.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private lateinit var myRVC : MyRecyclerViewController

    private val fragmentManager = supportFragmentManager

    private val fragmentNormal = FragmentNormal()
    private val fragmentStaccato = FragmentStaccato()
    private val fragmentCustom = FragmentCustom()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myDataset = mutableListOf<CustomVibration>()
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val myVibrator  = MyVibrator(vib,myDataset)

        //my_recycler_view
        myRVC = MyRecyclerViewController(this,findViewById(R.id.my_recycler_view),myDataset)


    //BottomNavigationView
        //첫화면 지정
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout,fragmentNormal).commitAllowingStateLoss()

        //BottomNavigationView 설정
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            val transaction = fragmentManager.beginTransaction()
            when(item.itemId) {

                R.id.menu_normal -> transaction.replace(R.id.frame_layout, fragmentNormal)
                    .commitAllowingStateLoss()
                R.id.menu_staccato -> transaction.replace(R.id.frame_layout, fragmentStaccato)
                    .commitAllowingStateLoss()
                R.id.menu_custom -> transaction.replace(R.id.frame_layout, fragmentCustom)
                    .commitAllowingStateLoss()
                else -> return@setOnNavigationItemSelectedListener true
            }

            return@setOnNavigationItemSelectedListener true
        }


        //EditText_for_test

        //Button_for_test
        Button_for_test.setOnClickListener {
            when (EditText_for_test.text.toString()) {
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
                "text" -> Log.i("dd",myRVC.Dataset.toString())
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE))
                    }else{
                        @Suppress("DEPRECATION")
                        vib.vibrate(longArrayOf(0, 500), -1)
                    }
                    Toast.makeText(this,"잘못입력",Toast.LENGTH_LONG).show()

                }
            }
            EditText_for_test.setText("")
        }
        //Button_for_test _ end

        //Buttons


        //playButton
        floatingActionButton_play.setOnClickListener {
            myVibrator.vibrate()
        }

        //touch


    }//fin onCreate


    fun setButton(button: Button, staccato : Boolean){
        val codeName : String = button.text.toString()
        button.text = ""
        button.setBackgroundResource( resources.getIdentifier(codeName,"mipmap",packageName))
        button.setOnClickListener {
            myRVC.addItemAt(myRVC.size,CustomVibration(this, codeName))
        }
    }
    fun addInMyRVC(codeName : String){
        try {
            val fIS: FileInputStream = openFileInput(codeName)
            myRVC.addItemAt(myRVC.size, CustomVibration(fIS, codeName))
        }catch (e: FileNotFoundException){
            Log.v("반쯤",codeName)
        }
    }

}
