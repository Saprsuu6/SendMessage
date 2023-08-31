package com.example.sendmessage.services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.sendmessage.MainActivity

class Permissions {
    companion object {
        private const val contactsPermission: String = Manifest.permission.READ_CONTACTS
        private const val sendSmsPermission: String = Manifest.permission.SEND_SMS

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val notificationPermissions: String = Manifest.permission.POST_NOTIFICATIONS
        const val requestCode: Int = 1

        fun checkSmsPermission(activity: MainActivity): Boolean {
            return ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun requestSmsPermission(activity: MainActivity) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.SEND_SMS), requestCode
            )
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun checkNotificationsPermission(activity: MainActivity): Boolean {
            return ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun requestNotificationsPermission(activity: MainActivity) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), requestCode
            )
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun setPermissions(activity: MainActivity) {
            val permissions = getPermissions()
            if (!checkPermission(activity)) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode)
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun checkPermission(activity: MainActivity): Boolean {
            val permissions = getPermissions()
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        activity, permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) return false
            }

            return true
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun getPermissions(): Array<String> {
            return arrayOf(
                contactsPermission, sendSmsPermission, notificationPermissions
            )
        }
    }
}