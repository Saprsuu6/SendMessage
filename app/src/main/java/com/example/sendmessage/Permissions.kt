package com.example.sendmessage

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Permissions {
    companion object {
        private const val contactsPermission: String = Manifest.permission.READ_CONTACTS
        private const val sendSmsPermission: String = Manifest.permission.SEND_SMS
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

        fun setPermissions(activity: MainActivity) {
            val permissions = getPermissions()
            if (!checkPermission(activity)) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode)
            }
        }

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

        private fun getPermissions(): Array<String> {
            return arrayOf(contactsPermission, sendSmsPermission)
        }
    }
}