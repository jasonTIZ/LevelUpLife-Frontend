package com.example.leveluplife.data.rewards

import android.content.Context

class PurchasedItemStorage(context: Context) {

    private val prefs = context.getSharedPreferences("purchased_items", Context.MODE_PRIVATE)
    private val key = "owned_ids"

    fun getPurchasedIds(): Set<Int> =
        prefs.getStringSet(key, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()

    fun addPurchasedId(id: Int) {
        val current = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(id.toString())
        prefs.edit().putStringSet(key, current).apply()
    }

    fun isPurchased(id: Int): Boolean = id.toString() in (prefs.getStringSet(key, emptySet()) ?: emptySet())
}
