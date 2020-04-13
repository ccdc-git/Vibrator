package ccdc.lib.customvibrator

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import kotlin.math.abs

class InputVibrationView @JvmOverloads //입력을 보여주는 뷰
constructor(context: Context, attr : AttributeSet? = null,defStyleAttr : Int = 0 ) : View(context, attr, defStyleAttr) {
    private val paint = Paint()
    private val paint2 = Paint()
    var passedMillis : Float = 0F
    var arrayOnOff : ArrayList<OnOffVibration> = ArrayList()
    var maxDuration : Int = 4000
    init {
        context.theme?.obtainStyledAttributes(
            attr,
            R.styleable.InputVibrationView,
            0,0)?.apply {
            try {
                this@InputVibrationView.paint.color = getColor(R.styleable.InputVibrationView_paintColor,Color.BLACK)
                paint2.color = Color.CYAN
            }finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            //네모 그리기
            if (this@InputVibrationView.arrayOnOff.size != 0) {
                this.arrayOnOff.forEach { onOff: OnOffVibration ->
                    val left = (this.width * onOff.stTime / maxDuration).toFloat()
                    val right = if (onOff.fnTime == 0.toLong()) (this.width * passedMillis / maxDuration) //입력중인 OnOff
                    else (onOff.fnTime * this.width / maxDuration).toFloat() //지금까지 입력받은 OnOff

                    canvas.drawRect(
                        left, 0F,
                        right, this.height.toFloat(), paint)
                }
            }
            //움직이는 동그라미
            canvas.drawCircle((this.width * passedMillis / maxDuration), (height/2).toFloat(), (height/2).toFloat(),paint2)
        }
    }
}


class VibeBlockView @JvmOverloads  //한 블럭 보여주기
constructor(context: Context, attr : AttributeSet? = null,defStyleAttr : Int = 0 ) : androidx.appcompat.widget.AppCompatImageView(context, attr, defStyleAttr) {
    init {
        (this as ImageView).scaleType = ScaleType.FIT_XY
        this.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    setTouchDownListener(v)
                }
                else{
                    v.setOnTouchListener { _, _ ->return@setOnTouchListener false}
                }

            }
    }
    var customVibration : CustomVibration? = null
    private fun setTouchDownListener(v :View){
        v.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN)
                setTouchHorizonOrVerticalListener(v,event)
            return@setOnTouchListener true
        }
    }
    private fun setTouchHorizonOrVerticalListener(v: View, downEvent: MotionEvent){
        val downX = downEvent.x
        val downY = downEvent.y
        v.setOnTouchListener { v, event ->
            Log.v("action",MotionEvent.actionToString(event.action))
            if (event.action == MotionEvent.ACTION_MOVE){
                if (abs(downX - event.x) > 25){
                    setTouchMoveHorizonListener(v, downX)
                }else if(abs(downY - event.y) > 25){
                    setTouchMoveVerticalListener(v,downY)
                }

            }else{
                setTouchDownListener(v)
            }
            return@setOnTouchListener true
        }

    }
    private fun setTouchMoveHorizonListener(v : View, downX : Float){
        val currentDuration = (v as VibeBlockView).customVibration!!.duration
        v.setOnTouchListener { v, event ->
            val vibeBlockView = v as VibeBlockView
            val dx = event.x - downX
            var newDuration = (currentDuration + dx * (1000F / v.height)).toInt()

            if(newDuration > 4000) {
                newDuration = 4000
            }else if(newDuration < 100){
                newDuration = 100
            }

            if (event.action == MotionEvent.ACTION_MOVE){
                val params = vibeBlockView.layoutParams
                params.width = (params.height* (newDuration)/1000F).toInt()
                vibeBlockView.layoutParams = params  //속도를 위해 가로만 수정
            }else {
                vibeBlockView.customVibration!!.changeDuration(newDuration)
                vibeBlockView.setBlock()
                setTouchDownListener(v)
            }
            return@setOnTouchListener true
        }
    }
    private fun setTouchMoveVerticalListener(v:View, downY: Float){
        val currentMaxAmp = (v as VibeBlockView).customVibration!!.maxAmp
        v.setOnTouchListener { v, event ->
            val vibeBlockView = v as VibeBlockView
            val dy = event.y - downY
            var newMaxAmp =(currentMaxAmp -  (255F / v.height * dy)).toInt()

            if(newMaxAmp > 255) {
                newMaxAmp = 255
            }else if(newMaxAmp < 1){
                newMaxAmp = 1
            }
            vibeBlockView.customVibration!!.changeMaxAmp(newMaxAmp)
            vibeBlockView.setBlock()

            if (event.action == MotionEvent.ACTION_MOVE){
            }else {
                setTouchDownListener(v)
            }
            return@setOnTouchListener true
        }

    }


    fun setBlock(sizeDp : Float){  //새로운 높이로 setting
        this.layoutParams.height = dpToPx(sizeDp)
        setBlock()
    }
    fun setBlock(){ //기존의 높이대로 setting
        val cV = customVibration
        if (cV != null){
            val bitmap = cV.makeBitmap()
            val bitmapDrawable = BitmapDrawable(this.resources,bitmap)
            val layerDrawable = LayerDrawable(arrayOf(bitmapDrawable,context.getDrawable(R.drawable.block_bg)))
            this.setImageDrawable(layerDrawable)
            this.layoutParams.width = (this.layoutParams.height * (cV.duration / 1000F)).toInt()
        }

    }

    private fun dpToPx(size : Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,context.resources.displayMetrics).toInt()
}