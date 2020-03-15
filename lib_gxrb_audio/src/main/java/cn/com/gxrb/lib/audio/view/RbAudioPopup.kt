package cn.com.gxrb.lib.audio.view

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import cn.com.gxrb.lib.audio.R

class RbAudioPopup(context: Context) : PopupWindow(context) {

    var isOk = 0

    init {
        contentView = View.inflate(context, R.layout.audio_pop, null)
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        isOutsideTouchable = false
        setBackgroundDrawable(context.resources.getDrawable(android.R.color.transparent))

        contentView.findViewById<View>(R.id.tv_cancel).setOnClickListener {
            dismiss()
        }
        contentView.findViewById<View>(R.id.tv_upload).setOnClickListener {
            isOk = Activity.RESULT_OK
            dismiss()
        }
    }
}