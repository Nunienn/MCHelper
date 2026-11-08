package io.github.ellamosi.mchelper

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Réglages stockés sur la Shield (pas besoin de recompiler pour les changer).
 * Ils se règlent par ADB, au premier lancement du service :
 *   adb shell am start-foreground-service -n io.github.ellamosi.mchelper/.MCHService \
 *     --es ip 192.168.1.50 --es input optical1 --ei maxStartVolumePct 30 --ez autoOff true
 */
object Config {
    private const val TAG = "Config"
    private const val PREFS = "mchelper"

    private const val KEY_IP = "ip"
    private const val KEY_INPUT = "input"
    private const val KEY_MAX_START_VOLUME_PCT = "maxStartVolumePct"
    private const val KEY_AUTO_OFF = "autoOff"
    private const val KEY_VOLUME_RELAY = "volumeRelay"
    private const val KEY_VOLUME_STEP = "volumeStep"
    private const val KEY_MAX_VOLUME_PCT = "maxVolumePct"

    // Valeurs par défaut
    private const val DEFAULT_IP = "10.0.0.7"
    private const val DEFAULT_INPUT = "optical1"
    private const val DEFAULT_MAX_START_VOLUME_PCT = 30   // % du volume max de l'ampli
    private const val DEFAULT_AUTO_OFF = true
    private const val DEFAULT_VOLUME_RELAY = true
    private const val DEFAULT_VOLUME_STEP = 3          // crans d'ampli par appui (1 cran = 0,5 dB)
    private const val DEFAULT_MAX_VOLUME_PCT = 60      // plafond absolu via la télécommande

    private fun prefs() =
        MCHelper.instance.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Adresse IP locale de l'ampli. */
    val ip: String get() = prefs().getString(KEY_IP, DEFAULT_IP) ?: DEFAULT_IP

    /** Entrée à sélectionner sur l'ampli (ex. optical1, optical2, coaxial1, bluetooth). */
    val input: String get() = prefs().getString(KEY_INPUT, DEFAULT_INPUT) ?: DEFAULT_INPUT

    /** Plafond du volume au démarrage, en % du volume max de l'ampli (0 = désactivé). */
    val maxStartVolumePct: Int
        get() = prefs().getInt(KEY_MAX_START_VOLUME_PCT, DEFAULT_MAX_START_VOLUME_PCT).coerceIn(0, 100)

    /** Mettre l'ampli en veille quand la Shield se met en veille. */
    val autoOff: Boolean get() = prefs().getBoolean(KEY_AUTO_OFF, DEFAULT_AUTO_OFF)

    /** Les touches volume de la Shield pilotent le volume de l'ampli. */
    val volumeRelay: Boolean get() = prefs().getBoolean(KEY_VOLUME_RELAY, DEFAULT_VOLUME_RELAY)

    /** Nombre de crans d'ampli par appui (1 à 20). */
    val volumeStep: Int get() = prefs().getInt(KEY_VOLUME_STEP, DEFAULT_VOLUME_STEP).coerceIn(1, 20)

    /** Volume jamais dépassé via la télécommande, en % du volume max de l'ampli (10 à 100). */
    val maxVolumePct: Int get() = prefs().getInt(KEY_MAX_VOLUME_PCT, DEFAULT_MAX_VOLUME_PCT).coerceIn(10, 100)

    /** Lit les réglages éventuellement passés en extras de l'intent ADB et les enregistre. */
    fun updateFromIntent(intent: Intent?) {
        if (intent == null) return
        val editor = prefs().edit()
        var changed = false
        intent.getStringExtra(KEY_IP)?.trim()?.let {
            if (isValidLocalIp(it)) {
                editor.putString(KEY_IP, it); changed = true
            } else {
                Log.e(TAG, "IP refusée (doit être une IP locale valide, ex. 192.168.1.50) : $it")
            }
        }
        intent.getStringExtra(KEY_INPUT)?.trim()?.let {
            if (it.isNotEmpty()) { editor.putString(KEY_INPUT, it); changed = true }
        }
        if (intent.hasExtra(KEY_MAX_START_VOLUME_PCT)) {
            editor.putInt(KEY_MAX_START_VOLUME_PCT,
                intent.getIntExtra(KEY_MAX_START_VOLUME_PCT, DEFAULT_MAX_START_VOLUME_PCT).coerceIn(0, 100))
            changed = true
        }
        if (intent.hasExtra(KEY_AUTO_OFF)) {
            editor.putBoolean(KEY_AUTO_OFF, intent.getBooleanExtra(KEY_AUTO_OFF, DEFAULT_AUTO_OFF))
            changed = true
        }
        if (intent.hasExtra(KEY_VOLUME_RELAY)) {
            editor.putBoolean(KEY_VOLUME_RELAY, intent.getBooleanExtra(KEY_VOLUME_RELAY, DEFAULT_VOLUME_RELAY))
            changed = true
        }
        if (intent.hasExtra(KEY_VOLUME_STEP)) {
            editor.putInt(KEY_VOLUME_STEP, intent.getIntExtra(KEY_VOLUME_STEP, DEFAULT_VOLUME_STEP).coerceIn(1, 20))
            changed = true
        }
        if (intent.hasExtra(KEY_MAX_VOLUME_PCT)) {
            editor.putInt(KEY_MAX_VOLUME_PCT, intent.getIntExtra(KEY_MAX_VOLUME_PCT, DEFAULT_MAX_VOLUME_PCT).coerceIn(10, 100))
            changed = true
        }
        if (changed) {
            editor.apply()
            Log.i(TAG, "Réglages : ip=$ip input=$input maxStartVolumePct=$maxStartVolumePct autoOff=$autoOff " +
                "volumeRelay=$volumeRelay volumeStep=$volumeStep maxVolumePct=$maxVolumePct")
        }
    }

    /** Accepte uniquement les plages privées (192.168.x.x, 10.x.x.x, 172.16-31.x.x). */
    fun isValidLocalIp(s: String): Boolean {
        val parts = s.split(".")
        if (parts.size != 4) return false
        val n = parts.map { it.toIntOrNull() ?: return false }
        if (n.any { it !in 0..255 }) return false
        return n[0] == 10 ||
            (n[0] == 192 && n[1] == 168) ||
            (n[0] == 172 && n[1] in 16..31)
    }
}
