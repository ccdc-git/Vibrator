package ccdc.lib.customvibrator

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.VibrationEffect
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.math.roundToInt

data class OnOffVibration (
    var is_On : Boolean,
    var stTime : Long,
    var fnTime : Long)

data class AmpPoint(
    var x : Int,
    var y : Int)

class CustomVibration(var originArrayOnOff : MutableList<OnOffVibration>, var originDuration : Int, var codeName: String){
    private var originPathPoints: MutableList<AmpPoint>
    var blockColor = Color.BLACK
    var bgColor = Color.WHITE
    lateinit var arrayOnOff : MutableList<OnOffVibration>
        private set
    lateinit var pathPoints : MutableList<AmpPoint>
        private set
    var maxAmp : Int = 255
        private set
    var duration : Int = 0 //0으로 하면 init에서
        private set
    val mspv = 25 //milli second per vibration
    init { //기본생성자 originArrayOnOff ,  originDuration
        originPathPoints = mutableListOf(AmpPoint(0,255), AmpPoint(originDuration,255))
        changeDuration(originDuration)
    }

    constructor(arrayOnOff: ArrayList<OnOffVibration>, duration: Int, pathPoints: MutableList<AmpPoint>, codeName : String) : this(arrayOnOff,0,codeName){
        this.originPathPoints = pathPoints
        this.originDuration = duration
        this.codeName = codeName
        changeDuration(originDuration)
    }
    @Throws(ImportWrongFileException::class)
    constructor(fileInputStream: FileInputStream, fileName: String) : this(mutableListOf(),0, fileName) {
        val reader = fileInputStream.reader()
        val importBytes =  reader.readLines()
        /*
            1번째 줄 : originArrayOnOff
            2번째 줄 : originPathPoints
            3번째 줄 : originDuration
            4번째 줄 : duration
         */
        if(importBytes.size != 4) throw ImportWrongFileException("file has ${importBytes.size} lines")
        try{
            this.originArrayOnOff = stringToOnOffArray(importBytes[0])
            this.originPathPoints =stringToPathPoints(importBytes[1])
            this.originDuration = importBytes[2].toInt()
            changeDuration(importBytes[3].toInt())
        }catch (e: NumberFormatException){
            throw ImportWrongFileException("wrong Durations, they are not number")
        }
    }
    constructor(context: Context,codeName : String) : this(mutableListOf(),0,codeName){
        //codeName = (세기)_(스타카토여부) ex)forte_staccato
        val arrayOnOffStrings = context.getString(context.resources.getIdentifier("${codeName}_ArrayOnOff","string",context.packageName)).split(',')
        val pathPointsStrings = context.getString(context.resources.getIdentifier("${codeName}_PathPoints","string",context.packageName)).split(',')
        originPathPoints.clear()
        for (i in 0 until arrayOnOffStrings.size/2){
            originArrayOnOff.add(OnOffVibration(true,arrayOnOffStrings[i*2].toLong(),arrayOnOffStrings[i*2+1].toLong()))
        }
        for (i in 0 until pathPointsStrings.size/2){
            originPathPoints.add(AmpPoint(pathPointsStrings[i*2].toInt(),pathPointsStrings[i*2+1].toInt()))
        }
        originDuration = 1000
        //Log.v("originArrayOnOff",originArrayOnOff.toString())
        //Log.v("originPathPoints",originPathPoints.toString())
        changeDuration(originDuration)
        this.blockColor = when(codeName.split("_")[0]){
            "piano"-> Color.RED
            "forte" -> Color.BLUE
            "crescendo" ->Color.GREEN
            "decrescendo" -> Color.BLACK
            else -> Color.MAGENTA
        }
    }


    fun changeDuration(to : Int): Boolean {
        /*
            지속시간을 바꿈
            to는 바꿀 지속시간
         */
        if (to == 0){
            return false
        }
        val scale = to.toFloat() / this.originDuration
        this.duration = to
        this.arrayOnOff =  mutableListOf()
        for(onOff in originArrayOnOff){
            this.arrayOnOff.add( OnOffVibration(true, (onOff.stTime * scale).toLong(), (onOff.fnTime*scale).toLong()) )
        }
        this.pathPoints = mutableListOf()
        for (point in originPathPoints){
            this.pathPoints.add(AmpPoint((point.x * scale).toInt(),point.y))
        }
        return true
    }
    fun changeMaxAmp(to : Int): Boolean{
        /*
            최대 진동 세기를 바꿈
            기본값은 255
            to는 바꿀 지속시간
         */
        if(to > 255 || to <= 0) return false
        maxAmp = to
        val scale = to.toFloat() / 255F
        for (i in 0 until originPathPoints.size) {
            this.pathPoints[i].y = (originPathPoints[i].y * scale).toInt()
        }
        return true
    }

    fun makeBitmap(blockColor: Int, bgColor : Int ): Bitmap? {
        val paint = Paint()
        val bgPaint = Paint()
        val bitmap = Bitmap.createBitmap(this.duration,255, Bitmap.Config.ARGB_8888)
        val ctCanvas = Canvas(bitmap)
        ctCanvas.drawColor(bgColor)

        val ampPath = Path()
            ampPath.moveTo(0F, 255F)
            for(point in this.pathPoints){
                ampPath.lineTo(point.x.toFloat(), 255F - point.y.toFloat())
            }
            ampPath.lineTo(duration.toFloat(),255F)
            ampPath.close()
        val blockPaths = Path()
        for(ons in this.arrayOnOff){
            blockPaths.addRect(ons.stTime.toFloat(),0F, ons.fnTime.toFloat(),255F,Path.Direction.CW)
        }
        blockPaths.addPath(ampPath)

        blockPaths.fillType = Path.FillType.INVERSE_EVEN_ODD
        ampPath.fillType = Path.FillType.INVERSE_WINDING

        paint.color = blockColor
        bgPaint.color = bgColor

        ctCanvas.drawPath(blockPaths,paint)
        ctCanvas.drawPath(ampPath,bgPaint)

        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getVibrationEffect() : VibrationEffect{
        //mspv (ms per vibe) 만큼 자르기

        val timings = MutableList(duration/mspv){ mspv.toLong() }
        val amplitudes = MutableList(duration/mspv){0}
        for (onOff in arrayOnOff){
            for(i in onOff.stTime/mspv until onOff.fnTime/mspv){
                amplitudes[i.toInt()] = findAmp((i*mspv).toInt())
            }
        }
        /*Log.v("timings",timings.toString())
        Log.v("amplitudes",amplitudes.toString())*/
        return VibrationEffect.createWaveform(timings.toLongArray(),amplitudes.toIntArray(),-1)
    }
    fun getTimingsArray(): MutableList<Long> {
        return MutableList(duration/mspv){ mspv.toLong() }
    }
    fun getAmplitudesArray() : MutableList<Int>{
        val amplitudes = MutableList(duration/mspv){0}
        for (onOff in arrayOnOff){
            for(i in onOff.stTime/mspv until onOff.fnTime/mspv){
                amplitudes[i.toInt()] = findAmp((i*mspv).toInt())
            }
        }
        return amplitudes
    }


    fun saveAsFile(fileOutputStream: FileOutputStream){
        Log.i("save arrayOnOff", originArrayOnOff.toString())
        Log.i("save originPathPoints", originPathPoints.toString())
        val stArOF = originArrayOnOff.toString()
        val stPP = originPathPoints.toString()

        fileOutputStream.write(stArOF.toByteArray())
        fileOutputStream.write("\n".toByteArray())
        fileOutputStream.write(stPP.toByteArray())
        fileOutputStream.write("\n".toByteArray())
        fileOutputStream.write(originDuration.toString().toByteArray())
        fileOutputStream.write("\n".toByteArray())
        fileOutputStream.write(duration.toString().toByteArray())
        fileOutputStream.close()
    }

    @Throws(ImportWrongFileException::class)
    private fun stringToOnOffArray(inp : String): MutableList<OnOffVibration> {
        val tempList = inp.replace("[","").replace("]","").replace(" ","").split(",","(",")")
        val arraySize = tempList.size / 5
        val onOffArray = mutableListOf<OnOffVibration>()
        try {
            for (i in 0 until arraySize) {
                onOffArray.add(
                    OnOffVibration(
                        tempList[i * 5 + 1].replace("is_On=", "").toBoolean(),
                        tempList[i * 5 + 2].replace("stTime=", "").toLong(),
                        tempList[i * 5 + 3].replace("fnTime=", "").toLong()
                    )
                )
            }
        }catch (e: Exception){
            throw ImportWrongFileException("wrong OnOffArray String, can't change to OnOffArray")
        }
        return onOffArray
    }
    @Throws(ImportWrongFileException::class)
    private fun stringToPathPoints(inp : String): MutableList<AmpPoint> {
        val tempList = inp.replace("[","").replace("]","").replace(" ","").split(",","(",")")
        val arraySize = tempList.size / 4
        val points = mutableListOf<AmpPoint>()
        try {
            for (i in 0 until arraySize) {
                points.add(
                    AmpPoint(
                        tempList[i * 4 + 1].replace("x=", "").toInt(),
                        tempList[i * 4 + 2].replace("y=", "").toInt()
                    )
                )
            }
        }catch (e: Exception){
            throw ImportWrongFileException("wrong PathPoints String, can't change to PathPoints")
        }
        return points
    }
    private fun findAmp(time : Int) : Int{
        if(pathPoints.size == 0) return 0
        if (time < pathPoints[0].x){ //맨 처음 point 보다 앞에 있을 때
            return xyTime(0,0,pathPoints[0].x,pathPoints[0].y,time)
        }
        for (i in 0 until pathPoints.size-1){
            if(pathPoints[i].x <= time && time < pathPoints[i+1].x){
                return xyTime(pathPoints[i].x,pathPoints[i].y,pathPoints[i+1].x,pathPoints[i+1].y,time)
            }
        }
        return xyTime(pathPoints.last().x,pathPoints.last().y,duration,0,time)
    }
    private fun xyTime(x1 : Int, y1 : Int, x2 : Int, y2 : Int, time : Int) : Int = if(x1 == x2) y1 else (((y1 - y2) / (x1 - x2).toFloat()) * (time - x1) + y1).roundToInt()

}