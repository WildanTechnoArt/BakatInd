package com.bakatind.indonesia.skillacademy.fcm

import android.content.Context
import android.content.Intent
import com.bakatind.indonesia.skillacademy.activity.SplashActivity
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal

class NotificationOpenedHandler(private var ctx: Context) :
    OneSignal.OSNotificationOpenedHandler {

    override fun notificationOpened(result: OSNotificationOpenedResult?) {
        val intent = Intent(ctx, SplashActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
        ctx.startActivity(intent)
    }
}