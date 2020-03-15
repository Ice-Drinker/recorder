package cn.com.gxrb.lib.audio

interface IAudioRecord {

    fun updateAmplitude(x:Float)

    fun startRecording()

    fun stopRecording()

    fun playRecord()

    fun stopPlayRecord()

    fun tooShortRecording()

    fun cancelRecording()
}