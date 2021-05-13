package jp.huawei.a2hdemo.local

import android.content.SharedPreferences
import jp.huawei.a2hdemo.extensions.boolean

class Config(sharedPreferences: SharedPreferences) {
    var isGameRunning: Boolean by sharedPreferences.boolean()
}