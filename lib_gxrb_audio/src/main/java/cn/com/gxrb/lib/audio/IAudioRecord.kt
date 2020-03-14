package cn.com.gxrb.lib.audio

interface IAudioRecord {

    fun popupAreaShow()

    fun popupAreaHide()

    fun startRecording()

    fun stopRecording()

    fun tooShortRecording()

    fun cancelRecording()
}