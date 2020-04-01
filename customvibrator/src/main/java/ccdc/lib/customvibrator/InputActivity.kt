package ccdc.lib.customvibrator

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.input_main.*
import java.io.File
import java.io.FileNotFoundException
import java.net.FileNameMap
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
    private var fileName : String = ""


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
        button_toTest.setOnClickListener {
            if(saveCustomVibration()) {
                val intentToTest = Intent(applicationContext, TestActivity::class.java)
                intentToTest.putExtra("fileName", fileName)
                startActivity(intentToTest)
            }
        }
        button_toHome.setOnClickListener {
            if(saveCustomVibration()){
                intent.putExtra("newFileName",fileName)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
        }
    }

    private fun saveCustomVibration(): Boolean {
        if(!recording && duration !=0){
            val newFileName = EditText_fileName.text.toString()
            if(newFileName == ""){
                Toast.makeText(this,"파일이름을 입력하세요",Toast.LENGTH_LONG).show()
                return false
            }
            if(fileName != newFileName){ //새로운 파일이름을 받으면 원래 있던걸 삭제하고 다시 입력
                deleteFile(fileName)  //원래 파일이 있는지 없는지는 중요하지 않음
                fileName = newFileName
            }
            if(isExist(fileName)){
                Toast.makeText(this,"이미 있는 파일이름 입니다.",Toast.LENGTH_LONG).show()
                fileName = ""
                return false
            }
            return try {
                val fOS = openFileOutput(fileName, Context.MODE_PRIVATE)
                customVibration.saveAsFile(fOS)
                true
            }catch (e:FileNotFoundException){
                Toast.makeText(applicationContext,"저장실패",Toast.LENGTH_LONG).show()
                false
            }
        }
        return false
    }
    private fun isExist(fileName : String) : Boolean{
        val file = getFileStreamPath(fileName)
        return file.exists()
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

        customVibration = CustomVibration(arrayOnOff,passedMillis10,EditText_fileName.text.toString())
        VibeBlockView_testing_main.customVibration = customVibration
        VibeBlockView_testing_main.setBlock(96F)


        button.text = "시작"
        recording = false
    }

}
