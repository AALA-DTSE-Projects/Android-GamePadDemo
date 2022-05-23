package jp.huawei.a2hdemo.app

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.huawei.ohos.localability.AbilityUtils
import com.unity3d.player.UnityPlayer
import jp.huawei.a2hdemo.IGameInterface
import jp.huawei.a2hdemo.remote.ResultServiceProxy

class GameService : Service() {

    companion object {
        const val UNITY_ACTIVITY_NAME = "com.unity3d.player.UnityPlayerActivity"
        const val UNITY_GAME_OBJECT_NAME = "HuaweiController"
        const val ADD = "Add"
        const val MOVE = "Move"
        const val BUTTON_CLICK = "ButtonClick"
        const val REMOVE = "Remove"
        const val HARMONY_BUNDLE_NAME = "com.huawei.gamepaddemo"
        const val HARMONY_ABILITY_NAME = "com.huawei.gamepaddemo.ResultServiceAbility"
    }

    private var mApplication: Application? = null
    private var activityManager: ActivityManager? = null
    private var resultServiceProxy: ResultServiceProxy? = null
    private var deviceId: String? = null

    override fun onBind(intent: Intent?): IBinder {
        mApplication = application
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        resultServiceProxy?.disconnect(deviceId ?: return)
    }

    private val binder = object : IGameInterface.Stub() {
        override fun start(deviceId: String) {
            if (!isApplicationRunning()) {
                startUnityPlayer()
            }
            connectToHarmonyService {
                it.connect(deviceId)
                this@GameService.deviceId = deviceId
            }
            mApplication?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityResumed(activity: Activity) {}

                override fun onActivityPaused(activity: Activity) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityDestroyed(activity: Activity) {
                    if (activity::class.java.simpleName == UNITY_ACTIVITY_NAME) {
                        resultServiceProxy?.disconnect(deviceId)
                    }
                }

            })
            UnityPlayer.UnitySendMessage(
                UNITY_GAME_OBJECT_NAME,
                ADD,
                deviceId
            )
        }

        override fun move(deviceId: String, angle: Int) {
            UnityPlayer.UnitySendMessage(
                UNITY_GAME_OBJECT_NAME,
                MOVE,
                "$deviceId;${Math.toRadians(angle.toDouble())}"
            )
        }

        override fun buttonClick(deviceId: String, buttonId: String) {
            UnityPlayer.UnitySendMessage(
                UNITY_GAME_OBJECT_NAME,
                BUTTON_CLICK,
                "$deviceId;$buttonId"
            )
        }

        override fun finish(deviceId: String) {
            UnityPlayer.UnitySendMessage(
                UNITY_GAME_OBJECT_NAME,
                REMOVE,
                deviceId
            )
        }

        private fun isApplicationRunning(): Boolean {
            activityManager?.let { activityManager ->
                val processes = activityManager.runningAppProcesses
                processes.forEach {
                    if (it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        it.pkgList.forEach { process ->
                            if (process == packageName) {
                                return true
                            }
                        }
                    }
                }
                return false
            } ?: return false
        }

        private fun connectToHarmonyService(callback: (ResultServiceProxy) -> Unit)  {
            val intent = Intent()
            val componentName = ComponentName(HARMONY_BUNDLE_NAME, HARMONY_ABILITY_NAME)
            intent.component = componentName
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    resultServiceProxy = ResultServiceProxy(service)
                    callback.invoke(resultServiceProxy ?: return)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    resultServiceProxy = null
                }
            }
            AbilityUtils.connectAbility(this@GameService, intent, connection)
        }

        private fun startUnityPlayer() {
            try {
                val unityPlayerClass = Class.forName(UNITY_ACTIVITY_NAME);
                val intent = Intent(this@GameService, unityPlayerClass)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}