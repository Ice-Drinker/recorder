package cn.com.gxrb.lib.audio.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import cn.com.gxrb.lib.audio.R
import cn.com.gxrb.lib.audio.RbAudioArmMachine

class RbAudioActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            val audioWindow = RbAudioPopup(this)
            audioWindow.setOnDismissListener {
                val intent = Intent().apply {
                    putExtra("audio_path", RbAudioArmMachine.get().recordAudioPath)
                }
                setResult(audioWindow.isOk, intent)
                finish()
            }
            audioWindow.showAtLocation(window.decorView, Gravity.BOTTOM, 0, 0)
        }, 50)
    }
}