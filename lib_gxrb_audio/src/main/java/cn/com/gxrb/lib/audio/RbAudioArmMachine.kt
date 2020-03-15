package cn.com.gxrb.lib.audio

import android.media.MediaPlayer
import android.media.MediaRecorder
import cn.com.gxrb.lib.audio.config.AudioConfig
import cn.com.gxrb.lib.audio.utils.AudioUIUtils
import java.io.File

class RbAudioArmMachine private constructor() {
    var isPlayingRecord = false
        private set
    private var innerRecording = false
    @Volatile
    var recording = false
    private var mRecordCallback: AudioRecordCallback? = null
    private var mPlayCallback: AudioPlayCallback? = null
    var recordAudioPath: String? = null
        private set
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var mPlayer: MediaPlayer? = null
    private var mRecorder: MediaRecorder? = null
    private val REPEAT_INTERVAL = 40L

    val duration: Int
        get() = (endTime - startTime).toInt()

    fun startRecord(callback: AudioRecordCallback) {
        synchronized(recording) {
            mRecordCallback = callback
            recording = true
            RecordThread().start()
        }
    }

    fun stopRecord() {
        synchronized(recording) {
            if (recording) {
                recording = false
                endTime = System.currentTimeMillis()
                if (mRecordCallback != null) mRecordCallback!!.recordComplete(endTime - startTime)
                if (mRecorder != null && innerRecording) {
                    try {
                        innerRecording = false
                        mRecorder!!.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun playRecord(filePath: String, callback: AudioPlayCallback) {
        mPlayCallback = callback
        PlayThread(filePath).start()
    }

    fun stopPlayRecord() {
        if (mPlayer != null) {
            mPlayer!!.stop()
            isPlayingRecord = false
            mPlayCallback!!.playComplete()
        }
    }

    interface AudioRecordCallback {
        fun recordComplete(duration: Long)
    }

    interface AudioPlayCallback {
        fun playComplete()
    }

    private inner class RecordThread : Thread() {
        override fun run() { //根据采样参数获取每一次音频采样大小
            try {
                mRecorder = MediaRecorder()
                mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                //RAW_AMR虽然被高版本废弃，但它兼容低版本还是可以用的
                mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR)
                recordAudioPath =
                    CURRENT_RECORD_FILE + System.currentTimeMillis()
                mRecorder!!.setOutputFile(recordAudioPath)
                mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                startTime = System.currentTimeMillis()

                val audioFile = File(CURRENT_RECORD_FILE)
                if (!audioFile.exists())
                    audioFile.mkdirs()

                synchronized(recording) {
                    if (!recording) return
                    mRecorder!!.prepare()
                    mRecorder!!.start()
                }
                innerRecording = true

                object : Thread() {
                    override fun run() {
                        while (recording && innerRecording) {
                            try {
                                sleep(200)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            if (System.currentTimeMillis() - startTime >= AudioConfig.get().audioRecordMaxTime) {
                                stopRecord()
                                return
                            }
                        }
                    }
                }.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private inner class PlayThread internal constructor(var audioPath: String) :
        Thread() {
        override fun run() {
            try {
                mPlayer = MediaPlayer()
                mPlayer!!.setDataSource(audioPath)
                mPlayer!!.setOnCompletionListener {
                    mPlayCallback!!.playComplete()
                    isPlayingRecord = false
                }
                mPlayer!!.prepare()
                mPlayer!!.start()
                isPlayingRecord = true
            } catch (e: Exception) {
                AudioUIUtils.toastLongMessage("语音文件已损坏或不存在")
                e.printStackTrace()
                mPlayCallback!!.playComplete()
                isPlayingRecord = false
            }
        }

    }

    fun getMediaRecorder(): MediaRecorder? {
        return mRecorder
    }

    fun getMediaPlayer(): MediaPlayer? {
        return mPlayer
    }

    companion object {
        var CURRENT_RECORD_FILE =
            AudioConfig.get().audioPath + File.separator + "auto_"

        fun get(): RbAudioArmMachine {
            return instance
        }

        val instance = RbAudioArmMachine()
    }
}