package cn.com.gxrb.lib.audio.view

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import cn.com.gxrb.lib.audio.IAudioRecord
import cn.com.gxrb.lib.audio.R
import cn.com.gxrb.lib.audio.RbAudioArmMachine
import cn.com.gxrb.lib.audio.config.AudioConfig
import cn.com.gxrb.lib.audio.utils.AudioUIUtils
import kotlinx.android.synthetic.main.view_recorder.view.*
import java.util.*

class RbRecorderView : FrameLayout, IAudioRecord {

    val WHAT_STOP_RECORD = 10
    val WHAT_AMPLITUDE = 11
    val WHAT_TIME_RECORD = 12
    val WHAT_TIME_PLAY = 13
    val WHAT_STOP_PLAY = 14
    val REPEAT_INTERVAL = 40L
    val ONE_SECOND = 1000L

    private var mFormatBuilder: StringBuilder? = null
    private var mFormatter: Formatter? = null
    var startTime = 0L
    var maxLength = AudioConfig.get().audioRecordMaxTime  //录音最长一分钟
    var voiceAnim: AnimationDrawable? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        View.inflate(context, R.layout.view_recorder, this)
        mFormatBuilder = java.lang.StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())

        iv_start.setOnClickListener {
            RbAudioArmMachine.get().startRecord(object : RbAudioArmMachine.AudioRecordCallback {
                override fun recordComplete(duration: Long) {
                    if (duration < 500) {
                        tooShortRecording()
                        return
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(WHAT_STOP_RECORD))
                }
            })
            startRecording()
        }
        iv_end.setOnClickListener {
            val arm = RbAudioArmMachine.get()
            if (arm.recording) {
                arm.stopRecord()
            } else if (arm.isPlayingRecord) {
                arm.stopPlayRecord()
            }
        }
        ll_voice.setOnClickListener {
            val arm = RbAudioArmMachine.get()
            if (arm.isPlayingRecord) {
                arm.stopPlayRecord()
                stopPlayRecord()
            } else {
                playRecord()
                arm.playRecord(
                    arm.recordAudioPath!!,
                    object : RbAudioArmMachine.AudioPlayCallback {
                        override fun playComplete() {
                            mHandler.sendMessage(mHandler.obtainMessage(WHAT_STOP_PLAY))
                        }
                    })
            }
        }
    }

    val mHandler = Handler(HandlerBack())

    var p = 0

    inner class HandlerBack : Handler.Callback {
        override fun handleMessage(p0: Message): Boolean {
            when (p0.what) {
                WHAT_STOP_RECORD -> {
                    stopRecording()
                }
                WHAT_AMPLITUDE -> {
                    val x = RbAudioArmMachine.get().getMediaRecorder()!!.maxAmplitude
                    updateAmplitude(x.toFloat())
                    val aMsg = mHandler.obtainMessage(WHAT_AMPLITUDE)
                    mHandler.sendMessageDelayed(aMsg, REPEAT_INTERVAL)
                }
                WHAT_TIME_RECORD -> {
                    val now = System.currentTimeMillis()
                    val duration = now - startTime
                    if (p++ <= maxLength / ONE_SECOND) {
                        tv_time.visibility = View.VISIBLE
                        tv_time.text = stringForTime(duration)
                        val tMsg = mHandler.obtainMessage(WHAT_TIME_RECORD)
                        mHandler.sendMessageDelayed(tMsg, ONE_SECOND)
                    } else {
                        p = 0
                        iv_end.performClick()
                    }
                }
                WHAT_TIME_PLAY -> {
                    val now = System.currentTimeMillis()
                    val duration = now - startTime
                    tv_duration.text = stringForTime(duration)

                    val pMsg = mHandler.obtainMessage(WHAT_TIME_PLAY)
                    mHandler.sendMessageDelayed(pMsg, ONE_SECOND)
                }
                WHAT_STOP_PLAY -> {
                    stopPlayRecord()
                }
            }
            return false
        }

    }

    override fun updateAmplitude(x: Float) {
        view_recorder_visualizer.addAmplitude(x); // update the VisualizeView
        view_recorder_visualizer.invalidate(); // refresh the VisualizerView
    }

    override fun startRecording() {
        view_recorder_visualizer.clear()
        iv_start.visibility = View.GONE
        iv_end.visibility = View.VISIBLE
        view_recorder_visualizer.visibility = View.VISIBLE
        tv_time.visibility = View.VISIBLE
        fl_voice.visibility = View.GONE

        startTime = System.currentTimeMillis()
        val msg = mHandler.obtainMessage(WHAT_AMPLITUDE)
        mHandler.sendMessageDelayed(msg, REPEAT_INTERVAL)
        val timeMsg = mHandler.obtainMessage(WHAT_TIME_RECORD)
        mHandler.sendMessage(timeMsg)
    }

    override fun stopRecording() {
        iv_start.visibility = View.VISIBLE
        iv_end.visibility = View.GONE
        view_recorder_visualizer.clear()
        view_recorder_visualizer.visibility = View.GONE
        tv_time.visibility = View.INVISIBLE
        fl_voice.visibility = View.VISIBLE
        calcVoiceWidth()

        val voicePath = RbAudioArmMachine.get().recordAudioPath
        voicePath?.let {
            tv_duration.text = stringForTime(getDuration(voicePath).toLong())
        }

        mHandler.removeMessages(WHAT_AMPLITUDE)
        mHandler.removeMessages(WHAT_TIME_RECORD)
    }

    override fun tooShortRecording() {
        AudioUIUtils.toastShortMessage("时间太短了")
    }

    override fun cancelRecording() {

    }

    override fun playRecord() {
        startTime = System.currentTimeMillis()
        view_recorder_visualizer.clear()
        view_recorder_visualizer.visibility = View.GONE
        iv_start.visibility = View.GONE
        iv_end.visibility = View.GONE
        tv_time.visibility = View.INVISIBLE
        fl_voice.visibility = View.VISIBLE
        iv_voice.setImageResource(R.drawable.voice_speak_anim)
        voiceAnim = iv_voice.drawable as AnimationDrawable?
        voiceAnim?.start()

        mHandler.sendMessage(mHandler.obtainMessage(WHAT_TIME_PLAY))
    }

    override fun stopPlayRecord() {
        iv_start.visibility = View.VISIBLE
        iv_end.visibility = View.GONE
        voiceAnim?.stop()
        iv_voice.setImageResource(R.drawable.voice_speak)
        val voicePath = RbAudioArmMachine.get().recordAudioPath
        voicePath?.let {
            tv_duration.text = stringForTime(getDuration(voicePath).toLong())
        }

        mHandler.removeMessages(WHAT_TIME_PLAY)
    }

    private fun stringForTime(timeMs: Long): String? {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder!!.setLength(0)
        return if (hours > 0) {
            mFormatter!!.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter!!.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    fun getDuration(audioPath: String): Int {
        val player = MediaPlayer()
        player.setDataSource(audioPath)
        player.prepare()
        return player.duration
    }

    fun calcVoiceWidth() {
        var fx = fl_voice.measuredWidth
        if (fx == 0) {
            fx = measuredWidth - dip2px(context, 50f)
        }
        val duration = getDuration(RbAudioArmMachine.get().recordAudioPath!!)
        var lx = (fx * duration) / maxLength.toInt()
        if (lx < fx / 3) {
            lx = fx / 3
        }
        ll_voice.layoutParams.width = lx
    }

    fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}