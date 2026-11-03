package io.github.ellamosi.mchelper

import android.util.Log

/**
 * Plafonne le volume au démarrage. Ne monte jamais le volume :
 * il ne fait que le baisser s'il dépasse le seuil.
 */
class VolumeController(private val state: MCDeviceState) {
    companion object {
        private const val TAG = "VolumeController"
    }

    fun capStartVolume() {
        val pct = Config.maxStartVolumePct
        if (pct <= 0) return
        val status = state.status()
        val current = status.optInt("volume", -1)
        val max = MCApi.getMaxVolume(status)
        if (current < 0 || max == null) {
            Log.w(TAG, "Volume ou volume max illisible : plafond non appliqué")
            return
        }
        val cap = max * pct / 100
        if (current > cap) {
            Log.i(TAG, "Volume $current > plafond $cap ($pct % de $max) : abaissé")
            MCApi.setVolume(cap)
        }
    }
}
