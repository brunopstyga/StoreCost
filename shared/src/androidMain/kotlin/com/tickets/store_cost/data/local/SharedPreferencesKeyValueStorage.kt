package com.tickets.store_cost.data.local

import android.content.Context

/**
 * Implementación Android de [KeyValueStorage] basada en SharedPreferences.
 */
class SharedPreferencesKeyValueStorage(context: Context) : KeyValueStorage {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "store_cost_auth"
    }
}
