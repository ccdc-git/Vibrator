package ccdc.lib.customvibrator
//630479983195 cj 대한통운
import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_editor.*

class EditActivity : AppCompatActivity() {
    var leftEnd : Int = 0
    var rightEnd : Int = 0
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
        setContentView(R.layout.activity_editor)

        //intent 안에 fileName 있음
        //파일 이름으로 customVibration 불러오기

        val fileName = if( intent.extras == null) "" else  intent.extras?.getString("fileName").toString()
        val cV = CustomVibration(this,fileName)
        setWorkBoard(cV,imageView_workBoard)
        editText_blockColor.setText("black")
        editText_duration.setText(cV.duration.toString())

        //버튼 누르면 저장
        button_save.setOnClickListener {
            imageView_workBoard.makeNewCustomVibration().saveAsFile(this,fileName)
            setResult(Activity.RESULT_OK)
            finish()
        }

        val vib =getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib.vibrate(VibrationEffect.createWaveform(cV.getTimingsArray().toLongArray(),cV.getAmplitudesArray().toIntArray(),-1)) // 처음 진동

        //필요한 값들
        val displayMetrics = DisplayMetrics()
        val cVBitmapWidth = (imageView_workBoard.layoutParams.height * cV.duration / 1000F).toInt()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val displayWidth = displayMetrics.widthPixels
        leftEnd = (displayWidth - cVBitmapWidth)/2
        rightEnd = (displayWidth + cVBitmapWidth)/2

        //workBoard 초기 설정
        imageView_workBoard.setWorkBoard(cV, displayWidth, imageView_leftPin.width)
        imageView_workBoard.pointImageView = imageView_point



        //초기 위치 지정
        val leftPinParams = imageView_leftPin.layoutParams as ConstraintLayout.LayoutParams
        val rightPinParams = imageView_rightPin.layoutParams as ConstraintLayout.LayoutParams

        leftPinParams.marginStart = leftEnd - leftPinParams.width
        rightPinParams.marginEnd = displayWidth - rightEnd - rightPinParams.width

        imageView_leftPin.layoutParams = leftPinParams
        imageView_rightPin.layoutParams = rightPinParams

        refreshTextView()


        //움직임 설정
        setLeftPinDownListener(view_leftPin_touch)
        setRightPinDownListener(view_rightPin_touch)

        //point
        imageView_point.setOnLongClickListener {v : View ->
            val myShadowBuilder =  MyDragShadowBuilder(v)
            val dragData = ClipData(v.tag as? CharSequence, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                ClipData.Item(v.tag as? CharSequence))
            v.startDragAndDrop(dragData,myShadowBuilder,null,0)
            return@setOnLongClickListener false
        }
        imageView_workBoard.setOnDragListener { v, event ->
            when(event.action){
                DragEvent.ACTION_DRAG_STARTED ->{
                    if(event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                        return@setOnDragListener true
                    return@setOnDragListener false
                }
                DragEvent.ACTION_DROP , DragEvent.ACTION_DRAG_LOCATION , DragEvent.ACTION_DRAG_EXITED , DragEvent.ACTION_DRAG_ENTERED , DragEvent.ACTION_DRAG_ENDED ->{
                    (v as WorkBoardView).newAmpPoint(event)
                    return@setOnDragListener true
                }
                else -> {
                    return@setOnDragListener false
                }
            }
        }
    }

    private fun setLeftPinMoveListener(view: View, lastEvent : MotionEvent){
        val lastX = lastEvent.x
        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE){
                val dx = event.x - lastX
                //Log.v("dx","$dx  $lastX")
                val params = (imageView_leftPin.layoutParams as ConstraintLayout.LayoutParams)
                params.marginStart += dx.toInt()

                if(params.marginStart + params.width > imageView_rightPin.x){
                    params.marginStart = (imageView_rightPin.x - params.width).toInt()
                }else if(params.marginStart + params.width < leftEnd){
                    params.marginStart = leftEnd - params.width
                }
                imageView_leftPin.layoutParams = params
            }else{
                setLeftPinDownListener(v)
            }
            imageView_leftPin.invalidate()
            imageView_workBoard.leftX = ((imageView_leftPin.layoutParams as ConstraintLayout.LayoutParams).marginStart + imageView_leftPin.width)
            imageView_workBoard.refreshWorkBoardDrawable(false,true,false)
            refreshTextView()
            return@setOnTouchListener false
        }
    }
    private fun setLeftPinDownListener(view: View){
        view.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    setLeftPinMoveListener(v,event)
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }
    private fun setRightPinMoveListener(view: View, lastEvent : MotionEvent){
        val lastX = lastEvent.x
        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE){
                val dx = event.x - lastX
                //Log.v("dx",dx.toString())
                val params = ( imageView_rightPin.layoutParams as ConstraintLayout.LayoutParams)
                params.marginEnd += -1 * dx.toInt()
                if(imageView_workBoard.width - (params.marginEnd + params.width) < (imageView_leftPin.x + params.width)){
                    params.marginEnd = (imageView_workBoard.width - imageView_leftPin.x - params.width * 2 ).toInt()
                }else if(imageView_workBoard.width - (params.marginEnd + params.width) > rightEnd){
                    params.marginEnd = imageView_workBoard.width - params.width - rightEnd
                }
                imageView_rightPin.layoutParams = params
            }else{
                setRightPinDownListener(v)
            }
            imageView_rightPin.invalidate()
            imageView_workBoard.rightX = (imageView_rightPin.layoutParams as ConstraintLayout.LayoutParams).marginEnd + imageView_rightPin.width
            imageView_workBoard.refreshWorkBoardDrawable(false,true,false)
            refreshTextView()
            return@setOnTouchListener false
        }
    }
    private fun setRightPinDownListener(view: View){
        view.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    val ex = event.rawX - dPtoPX(24F)
                    if(ex - view_leftPin_touch.x < view_rightPin_touch.x - ex) return@setOnTouchListener false
                    setRightPinMoveListener(v,event)
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }
    private fun refreshTextView(){
        textView_duration.text = imageView_workBoard.duration().toString()
        textView_startPoint.text = imageView_workBoard.startPoint().toString()
        textView_endPoint.text = imageView_workBoard.endPoint().toString()
    }


    private fun setWorkBoard(cV : CustomVibration, iV : ImageView){
        val cVBitmap = cV.makeBitmap()
        val resizedBitmap = Bitmap.createScaledBitmap(cVBitmap,(iV.layoutParams.height * cV.duration / 1000F).toInt(), iV.layoutParams.height,false)
        val bitmapDrawable = BitmapDrawable(resources,resizedBitmap)
        bitmapDrawable.gravity = Gravity.CENTER
        iV.setImageDrawable(bitmapDrawable)
    }
    private fun dPtoPX(dp : Float) : Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
}//EditActivity
class MyDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {

    private val shadow = ColorDrawable(Color.LTGRAY)

    override fun onDrawShadow(canvas: Canvas) {
        // Draws the ColorDrawable in the Canvas passed in from the system.
        view.draw(canvas)
    }
}



