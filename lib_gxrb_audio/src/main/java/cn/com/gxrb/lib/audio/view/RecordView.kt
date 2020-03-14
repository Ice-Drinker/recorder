package cn.com.gxrb.lib.audio.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import cn.com.gxrb.lib.audio.IAudioRecord
import cn.com.gxrb.lib.audio.R
import kotlinx.android.synthetic.main.view_audio.view.*

class RecordView(context: Context, attributes: AttributeSet) : FrameLayout(context, attributes),
    IAudioRecord {

    var mVolumeAnim: AnimationDrawable? = null

    init {
        View.inflate(context, R.layout.view_audio, this)
    }

    override fun popupAreaShow() {

    }

    override fun popupAreaHide() {

    }

    override fun startRecording() {
        recording_icon.setImageResource(R.drawable.recording_volume)
        mVolumeAnim = recording_icon.drawable as AnimationDrawable?
        voice_recording_view.visibility = View.VISIBLE
        mVolumeAnim?.start()
        recording_tips.text = "上滑取消录音"
        recording_tips.setTextColor(Color.WHITE)
    }

    override fun stopRecording() {
        post {
            mVolumeAnim?.stop()
            voice_recording_view.visibility = View.GONE
        }
    }

    override fun tooShortRecording() {
        mVolumeAnim?.stop()
        recording_icon.setImageResource(R.drawable.exclama)
        recording_tips.text = "说话时间太短"
        recording_tips.setTextColor(Color.WHITE)

        postDelayed({
            voice_recording_view.visibility = View.GONE
        }, 1000)
    }

    override fun cancelRecording() {
        recording_icon.setImageResource(R.drawable.recording_cancel)
        recording_tips.text = "松开手指，取消发送"
        recording_tips.setTextColor(Color.RED)
    }
}