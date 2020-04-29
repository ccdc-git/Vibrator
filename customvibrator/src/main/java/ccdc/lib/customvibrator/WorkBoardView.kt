package ccdc.lib.customvibrator

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import kotlin.math.abs
import kotlin.math.roundToInt


class WorkBoardView @JvmOverloads
constructor(context: Context, attr : AttributeSet? = null, defStyleAttr : Int = 0 ) : androidx.appcompat.widget.AppCompatImageView(context,attr,defStyleAttr)
{
    lateinit var cVBitmap : Bitmap
    private lateinit var cVBitmapDrawable: BitmapDrawable
    private lateinit var coverDrawable : BitmapDrawable
    private lateinit var ampPointsDrawable: BitmapDrawable
    private lateinit var coverBitmap : Bitmap
    private val coverPaint = Paint()
    private val pointPaint = Paint()
    lateinit var coverCanvas : Canvas
    lateinit var cV : CustomVibration
    lateinit var ampPointArray : MutableList<AmpPoint>
    var pointImageView : ImageView? = null
    var leftX : Int = 0
    var rightX : Int = 0
    var cVStart : Int = 0
    var cVEnd : Int = 0
    var displayWidth : Int = 0

    fun duration() : Int = ((displayWidth - leftX - rightX) * 1000F / (this.layoutParams.height) ).roundToInt()
    fun startPoint() : Int = ((leftX - (this.displayWidth - cVBitmap.width)/2) * 1000F / (this.layoutParams.height)).roundToInt()
    fun endPoint() : Int = (((this.displayWidth + cVBitmap.width)/2 - rightX)* 1000F / (this.layoutParams.height)).roundToInt()

    init {
        coverPaint.color = Color.argb(125,0,0,0)
        pointPaint.color = Color.argb(100,0,0,0)
        this.isClickable = true
        setTouchDown(this)
    }

    private fun setTouchDown(view : View){
        view.setOnTouchListener { v, e ->
            if(e.action == MotionEvent.ACTION_DOWN) {
                val upX = e.x
                val upY = e.y
                var toRemove : AmpPoint? = null
                if (ampPointArray.size < 2) return@setOnTouchListener false
                for (i in 1 until ampPointArray.size - 1) {
                    val rectX =
                        (this.cVStart + ampPointArray[i].timing * this.layoutParams.height / 1000)
                    val rectY = ((255 - ampPointArray[i].amp) * this.layoutParams.height / 255)
                    val rect = Rect(
                        rectX - dPtoPX(8F),
                        rectY - dPtoPX(8F),
                        rectX + dPtoPX(8F),
                        rectY + dPtoPX(8F)
                    )
                    if (rect.contains(upX.toInt(), upY.toInt())) {
                        toRemove = ampPointArray[i]
                        break
                    }
                }
                setTouchMove(v,e, toRemove)
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
    }
    private fun setTouchMove(view: View, event: MotionEvent, toRemove : AmpPoint?){
        val downX = event.x
        val downY = event.y
        view.setOnTouchListener { v, e ->
            when(e.action){
                MotionEvent.ACTION_MOVE -> {
                    val mScaleTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
                    if((!(abs(e.x - downX) <= mScaleTouchSlop && abs(e.y - downY) <= mScaleTouchSlop))&& pointImageView != null){
                        val myShadowBuilder =  MyDragShadowBuilder(pointImageView!!)
                        val dragData = ClipData(pointImageView!!.tag as? CharSequence, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                            ClipData.Item(pointImageView!!.tag as? CharSequence))
                        v.startDragAndDrop(dragData,myShadowBuilder,null,0)
                    }else
                        return@setOnTouchListener true
                }
                else -> {}
            }
            if(toRemove != null) {
                this.ampPointArray.remove(toRemove)
                if (e.action == MotionEvent.ACTION_UP)
                    this.cV.setOriginPathPoints(this.ampPointArray)
                    this.refreshWorkBoardDrawable(changedCV = true, changedCover = false, changedAmpPoint = true)
            }
            setTouchDown(v)
            return@setOnTouchListener false
        }
    }

    fun setWorkBoard(cV : CustomVibration , displayWidth : Int , pinWidth : Int){
        this.cV = cV
        this.ampPointArray = cV.pathPoints
        this.displayWidth = displayWidth
        refreshWorkBoardDrawable(changedCV = true, changedCover = true, changedAmpPoint = true)


        leftX = (this.displayWidth - cVBitmap.width)/2
        rightX = (this.displayWidth - cVBitmap.width)/2
        cVStart = leftX
        cVEnd = this.displayWidth - leftX
    }

    private fun setAmpPointsDrawable(){
        val ampPointBitmap = Bitmap.createBitmap((this.layoutParams.height * cV.duration / 1000F).toInt(),this.layoutParams.height,
            Bitmap.Config.ARGB_8888)
        val canvas = Canvas(ampPointBitmap)
        val scaleX = ampPointBitmap.width.toFloat() / cV.duration
        val scaleY = ampPointBitmap.height.toFloat() / 255F

        for (point in ampPointArray){
            val cx = (point.timing * scaleX)
            val cy = ((255-point.amp) * scaleY )
            canvas.drawCircle(cx,cy,dPtoPX(6F).toFloat(),pointPaint)
        }
        ampPointsDrawable = BitmapDrawable(resources,ampPointBitmap)
        ampPointsDrawable.gravity = Gravity.CENTER
    }
    private fun setCVBitmapDrawable(){
        cVBitmap = cV.makeBitmap()
        cVBitmap = Bitmap.createScaledBitmap(cVBitmap,(this.layoutParams.height * cV.duration / 1000F).toInt(), this.layoutParams.height,false)
        cVBitmapDrawable = BitmapDrawable(resources,cVBitmap)
        cVBitmapDrawable.gravity = Gravity.CENTER
    }
    private fun setCoverDrawable(){
        coverBitmap = Bitmap.createBitmap(this.displayWidth,this.layoutParams.height, Bitmap.Config.ARGB_8888)
        coverCanvas = Canvas(coverBitmap)
        val leftRect = Rect(0,0,leftX ,this.layoutParams.height)
        val rightRect = Rect(this.displayWidth - rightX,0,this.displayWidth,this.layoutParams.height)
        coverCanvas.drawRect(leftRect,coverPaint)
        coverCanvas.drawRect(rightRect,coverPaint)
        coverDrawable = BitmapDrawable(resources,coverBitmap)
    }
    fun refreshWorkBoardDrawable(changedCV : Boolean, changedCover : Boolean, changedAmpPoint: Boolean){
        if(changedCV) setCVBitmapDrawable()
        if(changedCover) setCoverDrawable()
        if(changedAmpPoint) setAmpPointsDrawable()
        val layerDrawable = LayerDrawable(arrayOf(cVBitmapDrawable,coverDrawable,ampPointsDrawable))
        this.setImageDrawable(layerDrawable)
    }
    var newAmp : AmpPoint? = null
    fun newAmpPoint(e: DragEvent){
        if(e.action == DragEvent.ACTION_DRAG_ENDED || e.action == DragEvent.ACTION_DRAG_EXITED){
            if(newAmp == null) return
        }else{
            var timing : Int = ((e.x - cVStart)* 1000F / (this.layoutParams.height)).roundToInt()
            if(timing < 0) timing = 0
            if(timing > this.cV.duration) timing = this.cV.duration

            var amp : Int = (255 - e.y * 255F / this.layoutParams.height).roundToInt()
            if(amp < 0) amp = 0
            if(amp > 255) amp = 255

            newAmp = AmpPoint(timing,amp)
        }

        for(i in 1 until ampPointArray.size){
            if(newAmp!!.timing <= ampPointArray[i].timing){
                ampPointArray.add(i, newAmp!!)
                cV.setOriginPathPoints(ampPointArray)
                break
            }
        }
        when(e.action){
            DragEvent.ACTION_DRAG_LOCATION , DragEvent.ACTION_DRAG_ENTERED ->{
                refreshWorkBoardDrawable(changedCV = true, changedCover = false, changedAmpPoint = true)
                ampPointArray.remove(newAmp!!)
            }
            DragEvent.ACTION_DRAG_EXITED ->{
                refreshWorkBoardDrawable(changedCV = true, changedCover = false, changedAmpPoint = true)
                ampPointArray.remove(newAmp!!)
            }
            DragEvent.ACTION_DROP ->{
                refreshWorkBoardDrawable(changedCV = true, changedCover = false, changedAmpPoint = true)
                newAmp = null
            }
            else -> {
                refreshWorkBoardDrawable(changedCV = true, changedCover = false, changedAmpPoint = true)
                newAmp = null
            }
        }
        return
    }

    fun makeNewCustomVibration() : CustomVibration{
        val frontCutTiming = startPoint()
        val endCutTiming = endPoint()
        val tempArray = cV.arrayOnOff
        while(tempArray.isNotEmpty()){
            if(tempArray.first().stTime >= frontCutTiming ) break
            else{
                if(tempArray.first().fnTime > frontCutTiming){
                    tempArray.add(0, OnOffVibration(frontCutTiming.toLong(),tempArray.first().fnTime))
                    tempArray.removeAt(1)
                    break
                }else{
                    tempArray.removeAt(0)
                }
            }
        }
        while(tempArray.isNotEmpty()){
            if(tempArray.last().fnTime <= endCutTiming) break
            else{
                if(tempArray.last().stTime < endCutTiming){
                    tempArray.add(tempArray.size - 1, OnOffVibration(tempArray.last().stTime,endCutTiming.toLong()))
                    tempArray.removeAt(tempArray.size - 1)
                    break
                }else{
                    tempArray.removeAt(tempArray.size-1)
                }
            }
        }
        val newOnOffArray = mutableListOf<OnOffVibration>()
        for (onOff in tempArray){
            newOnOffArray.add(OnOffVibration(onOff.stTime - frontCutTiming, onOff.fnTime - frontCutTiming))
        }
        val newPathPoint = mutableListOf<AmpPoint>()
        newPathPoint.add(AmpPoint(frontCutTiming,0))
        if(cV.findAmp(frontCutTiming) != 0)
            newPathPoint.add(AmpPoint(frontCutTiming,cV.findAmp(frontCutTiming)))

        for(point in this.ampPointArray){
            if(point.timing in (frontCutTiming + 1) until endCutTiming)
                newPathPoint.add(point)
        }
        if(cV.findAmp(endCutTiming) != 0)
            newPathPoint.add(AmpPoint(endCutTiming,cV.findAmp(endCutTiming)))
        newPathPoint.add(AmpPoint(endCutTiming,0))
        for(point in newPathPoint){
            point.timing -= frontCutTiming
        }
        val newCV = CustomVibration(newOnOffArray as ArrayList<OnOffVibration>,endCutTiming - frontCutTiming,newPathPoint,cV.codeName)
        newCV.blockColor = cV.blockColor
        return newCV
    }
    private fun dPtoPX(dp : Float) : Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
}

