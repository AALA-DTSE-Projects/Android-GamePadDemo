package jp.huawei.a2hdemo.local

data class HandleEvent(
    val deviceId: String,
    val action: String,
    val force: Float? = null,
    val angle: Int? = null
)