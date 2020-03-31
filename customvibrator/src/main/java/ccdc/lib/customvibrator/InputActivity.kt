package ccdc.lib.customvibrator

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.input_main.*
import java.io.FileNotFoundException
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class InputActivity : AppCompatActivity() {
    var recording : Boolean = false
    private lateinit var arrayOnOff : ArrayList<OnOffVibration>
    private var duration = 0
    lateinit var startTime : LocalTime
    val mHandler = Handler()
    private lateinit var myThread: CircleThread
    private lateinit var customVibration : CustomVibration
    private val MAX_DURATION : Int = 4000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.input_main)

        InputVibrationView_rail.maxDuration = MAX_DURATION


        myThread = CircleThread()
        myThread.isDaemon = true
        button.text = "시작"

        button.setOnClickListener {
            if(!recording){ //시작시
                whenRecordingStart()
            }else{ //정지시
                whenRecordingFinish((ChronoUnit.MILLIS.between(startTime,LocalTime.now()).toInt()))
            }
        }

        //test 화면으로 이동
        button2.setOnClickListener {
            if(!recording){
                if(duration != 0) {
                    //파일 저장
                    val fileName =EditText_input_main.text.toString()
                    try {
                        val fOS = openFileOutput(fileName, Context.MODE_PRIVATE)
                        customVibration.saveAsFile(fOS)
                        val intentToTest = Intent(applicationContext, TestActivity::class.java)

                        intentToTest.putExtra("fileName", fileName)
                        startActivity(intentToTest)
                    }catch (e:FileNotFoundException){
                        Toast.makeText(applicationContext,"저장실패",Toast.LENGTH_LONG).show()
                        Log.d("쓰기","파일이 없다")
                    }
                }
            }
        }
    }



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!recording){
            return true
        }else{
            Log.i("action",event?.action.toString())
            when(event?.action){
                MotionEvent.ACTION_DOWN-> {
                    arrayOnOff.add(OnOffVibration(true,ChronoUnit.MILLIS.between(startTime,LocalTime.now())/10*10,0))
                }
                MotionEvent.ACTION_UP -> {
                    arrayOnOff[arrayOnOff.lastIndex].fnTime = ChronoUnit.MILLIS.between(startTime,LocalTime.now())/10*10
                }
                else -> return true
            }
        }

        return super.onTouchEvent(event)

    }


    inner class CircleThread : Thread(){
        override fun run() {
            val passedMillis = (ChronoUnit.MILLIS.between(startTime,LocalTime.now())).toInt()
            if (passedMillis <= MAX_DURATION) {
                InputVibrationView_rail.arrayOnOff = this@InputActivity.arrayOnOff
                InputVibrationView_rail.passedMillis = passedMillis.toFloat()
                InputVibrationView_rail.invalidate()

                if(this@InputActivity.recording){
                    sleep(1)
                    mHandler.post(this)
                }
            }
            else{
                whenRecordingFinish(MAX_DURATION)
            }
        }
    }

    private fun whenRecordingStart(){
        button.text = "정지"
        recording = true
        arrayOnOff = ArrayList()
        startTime = LocalTime.now()
        myThread.run()

    }
    private fun whenRecordingFinish(passedMillis : Int) {
        val passedMillis10 = passedMillis/10*10
        if (arrayOnOff.isNotEmpty()) {
            if (arrayOnOff.last().fnTime == 0.toLong()) {
                arrayOnOff.last().fnTime = passedMillis10.toLong()
            }
        }
        this.duration = passedMillis10

        customVibration = CustomVibration(arrayOnOff,passedMillis10,EditText_input_main.text.toString())
        VibeBlockView_testing_main.customVibration = customVibration
        VibeBlockView_testing_main.setBlock(96F)


        button.text = "시작"
        recording = false
    }

}
