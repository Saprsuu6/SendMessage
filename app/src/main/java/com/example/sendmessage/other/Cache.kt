package com.example.sendmessage.other

import android.content.Context
import com.example.sendmessage.R

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

        return sharedPreferences.getString(key, null)
    }

    fun saveInt(context: Context, key: String, value: Int) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun loadInt(context: Context, key: String): Int {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        return sharedPreferences.getInt(key, 0)
    }

    fun saveLong(context: Context, key: String, value: Long) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun loadLong(context: Context, key: String): Long {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        return sharedPreferences.getLong(key, 0)
    }

    fun remove(context: Context, key: String) {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.mainScope), Context.MODE_PRIVATE
        )

        sharedPreferences.edit().remove(key).apply()
    }
}