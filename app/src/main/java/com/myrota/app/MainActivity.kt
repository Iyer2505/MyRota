package com.myrota.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.myrota.app.navigation.MyRotaApp
import com.myrota.app.ui.theme.MyRotaTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // No action required here yet.
            //
            // If permission is granted,
            // MyRota can show shift reminders.
            //
            // If denied, the app continues normally.
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContent {

            MyRotaTheme {

                MyRotaApp()
            }
        }

        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.TIRAMISU
        ) {

            return
        }

        val permissionStatus =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

        if (
            permissionStatus !=
            PackageManager.PERMISSION_GRANTED
        ) {

            notificationPermissionLauncher
                .launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
        }
    }
}