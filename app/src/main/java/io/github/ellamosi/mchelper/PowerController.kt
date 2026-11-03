package io.github.ellamosi.mchelper

import android.util.Log

class PowerController(private val state: MCDeviceState) {
    companion object {
        private const val TAG = "PowerController"
        private const val MIN_POWER_CYCLE_DELAY = 10 * 1000L
        private const val WAKE_TIMEOUT_MS = 15 * 1000L
        private const val POLL_MS = 1000L
    }

    private var lastShutdown: Long? = null

    /** Allume l'ampli et attend qu'il soit réellement allumé. Renvoie true s'il est allumé. */
    fun turnOn(): Boolean {
        if (hasRecentlyTurnedOff()) return false
        if (state.power() == "on") return true
        MCApi.turnOn()
        val deadline = System.currentTimeMillis() + WAKE_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(POLL_MS)
            if (state.power() == "on") return true
        }
        Log.w(TAG, "L'ampli ne confirme pas l'allumage")
        return false
    }

    fun turnOff() {
        MCApi.turnOff()
        lastShutdown = System.currentTimeMillis()
    }

    private fun hasRecentlyTurnedOff(): Boolean =
        lastShutdown?.let { System.currentTimeMillis() - it < MIN_POWER_CYCLE_DELAY } ?: false
}
