package jp.huawei.a2hdemo.remote

import android.os.IBinder
import android.os.Parcel


internal class ResultServiceProxy(private val remote: IBinder?) {

    companion object {
        private const val DESCRIPTOR = "com.huawei.gamepaddemo.controller.IResultInterface"
        private const val LOCATION_COMMAND = IBinder.FIRST_CALL_TRANSACTION
        private const val DISCONNECT_COMMAND = IBinder.FIRST_CALL_TRANSACTION + 1
    }

    fun sendLocation(deviceId: String, x: Float, y: Float) {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        data.writeInterfaceToken(DESCRIPTOR)
        data.writeString(deviceId)
        data.writeFloat(x)
        data.writeFloat(y)
        try {
            remote?.transact(LOCATION_COMMAND, data, reply, 0)
            reply.writeNoException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    fun disconnect(deviceId: String) {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        data.writeInterfaceToken(DESCRIPTOR)
        data.writeString(deviceId)
        try {
            remote?.transact(DISCONNECT_COMMAND, data, reply, 0)
            reply.writeNoException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

}