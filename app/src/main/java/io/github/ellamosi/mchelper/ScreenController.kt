package io.github.ellamosi.mchelper

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/** Pilote l'écran de projection via un Shelly 2PM en mode volet (API locale Gen2/Gen3). */
object ScreenController {
    private const val TAG = "ScreenController"
    private const val TIMEOUT_MS = 3000
    private const val ATTEMPTS = 3

    fun lower() = goTo(Config.screenDownPos, "descente")
    fun raise() = goTo(Config.screenUpPos, "montée")

    private fun goTo(pos: Int, label: String) {
        val ip = Config.screenIp
        if (ip.isEmpty()) return
        // 0 et 100 : course complète jusqu'aux fins de course du moteur ; sinon arrêt à la position demandée.
        val path = when (pos) {
            0 -> "Cover.Close?id=0"
            100 -> "Cover.Open?id=0"
            else -> "Cover.GoToPosition?id=0&pos=$pos"
        }
        for (attempt in 1..ATTEMPTS) {
            try {
                val conn = URL("http://$ip/rpc/$path").openConnection() as HttpURLConnection
                conn.connectTimeout = TIMEOUT_MS
                conn.readTimeout = TIMEOUT_MS
                try {
                    val code = conn.responseCode
                    if (code == 200) { Log.i(TAG, "Écran : $label ($path)"); return }
                    Log.w(TAG, "Écran : $label → HTTP $code")
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Écran : $label, tentative $attempt/$ATTEMPTS échouée (${e.message})")
            }
            if (attempt < ATTEMPTS) Thread.sleep(1500)
        }
    }
}
