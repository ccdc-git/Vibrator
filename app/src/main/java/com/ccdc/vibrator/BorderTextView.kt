package com.ccdc.vibrator


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.TextView
import java.lang.Exception

class BorderTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr){
    var hasStroke : Boolean = false
    private var strokeColor : Int = Color.BLACK
    var strokeSize : Float = 0.0f

    init {
        context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.BorderTextView,
            0,0)?.apply {
            try{
                this@BorderTextView.hasStroke = getBoolean(R.styleable.BorderTextView_textStroke,false)
                this@BorderTextView.strokeColor = getColor(R.styleable.BorderTextView_textStrokeColor,Color.BLACK)
                this@BorderTextView.strokeSize = getDimension(R.styleable.BorderTextView_textStrokeSize,2F)
            }catch (e: Exception){
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (hasStroke) {
            val states : ColorStateList = textColors;
            paint.style = Paint.Style.STROKE;
            paint.strokeWidth = strokeSize;
            setTextColor(strokeColor);
            super.onDraw(canvas);

            paint.style = Paint.Style.FILL;
            setTextColor(states);
        }

        super.onDraw(canvas)
    }
}