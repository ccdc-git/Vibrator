package com.ccdc.vibrator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
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

        //권한 받아오기
        if(applicationContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            //권한 없을때 받아오기
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),200)
        }

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

        button_set.setOnClickListener {
            if(!phoneStatePermissionGranted()){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE),200)
            }else{
                val fOS = openFileOutput("phone_calling",Context.MODE_PRIVATE)
                myVibrator.makeVibrationEffect()
                fOS.write(myVibrator.timingsArrayAll.joinToString("\\").toByteArray())
                fOS.write("\n".toByteArray())
                fOS.write(myVibrator.amplitudeArrayAll.joinToString("\\").toByteArray())
                fOS.close()
                Toast.makeText(this,"적용됨",Toast.LENGTH_LONG).show()
            }
        }


    }//fin onCreate

    private fun phoneStatePermissionGranted() : Boolean{
        return applicationContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    private fun notificationPermissionGranted() : Boolean{
        //"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
        val sets = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets.contains(packageName)
    }
    @SuppressLint("ClickableViewAccessibility")
    fun setButton(button: TextView){
        val codeName : String = button.text.toString()
        button.text = ""
        val drawable = getDrawable(resources.getIdentifier(codeName,"mipmap",packageName))
        val layerDrawable = LayerDrawable(arrayOf(button.background, drawable))
        button.background = layerDrawable

        button.setOnTouchListener { v, e ->

            when(e.action){
                MotionEvent.ACTION_DOWN -> v.elevation = 0F
                MotionEvent.ACTION_MOVE -> v.elevation = 0F
                else -> v.elevation = 16F
            }
            return@setOnTouchListener false
        }
        button.setOnClickListener {
            it.elevation = 16F
            myRVC.addItemAt(myRVC.size,CustomVibration(this, codeName, true))
        }
    }
    fun addInMyRVC(codeName : String){
        try {
            val fIS: FileInputStream = openFileInput(codeName)
            myRVC.addItemAt(myRVC.size, CustomVibration(this, codeName))
        }catch (e: FileNotFoundException){
        }
    }

}
