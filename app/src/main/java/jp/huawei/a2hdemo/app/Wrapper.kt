package jp.huawei.a2hdemo.app

import android.app.Activity
import android.content.Intent
import android.os.Build

class Wrapper {
    private var activity: Activity? = null

    fun setupActivity(activity: Activity) {
        this.activity = activity
    }

    fun startService() {
        activity?.let {
            val intent = Intent(it, GameService::class.java)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                it.startService(intent)
            } else {
                it.startForegroundService(intent)
            }
        }
    }

    fun stopService() {
        activity?.let {
            val intent = Intent(it, GameService::class.java)
            it.stopService(intent)
        }
    }
}