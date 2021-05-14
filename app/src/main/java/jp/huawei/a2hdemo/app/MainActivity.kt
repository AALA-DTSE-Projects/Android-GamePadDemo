package jp.huawei.a2hdemo.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import jp.huawei.a2hdemo.R
import jp.huawei.a2hdemo.databinding.ActivityMainBinding
import jp.huawei.a2hdemo.local.Config
import jp.huawei.a2hdemo.local.HandleEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var config: Config
    private lateinit var players: HashMap<String, ImageView>

    companion object {
        const val DURATION = 1000L
        const val STEP = 100.0f
        const val DISTRIBUTED_PERMISSION_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        init()
        getData(intent)
    }

    override fun onResume() {
        super.onResume()
        config.isGameRunning = true
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        config.isGameRunning = false
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        config.isGameRunning = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHandleEvent(event: HandleEvent) {
        val deviceId = event.deviceId
        val imageView = players[deviceId]
        when (event.action) {
            GameService.ADD -> {
                val isInit = players.isEmpty()
                binding.numOfPlayers = if (isInit) 1 else 2
                binding.executePendingBindings()
                players[deviceId] = if (isInit) binding.human1 else binding.human2
            }
            GameService.UP -> {
                imageView?.animate()?.translationY(-STEP)?.apply {
                    duration = DURATION
                    start()
                }
            }
            GameService.DOWN -> {
                imageView?.animate()?.translationY(STEP)?.apply {
                    duration = DURATION
                    start()
                }
            }
            GameService.LEFT -> {
                imageView?.animate()?.translationX(-STEP)?.apply {
                    duration = DURATION
                    start()
                }
            }
            GameService.RIGHT -> {
                imageView?.animate()?.translationX(STEP)?.apply {
                    duration = DURATION
                    start()
                }
            }
            GameService.FINISH -> {
                finish()
            }
        }
    }

    private fun setupBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.numOfPlayers = 1
        disableClipOnParents(binding.human1)
        disableClipOnParents(binding.human2)
    }

    private fun disableClipOnParents(v: View) {
        if (v.parent == null) {
            return
        }
        if (v is ViewGroup) {
            v.clipChildren = false
        }
        if (v.parent is View) {
            disableClipOnParents(v.parent as View)
        }
    }

    private fun init() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        config = Config(sharedPreferences)
        players = hashMapOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestDistributedPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestDistributedPermission() {
        val requestedPermission = pickDistributedPermission()
        val message = when {
            requestedPermission == null -> {
                "Please add DISTRIBUTED permission to your MANIFEST"
            }
            checkSelfPermission(requestedPermission) == PackageManager.PERMISSION_DENIED -> {
                showPermissionRequest(requestedPermission)
                return
            }
            else -> {
                "permission $requestedPermission is already granted!"
            }
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermissionRequest(permission: String) {
        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(this)
                .setMessage("We need the permission to exchange data across device")
                .setCancelable(true)
                .setPositiveButton("OK") { _,_ ->
                    requestPermissions(
                        arrayOf(
                            permission
                        ), DISTRIBUTED_PERMISSION_CODE
                    )
                }.show()
        } else {
            requestPermissions(arrayOf(permission), DISTRIBUTED_PERMISSION_CODE)
        }
    }

    private fun pickDistributedPermission(): String? {
        val privilegedPermission = "com.huawei.hwddmp.servicebus.BIND_SERVICE"
        val dangerousPermission = "com.huawei.permission.DISTRIBUTED_DATASYNC"
        val isPrivileged: Boolean = isPrivilegedApp(this, Process.myUid())
        var candidate: String? = null
        val packageManager = this.packageManager
        try {
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            for (i in packageInfo.requestedPermissions.indices) {
                if (isPrivileged && privilegedPermission == packageInfo.requestedPermissions[i]) {
                    return privilegedPermission
                }
                if (candidate == null && dangerousPermission == packageInfo.requestedPermissions[i]) {
                    candidate = dangerousPermission
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return candidate
    }

    private fun isPrivilegedApp(context: Context, uid: Int): Boolean {
        if (uid == Process.SYSTEM_UID) {
            return true
        }
        val pm: PackageManager = context.packageManager ?: return false
        return pm.checkSignatures(uid, Process.SYSTEM_UID) == PackageManager.SIGNATURE_MATCH
    }

    private fun getData(intent: Intent) {
        val deviceId = intent.getStringExtra(GameService.DEVICE_ID_KEY)
        players[deviceId ?: return] = binding.human1
    }

}