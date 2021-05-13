package jp.huawei.a2hdemo.extensions

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Boolean Read Write Delegate
 */
fun SharedPreferences.boolean(
    defaultValue: Boolean = false,
    key: String? = null
): ReadWriteProperty<Any, Boolean> =
    delegate(defaultValue, key, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

/**
 * Nullable Boolean Read Write Delegate
 */
fun SharedPreferences.nullableBoolean(key: String? = null): ReadWriteProperty<Any, Boolean?> =
    nullableDelegate(
        false,
        key,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

/**
 * Float Read Write Delegate
 */
fun SharedPreferences.float(
    defaultValue: Float = 0f,
    key: String? = null
): ReadWriteProperty<Any, Float> =
    delegate(defaultValue, key, SharedPreferences::getFloat, SharedPreferences.Editor::putFloat)

/**
 * Nullable Float Read Write Delegate
 */
fun SharedPreferences.nullableFloat(key: String? = null): ReadWriteProperty<Any, Float?> =
    nullableDelegate(0f, key, SharedPreferences::getFloat, SharedPreferences.Editor::putFloat)

/**
 * Int Read Write Delegate
 */
fun SharedPreferences.int(defaultValue: Int = 0, key: String? = null): ReadWriteProperty<Any, Int> =
    delegate(defaultValue, key, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

/**
 * Nullable Int Read Write Delegate
 */
fun SharedPreferences.nullableInt(key: String? = null): ReadWriteProperty<Any, Int?> =
    nullableDelegate(0, key, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

/**
 * Long Read Write Delegate
 */
fun SharedPreferences.long(
    defaultValue: Long = 0,
    key: String? = null
): ReadWriteProperty<Any, Long> =
    delegate(defaultValue, key, SharedPreferences::getLong, SharedPreferences.Editor::putLong)

/**
 * Nullable Long Read Write Delegate
 */
fun SharedPreferences.nullableLong(key: String? = null): ReadWriteProperty<Any, Long?> =
    nullableDelegate(0, key, SharedPreferences::getLong, SharedPreferences.Editor::putLong)

/**
 * String Read Write Delegate
 */
fun SharedPreferences.string(
    defaultValue: String = "",
    key: String? = null
): ReadWriteProperty<Any, String> =
    object : ReadWriteProperty<Any, String> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String {
            return getString(key ?: property.name, defaultValue) ?: defaultValue
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            edit().putString(key ?: property.name, value).apply()
        }
    }

/**
 * Nullable String Read Write Delegate
 */
fun SharedPreferences.nullableString(key: String? = null): ReadWriteProperty<Any, String?> =
    object : ReadWriteProperty<Any, String?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String? {
            return getString(key ?: property.name, null)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
            edit().putString(key ?: property.name, value).apply()
        }
    }

private inline fun <T : Any> SharedPreferences.delegate(
    defaultValue: T, key: String?,
    crossinline getter: SharedPreferences.(key: String, defaultValue: T) -> T,
    crossinline setter: SharedPreferences.Editor.(key: String, value: T) -> SharedPreferences.Editor
) = object : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>) =
        getter(key ?: property.name, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        edit().setter(key ?: property.name, value).apply()
}

private inline fun <T : Any> SharedPreferences.nullableDelegate(
    dummy: T, key: String?,
    crossinline getter: SharedPreferences.(key: String, defaultValue: T) -> T,
    crossinline setter: SharedPreferences.Editor.(key: String, value: T) -> SharedPreferences.Editor
) = object : ReadWriteProperty<Any, T?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        val target = key ?: property.name
        return if (contains(target)) getter(target, dummy) else null
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        val target = key ?: property.name
        if (value == null) {
            edit().remove(target).apply()
        } else {
            edit().setter(target, value).apply()
        }
    }
}