package cn.com.gxrb.master.audio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class LineWaveVoiceView : View {
    private val DEFAULT_TEXT = " 请录音 "
    private val LINE_WIDTH = 9 //默认矩形波纹的宽度，9像素, 原则上从layout的attr获得

    private val paint: Paint = Paint()
    private var task: Runnable? = null
    private val executorService: ExecutorService = Executors.newCachedThreadPool()
    private val rectRight = RectF() //右边波纹矩形的数据，10个矩形复用一个rectF

    private val rectLeft = RectF() //左边波纹矩形的数据

    private var text = DEFAULT_TEXT
    private var updateSpeed = 0
    private var lineColor = 0
    private var textColor = 0
    private var lineWidth = 0f
    private var textSize = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs, context)
        resetView(mWaveList, DEFAULT_WAVE_HEIGHT)
        task = LineJitterTask()
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(attrs, context)
        resetView(mWaveList, DEFAULT_WAVE_HEIGHT)
        task = LineJitterTask()
    }

    private fun initView(attrs: AttributeSet, context: Context) { //获取布局属性里的值
        lineColor = Color.RED
        lineWidth = 20f
        textSize = 42f
        textColor = Color.BLACK
        updateSpeed = UPDATE_INTERVAL_TIME
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //获取实际宽高的一半
        val widthCentre = width / 2
        val heightCentre = height / 2
        paint.setStrokeWidth(0F)
        paint.setColor(textColor)
        paint.setTextSize(textSize)
        val textWidth: Float = paint.measureText(text)
        canvas.drawText(
            text,
            widthCentre - textWidth / 2,
            heightCentre - (paint.ascent() + paint.descent()) / 2,
            paint
        )
        //设置颜色
        paint.setColor(lineColor)
        //填充内部
        paint.setStyle(Paint.Style.FILL)
        //设置抗锯齿
        paint.setAntiAlias(true)

        if(mWaveList.size < 9)
            return
        for (i in 0..9) {
            rectRight.left = widthCentre + textWidth / 2 + (1 + 2 * i) * lineWidth
            rectRight.top = heightCentre - lineWidth * mWaveList.get(i) / 2
            rectRight.right = widthCentre + textWidth / 2 + (2 + 2 * i) * lineWidth
            rectRight.bottom = heightCentre + lineWidth * mWaveList.get(i) / 2
            //左边矩形
            rectLeft.left = widthCentre - textWidth / 2 - (2 + 2 * i) * lineWidth
            rectLeft.top = heightCentre - mWaveList.get(i) * lineWidth / 2
            rectLeft.right = widthCentre - textWidth / 2 - (1 + 2 * i) * lineWidth
            rectLeft.bottom = heightCentre + mWaveList.get(i) * lineWidth / 2
            canvas.drawRoundRect(rectRight, 6f, 6f, paint)
            canvas.drawRoundRect(rectLeft, 6f, 6f, paint)
        }
    }

    private val MIN_WAVE_HEIGHT = 2 //矩形线最小高

    private val MAX_WAVE_HEIGHT = 12 //矩形线最大高

    private val DEFAULT_WAVE_HEIGHT = intArrayOf(2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
    private val UPDATE_INTERVAL_TIME = 100 //100ms更新一次

    private val mWaveList: LinkedList<Int> = LinkedList()
    private var maxDb = 0f

    private fun resetView(
        list: MutableList<Int>,
        array: IntArray
    ) {
        list.clear()
        for (anArray in array) {
            list.add(anArray)
        }
    }

    @Synchronized
    private fun refreshElement() {
        val random = Random()
        maxDb = (random.nextInt(5) + 2).toFloat()
        val waveH =
            MIN_WAVE_HEIGHT + Math.round(maxDb * (MAX_WAVE_HEIGHT - MIN_WAVE_HEIGHT))
        mWaveList.add(0, waveH)
        mWaveList.removeLast()
    }

    var isStart = false

    private inner class LineJitterTask : Runnable {
        override fun run() {
            while (isStart) {
                refreshElement()
                try {
                    Thread.sleep(updateSpeed.toLong())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                postInvalidate()
            }
        }
    }

    @Synchronized
    fun startRecord() {
        isStart = true
        executorService.execute(task)
    }

    @Synchronized
    fun stopRecord() {
        isStart = false
        mWaveList.clear()
        resetView(mWaveList, DEFAULT_WAVE_HEIGHT)
        postInvalidate()
    }


    @Synchronized
    fun setText(text: String) {
        this.text = text
        postInvalidate()
    }

    fun setUpdateSpeed(updateSpeed: Int) {
        this.updateSpeed = updateSpeed
    }
}