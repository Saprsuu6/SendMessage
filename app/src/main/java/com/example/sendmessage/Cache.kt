package com.example.sendmessage

import android.content.Context

class Cache {
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun loadBoolean(context: Context, key: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        return sharedPreferences.getBoolean(key, false)
    }

    fun saveString(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        sharedPreferences.edit().putString(key, value).apply()
    }

    fun loadString(context: Context, key: String): String? {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(key, "")
    }
}