package ccdc.lib.customvibrator

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

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

    var customVibration : CustomVibration? = null

    fun setBlock(sizeDp : Float){  //새로운 높이로 setting
        val cV = customVibration
        if (cV != null){
                val bitmap = cV.makeBitmap(cV.blockColor, cV.bgColor)
                this.layoutParams.height = dpToPx(sizeDp,context)
                this.setImageDrawable(BitmapDrawable(resources, bitmap))
                this.layoutParams.width = (this.layoutParams.height * (cV.duration / 1000F)).toInt()
        }
    }
    fun setBlock(){ //기존의 높이대로 setting
        setBlock(this.height.toFloat())
    }
    private fun dpToPx(size : Float, context : Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,context.resources.displayMetrics).toInt()
}