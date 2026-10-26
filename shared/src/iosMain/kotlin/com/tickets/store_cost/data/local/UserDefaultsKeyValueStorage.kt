package com.tickets.store_cost.data.local

import platform.Foundation.NSUserDefaults

/**
 * Implementación iOS de [KeyValueStorage] basada en NSUserDefaults.
 */
class UserDefaultsKeyValueStorage(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : KeyValueStorage {

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
