package com.ccdc.vibrator

import android.content.Context
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import ccdc.lib.customvibrator.CustomVibration
import ccdc.lib.customvibrator.VibeBlockView
import fragments.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileInputStream
import java.io.FileNotFoundException


class MainActivity : AppCompatActivity() {
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(ev != null)
        if (ev?.action  == MotionEvent.ACTION_DOWN){
            val currentFocusView = currentFocus
            if(currentFocusView is VibeBlockView){
                var outRect = Rect()
                currentFocusView.getGlobalVisibleRect(outRect)
                if(!outRect.contains(ev.rawX.toInt() , ev.rawY.toInt())){
                    myRVC.onClearFocus(currentFocusView) //포커스 해제
                    val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocusView.windowToken,0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private lateinit var myRVC : MyRecyclerViewController

    private val fragmentManager = supportFragmentManager

    private val fragmentNormal = FragmentNormal()
    private val fragmentStaccato = FragmentStaccato()
    private val fragmentCustom = FragmentCustom()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mDataSet: MutableList<CustomVibration> = mutableListOf()
        val vib : Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val myVibrator  = MyVibrator(vib,mDataSet)

        //my_recycler_view
        myRVC = MyRecyclerViewController(this,findViewById(R.id.my_recycler_view),mDataSet)


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


        //playButton
        floatingActionButton_play.setOnClickListener {
            myVibrator.vibrate()
        }

        //touch


    }//fin onCreate


    fun setButton(button: Button){
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
        }
    }

}
