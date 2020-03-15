package cn.com.gxrb.master.audio

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cn.com.gxrb.lib.audio.IAudioRecord
import cn.com.gxrb.lib.audio.RbAudioArmMachine
import cn.com.gxrb.lib.audio.config.AudioConfig
import cn.com.gxrb.lib.audio.view.RbAudioActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    var audioCancel = false
    var startRecordY = 0F
    var start = 0L
    var iAudioRecord: IAudioRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWithPermission()

        val path =
            Environment.getExternalStorageDirectory().path + File.separator + "GXRB" + File.separator + "cache"
        AudioConfig.get().initDefault(this, path)
        btn_record.setOnClickListener {
            val intent = Intent(this, RbAudioActivity::class.java)
            startActivityForResult(intent, 11)
        }


//
//        iAudioRecord = v_record
//        btn_play.setOnClickListener {
//            var arm = RbAudioArmMachine.get()
//            arm.playRecord(arm.recordAudioPath!!, object : RbAudioArmMachine.AudioPlayCallback {
//                override fun playComplete() {
//
//                }
//            })
//        }
//        btn_record.setOnTouchListener { view, motionEvent ->
//            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
//                btn_play.visibility = View.GONE
//                tv_file.text = ""
//
//                audioCancel = true
//                startRecordY = motionEvent.y
//                iAudioRecord?.startRecording()
//                start = System.currentTimeMillis()
//
//                RbAudioArmMachine.get().startRecord(object : RbAudioArmMachine.AudioRecordCallback {
//                    override fun recordComplete(duration: Long) {
//                        if (audioCancel) {
//                            iAudioRecord?.stopRecording()
//                            return
//                        }
//                        if (duration < 500) {
//                            iAudioRecord?.tooShortRecording()
//                            return
//                        }
//                        iAudioRecord?.stopRecording()
//                        tv_file.text = RbAudioArmMachine.get().recordAudioPath
//                        btn_play.visibility = View.VISIBLE
//                    }
//                })
//            } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
//                if (motionEvent.y - startRecordY < -100) {
//                    audioCancel = true
//                    iAudioRecord?.cancelRecording()
//                } else {
//                    audioCancel = false
//                    iAudioRecord?.startRecording()
//                }
//            } else if (motionEvent.action == MotionEvent.ACTION_UP) {
//                audioCancel = motionEvent.y - startRecordY < -100
//                RbAudioArmMachine.get().stopRecord()
//            }
//            false
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 11 && resultCode == Activity.RESULT_OK) {
            Log.e("xxxx", "录音文件：${data?.getStringExtra("audio_path")}")
        } else {
            Log.e("xxxx", "取消录音了")
        }
    }

    private fun initWithPermission() {
        //6.0的机子
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                requestPermissions(permissions, 10)
                return
            }
        }
    }

}
