package ccdc.lib.customvibrator

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.input_main.*
import java.io.FileNotFoundException
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class InputActivity : AppCompatActivity() {
    private var recording = false
    private lateinit var arrayOnOff : ArrayList<OnOffVibration>
    private var duration = 0
    lateinit var startTime : LocalTime
    private var mHandler = Handler()
    private lateinit var myThread: CircleThread
    private lateinit var customVibration : CustomVibration
    private val maxDuration : Int = 4000
    private var fileName : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.input_main)

        InputVibrationView_rail.maxDuration = maxDuration


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

        //button
        button_toTest.setOnClickListener {
            if(saveCustomVibration()) {
                val intentToTest = Intent(applicationContext, EditActivity::class.java)
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
        //spinner
        val items = resources.getStringArray(R.array.spinner_color_items)
        val spinnerAdapter = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,items)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val color = getColor(resources.getIdentifier(items[position],"color",packageName))
                parent?.setBackgroundColor(color)
                if(::customVibration.isInitialized){
                    customVibration.blockColor = color
                    VibeBlockView_testing_main.setBlock(96F)
                }
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
            if(newFileName != fileName){
                if(isExist(newFileName)){
                    Toast.makeText(this,"이미 있는 파일이름 입니다.",Toast.LENGTH_LONG).show()
                    return false
                }else{
                    deleteFile(fileName) //원래있던파일 삭제
                }
            } //파일이름이 안바뀌었으면 그대로 덮어씌우기
            return try {
                val fOS = openFileOutput(newFileName, Context.MODE_PRIVATE)
                customVibration.saveAsFile(fOS)
                fileName = newFileName
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
                    arrayOnOff.add(OnOffVibration(ChronoUnit.MILLIS.between(startTime,LocalTime.now())/10*10,0))
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
            if (passedMillis <= maxDuration) {
                InputVibrationView_rail.arrayOnOff = this@InputActivity.arrayOnOff
                InputVibrationView_rail.passedMillis = passedMillis.toFloat()
                InputVibrationView_rail.invalidate()

                if(this@InputActivity.recording){
                    sleep(1)
                    mHandler.post(this)
                }
            }
            else{
                whenRecordingFinish(maxDuration)
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

        customVibration = CustomVibration(arrayOnOff,passedMillis10,"temptemp")
        customVibration.blockColor = getColor(resources.getIdentifier(spinner.selectedItem.toString(),"color",packageName))
        VibeBlockView_testing_main.customVibration = customVibration
        VibeBlockView_testing_main.setBlock(96F)


        button.text = "시작"
        recording = false
    }

}
