package cn.com.gxrb.lib.audio.config

import android.content.Context

class AudioConfig private constructor(){

    lateinit var context:Context
    lateinit var audioPath:String
        private set
    var audioRecordMaxTime = 60 * 1000; //录音最大时长

    companion object {
        private var instance = AudioConfig()

        fun get():AudioConfig {
            return instance
        }
    }

    fun initDefault(context:Context, audioPath:String) {
        this.context = context
        this.audioPath = audioPath
    }

}