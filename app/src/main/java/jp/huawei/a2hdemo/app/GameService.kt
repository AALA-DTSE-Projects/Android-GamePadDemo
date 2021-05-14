package jp.huawei.a2hdemo.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.preference.PreferenceManager
import jp.huawei.a2hdemo.IGameInterface
import jp.huawei.a2hdemo.local.Config
import jp.huawei.a2hdemo.local.HandleEvent
import org.greenrobot.eventbus.EventBus

class GameService : Service() {

    companion object {
        const val DEVICE_ID_KEY = "deviceId"
        const val START = "start"
        const val ADD = "add"
        const val UP = "up"
        const val DOWN = "down"
        const val LEFT = "left"
        const val RIGHT = "right"
        const val FINISH = "finish"
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private val binder = object : IGameInterface.Stub() {
        override fun action(deviceId: String?, action: String?) {
            deviceId ?: return
            action ?: return
            when (action) {
                START -> {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@GameService)
                    val config = Config(sharedPreferences)
                    if (config.isGameRunning) {
                        EventBus.getDefault().post(
                            HandleEvent(
                                deviceId,
                                ADD
                            )
                        )
                    } else {
                        val intent = Intent(this@GameService, MainActivity::class.java)
                        intent.putExtra(DEVICE_ID_KEY, deviceId)
                        startActivity(intent)
                    }
                }
                UP,
                DOWN,
                LEFT,
                RIGHT,
                FINISH -> {
                    EventBus.getDefault().post(
                        HandleEvent(deviceId, action)
                    )
                }
            }
        }
    }
}