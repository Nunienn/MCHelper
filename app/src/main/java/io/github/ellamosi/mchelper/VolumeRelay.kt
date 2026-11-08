package io.github.ellamosi.mchelper

import android.util.Log
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Transmet les appuis volume +/- de la télécommande Shield à l'ampli, par le réseau.
 * Les appuis rapides sont regroupés en une seule commande. Le volume ne dépasse jamais
 * Config.maxVolumePct % du volume max de l'ampli.
 */
object VolumeRelay {
    private const val TAG = "VolumeRelay"

    private val pending = AtomicInteger(0)   // appuis en attente (+ = monter, - = baisser)
    private val wakeUp = LinkedBlockingQueue<Unit>()
    private var maxVolume: Int? = null       // volume max de l'ampli, lu une seule fois

    private val thread = Thread({ loop() }, "VolumeRelay").apply {
        isDaemon = true
        start()
    }

    /** Appelé à chaque appui (direction = +1 ou -1). Ne bloque jamais. */
    fun press(direction: Int) {
        pending.addAndGet(direction)
        wakeUp.offer(Unit)
    }

    private fun loop() {
        while (true) {
            wakeUp.take()
            wakeUp.clear()
            val presses = pending.getAndSet(0)
            if (presses == 0) continue
            try {
                apply(presses)
            } catch (e: Exception) {
                Log.w(TAG, "Réglage du volume impossible (${e.message})")
            }
        }
    }

    private fun apply(presses: Int) {
        val status = MCApi.getStatus()
        if (status.optString("power") != "on") return   // ampli en veille : on ne fait rien
        val current = status.optInt("volume", -1)
        if (current < 0) return
        val max = maxVolume ?: MCApi.getMaxVolume(status)?.also { maxVolume = it } ?: return
        val cap = max * Config.maxVolumePct / 100
        val target = (current + presses * Config.volumeStep).coerceIn(0, cap)
        if (target != current) {
            MCApi.setVolume(target)
            Log.i(TAG, "Volume $current → $target (plafond $cap)")
        }
    }
}
