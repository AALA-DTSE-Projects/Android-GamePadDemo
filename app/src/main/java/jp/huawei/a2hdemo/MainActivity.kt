package jp.huawei.a2hdemo

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.huawei.ohos.localability.AbilityUtils
import jp.huawei.a2hdemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        val BUNDLE_NAME = "com.huawei.superdevicedemo"
        val ABILITY_NAME = "com.huawei.superdevicedemo.MainAbility"
        val DISTRIBUTED_PERMISSION_CODE = 0
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestDistributedPermission()
        }
        binding.startButton.setOnClickListener {
            startHarmonyOSAbility()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            DISTRIBUTED_PERMISSION_CODE -> {
                val msg = if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    "Permission is not granted!"
                } else {
                    "User already granted our request!"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }
    }

    private fun startHarmonyOSAbility() {
        try {
            val intent = Intent()
            val component = ComponentName(BUNDLE_NAME, ABILITY_NAME)
            intent.component = component
            AbilityUtils.startAbility(this, intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestDistributedPermission() {
        val message: String
        val requestedPermission = pickDistributedPermission()
        message = when {
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

    private fun pickDistributedPermission(): String? {
        val privilegedPermission = "com.huawei.hwddmp.servicebus.BIND_SERVICE"
        val dangerousPermission = "com.huawei.permission.DISTRIBUTED_DATASYNC"
        val isPrivileged = isPrivilegedApp(this, Process.myUid())
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermissionRequest(permission: String) {
        if (shouldShowRequestPermissionRationale(permission)) {
            /* 此处展示给用户为什么需要申请权限, 可按需修改. */
            AlertDialog.Builder(this)
                .setMessage("We need the permission to exchange data across device")
                .setCancelable(true)
                .setPositiveButton("OK") { _, _ ->
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

    private fun isPrivilegedApp(context: Context, uid: Int): Boolean {
        if (uid == Process.SYSTEM_UID) {
            return true
        }
        val pm: PackageManager = context.packageManager ?: return false
        return pm.checkSignatures(uid, Process.SYSTEM_UID) == PackageManager.SIGNATURE_MATCH
    }
}