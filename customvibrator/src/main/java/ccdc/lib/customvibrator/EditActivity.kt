package ccdc.lib.customvibrator

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_tester.*

class EditActivity : AppCompatActivity() {
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action  == MotionEvent.ACTION_DOWN){
            val current_focus = currentFocus
            if(current_focus is VibeBlockView){
                var outRect = Rect()
                current_focus.getGlobalVisibleRect(outRect)
                if(!outRect.contains(ev.rawX.toInt() , ev.rawY.toInt())){
                    current_focus.clearFocus()
                    val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(current_focus.windowToken,0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tester)




        //intent 안에 fileName 있음
        //파일 이름으로 customVibration 불러오기

        val fileName = if( intent.extras == null) "" else  intent.extras?.getString("fileName").toString()

        //Log.v("getFileName", fileName)
        val fIS = openFileInput(fileName)
        val cV = CustomVibration(fIS,fileName)
        VibeBlockView_testing_tester.customVibration = cV
        EditText_1.setText("white")
        EditText_2.setText("black")
        EditText_3.setText(cV.duration.toString())
        EditText_4.setText(cV.maxAmp.toString())
//        view_editor.visibility = View.GONE
        VibeBlockView_testing_tester.setOnLongClickListener { v ->
            v.requestFocus()
            Log.v("focus",currentFocus.toString())
//            view_editor.visibility = View.VISIBLE
            return@setOnLongClickListener false
        }


        //버튼 누르면 미리보기 초기화
        button.setOnClickListener {
            //1번은 배경색
            try {
                cV.bgColor = Color.parseColor(EditText_1.text.toString())
            } catch (e: IllegalArgumentException) {
                Log.d("bgcolor",EditText_1.text.toString())
                cV.bgColor = Color.WHITE
                EditText_1.setText("white")
            }

            //2번은 블록색
            try {
                cV.blockColor = Color.parseColor(EditText_2.text.toString())
            } catch (e: IllegalArgumentException) {
                cV.blockColor = Color.BLACK
                EditText_2.setText("black")
            }

            //3번은 duration
            try {
                cV.changeDuration(EditText_3.text.toString().toInt())
            }catch (e: NumberFormatException){
                EditText_3.setText(cV.duration.toString())
            }

            //4번은 MaxAmp
            try {
                cV.changeMaxAmp(EditText_4.text.toString().toInt())
            }catch (e: NumberFormatException){
                EditText_4.setText(cV.maxAmp.toString())
            }
            TextView_parameter.text = "${cV.arrayOnOff}\n${cV.pathPoints}\n${cV.duration}"

            val vib =getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.vibrate(VibrationEffect.createWaveform(cV.getTimingsArray().toLongArray(),cV.getAmplitudesArray().toIntArray(),-1))

            VibeBlockView_testing_tester.setBlock(96F)
        }



    }//onCreate

}//EditActivity
