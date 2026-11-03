package io.github.ellamosi.mchelper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Display
import androidx.core.app.NotificationCompat
import io.github.ellamosi.mchelper.actions.TurnOff
import io.github.ellamosi.mchelper.actions.TurnOn

class MCHService : Service() {
    companion object {
        private const val TAG = "MchService"
        private const val FOREGROUND_ID = 1337
        private const val CHANNEL_ID = "service_notifications"
    }

    private val worker = MCHWorker()
    private val workerThread = Thread(worker)
    private val screenReceiver = ScreenReceiver()
    private var displayManager: DisplayManager? = null

    override fun onCreate() {
        super.onCreate()
        displayManager = applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        application.registerReceiver(screenReceiver, filter)
        createNotificationChannel()
        workerThread.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_ID, buildForegroundNotification())

        // Réglages éventuellement passés par ADB (--es ip …, --es input …, --ei maxStartVolumePct …)
        Config.updateFromIntent(intent)

        val extraAction = intent?.getStringExtra("ACTION")
        Log.i(TAG, "Service démarré (action: '$extraAction')")

        when (extraAction) {
            "SCREEN_OFF" -> worker.enqueueAction(TurnOff())
            "SCREEN_ON", "BOOT" -> turnOnIfDisplayOn()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try { application.unregisterReceiver(screenReceiver) } catch (e: IllegalArgumentException) {}
        workerThread.interrupt()
    }

    private fun buildForegroundNotification(): Notification =
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(applicationContext.getText(R.string.notification_title))
            .setContentText(applicationContext.getText(R.string.notification_message))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Service Notifications Channel",
                NotificationManager.IMPORTANCE_MIN)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun turnOnIfDisplayOn() {
        if (isDisplayOn()) worker.enqueueAction(TurnOn())
    }

    private fun isDisplayOn(): Boolean =
        displayManager?.displays?.any { it.state == Display.STATE_ON } ?: false
}
