package ccdc.lib.customvibrator

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.VibrationEffect
import androidx.annotation.RequiresApi
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.math.roundToInt

data class OnOffVibration (
    var stTime : Long,
    var fnTime : Long){
    override fun toString(): String {
        return "$stTime,$fnTime"
    }
}

data class AmpPoint(
    var timing : Int,
    var amp : Int){
    override fun toString(): String {
        return "$timing,$amp"
    }
}

class CustomVibration{
    private var originArrayOnOff : MutableList<OnOffVibration>
    private var originPathPoints: MutableList<AmpPoint>
    private var originDuration : Int = 0
    var codeName: String

    lateinit var arrayOnOff : MutableList<OnOffVibration>
        private set
    lateinit var pathPoints : MutableList<AmpPoint>
        private set
    var maxAmp : Int = 255
        private set
    var duration : Int = 0 //0으로 하면 init에서
        private set

    val mspv = 25 //milli second per vibration
    var blockColor = Color.BLACK
    var bgColor = Color.WHITE
    private val paint = Paint()
    private val bgPaint = Paint()

    //empty CustomVibration
    constructor(){
        this.originArrayOnOff = mutableListOf()
        this.originPathPoints = mutableListOf()
        this.arrayOnOff = mutableListOf()
        this.pathPoints = mutableListOf()
        this.duration = 1000
        codeName = ""
    }

    //argument all elements
    constructor(arrayOnOff: ArrayList<OnOffVibration>, duration: Int, pathPoints: MutableList<AmpPoint>, codeName : String) {
        this.originArrayOnOff = arrayOnOff
        this.originPathPoints = pathPoints
        this.originDuration = duration
        this.codeName = codeName
        changeDuration(originDuration)
    }
    //default pathPoints
    constructor(arrayOnOff: ArrayList<OnOffVibration>, duration: Int, codeName : String) {
        this.originArrayOnOff = arrayOnOff
        this.originPathPoints = mutableListOf(AmpPoint(0,0),AmpPoint(0,255),AmpPoint(duration,255), AmpPoint(duration,0))
        this.originDuration = duration
        this.codeName = codeName
        changeDuration(originDuration)
    }

    //import from file
    @Throws(ImportWrongFileException::class)
    constructor(fileInputStream: FileInputStream, fileName: String) {
        val importBytes =  fileInputStream.reader().readLines()
        /*
            1st line : originArrayOnOff
            2nd line : originPathPoints
            3rd line : originDuration
            4th line : duration
            5th line : blockColor
         */
        if(importBytes.size != 5) throw ImportWrongFileException("file has ${importBytes.size} lines")
        try{
            this.originArrayOnOff = stringToOnOffArray(importBytes[0])
            this.originPathPoints =stringToPathPoints(importBytes[1])
            this.originDuration = importBytes[2].toInt()
            changeDuration(importBytes[3].toInt())
            this.blockColor = importBytes[4].toInt()
        }catch (e: NumberFormatException){
            throw ImportWrongFileException("wrong Durations, they are not number")
        }
        this.codeName = fileName
    }

    //basic blocks
    constructor(context: Context,codeName : String) {
        //codeName = (세기)_(스타카토여부) ex)forte_staccato
        val arrayOnOffStrings = context.getString(context.resources.getIdentifier("${codeName}_ArrayOnOff","string",context.packageName)).split(',')
        val pathPointsStrings = context.getString(context.resources.getIdentifier("${codeName}_PathPoints","string",context.packageName)).split(',')
        this.originArrayOnOff = mutableListOf()
        this.originPathPoints = mutableListOf()
        for (i in 0 until arrayOnOffStrings.size/2){
            originArrayOnOff.add(OnOffVibration(arrayOnOffStrings[i*2].toLong(),arrayOnOffStrings[i*2+1].toLong()))
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
        this.codeName = codeName
    }


    fun changeDuration(to: Int): Boolean {
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
            this.arrayOnOff.add( OnOffVibration((onOff.stTime * scale).toLong(), (onOff.fnTime*scale).toLong()) )
        }
        this.pathPoints = mutableListOf()
        val scaleAmp = maxAmp / 255F
        for (point in originPathPoints){
            this.pathPoints.add(AmpPoint((point.timing * scale).toInt(), (point.amp *scaleAmp).toInt()))
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
            this.pathPoints[i].amp = (originPathPoints[i].amp * scale).toInt()
        }
        return true
    }

    fun makeBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(this.duration,255, Bitmap.Config.ARGB_8888)
            if(codeName == "") return bitmap //빈 bitmap 리턴
        val ctCanvas = Canvas(bitmap)
        ctCanvas.drawColor(bgColor)

        val ampPath = Path()
        ampPath.moveTo(0F, 255F)
        for(point in this.pathPoints){
            ampPath.lineTo(point.timing.toFloat(), 255F - point.amp.toFloat())
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

        /*Log.v("timings",timings.toString())
        Log.v("amplitudes",amplitudes.toString())*/
        return VibrationEffect.createWaveform(getTimingsArray().toLongArray(),getAmplitudesArray().toIntArray(),-1)
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
    fun setOriginPathPoints(newPathPoints : MutableList<AmpPoint>){
        this.originPathPoints = newPathPoints
        changeDuration(duration)
    }


    fun saveAsFile(fileOutputStream: FileOutputStream){
//        Log.i("save arrayOnOff", originArrayOnOff.toString())
//        Log.i("save originPathPoints", originPathPoints.toString())
        val stArOF = originArrayOnOff.joinToString("_")
        val stPP = originPathPoints.joinToString("_")

        fileOutputStream.write(stArOF.toByteArray())
        fileOutputStream.write("\n".toByteArray())
        fileOutputStream.write(stPP.toByteArray())
        fileOutputStream.write("\n".toByteArray())
        fileOutputStream.write(originDuration.toString().toByteArray())
        fileOutputStream.write("\n".toByteArray())
        fileOutputStream.write(duration.toString().toByteArray())
        fileOutputStream.write("\n".toByteArray())
        fileOutputStream.write(blockColor.toString().toByteArray())
        fileOutputStream.close()
    }

    @Throws(ImportWrongFileException::class)
    private fun stringToOnOffArray(inp : String): MutableList<OnOffVibration> {
        val tempList = inp.split("_")
        val onOffArray = mutableListOf<OnOffVibration>()
        try {
            if(inp=="") return onOffArray
            for (i in tempList) {
                val temp = i.split(",")
                onOffArray.add(OnOffVibration(temp[0].toLong(), temp[1].toLong()))
            }
        }catch (e: Exception){
            throw ImportWrongFileException("wrong OnOffArray String, can't change to OnOffArray")
        }
        return onOffArray
    }
    @Throws(ImportWrongFileException::class)
    private fun stringToPathPoints(inp : String): MutableList<AmpPoint> {
        val tempList = inp.split("_")
        val points = mutableListOf<AmpPoint>()
        try {
            for (i in tempList) {
                val temp = i.split(",")
                points.add(AmpPoint(temp[0].toInt(), temp[1].toInt()))
            }
        }catch (e: Exception){
            throw ImportWrongFileException("wrong PathPoints String, can't change to PathPoints")
        }
        return points
    }
    fun findAmp(timing : Int) : Int{
        if(pathPoints.size == 0) return 0
        if (timing < pathPoints[0].timing){ //맨 처음 point 보다 앞에 있을 때
            return xyTime(0,0,pathPoints[0].timing,pathPoints[0].amp,timing)
        }
        for (i in 0 until pathPoints.size-1){
            if(pathPoints[i].timing <= timing && timing < pathPoints[i+1].timing){
                return xyTime(pathPoints[i].timing,pathPoints[i].amp,pathPoints[i+1].timing,pathPoints[i+1].amp,timing)
            }
        }
        return xyTime(pathPoints.last().timing,pathPoints.last().amp,duration,0,timing)
    }
    private fun xyTime(x1 : Int, y1 : Int, x2 : Int, y2 : Int, time : Int) : Int = if(x1 == x2) y1 else (((y1 - y2) / (x1 - x2).toFloat()) * (time - x1) + y1).roundToInt()

}