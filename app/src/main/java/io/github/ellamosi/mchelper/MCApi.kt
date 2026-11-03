package io.github.ellamosi.mchelper

import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Client minimal de l'API Yamaha Extended Control (MusicCast), en HTTP local. */
object MCApi {
    private const val TAG = "MCApi"
    private const val TIMEOUT_MS = 3000
    private const val ATTEMPTS = 3
    private const val RETRY_DELAY_MS = 1500L

    private fun baseUrl(): String {
        val ip = Config.ip
        if (ip.isEmpty()) throw IOException("IP de l'ampli non configurée")
        return "http://$ip/YamahaExtendedControl/v1/"
    }

    fun turnOn() { request("main/setPower?power=on") }
    fun turnOff() { request("main/setPower?power=standby") }
    fun setInput(input: String) { request("main/setInput?input=$input") }
    fun setVolume(volume: Int) { request("main/setVolume?volume=$volume") }
    fun getStatus(): JSONObject = request("main/getStatus")
    fun getFeatures(): JSONObject = request("system/getFeatures")

    /** Volume max de la zone principale, lu sur l'ampli (null si introuvable). */
    fun getMaxVolume(status: JSONObject? = null): Int? {
        status?.optInt("max_volume", -1)?.let { if (it > 0) return it }
        return try {
            val zones = getFeatures().optJSONArray("zone") ?: return null
            for (i in 0 until zones.length()) {
                val zone = zones.getJSONObject(i)
                if (zone.optString("id") != "main") continue
                val steps = zone.optJSONArray("range_step") ?: return null
                for (j in 0 until steps.length()) {
                    val step = steps.getJSONObject(j)
                    if (step.optString("id") == "volume") return step.optInt("max", -1).takeIf { it > 0 }
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Volume max introuvable", e); null
        }
    }

    /** Requête GET avec délai d'attente et nouvelles tentatives (l'ampli en Wi-Fi peut mettre du temps à répondre). */
    private fun request(path: String): JSONObject {
        var lastError: Exception? = null
        repeat(ATTEMPTS) { attempt ->
            try {
                val conn = URL(baseUrl() + path).openConnection() as HttpURLConnection
                conn.connectTimeout = TIMEOUT_MS
                conn.readTimeout = TIMEOUT_MS
                try {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val code = json.optInt("response_code", 0)
                    if (code != 0) Log.w(TAG, "$path → response_code=$code")
                    return json
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "$path : échec tentative ${attempt + 1}/$ATTEMPTS (${e.message})")
                if (attempt < ATTEMPTS - 1) Thread.sleep(RETRY_DELAY_MS)
            }
        }
        throw IOException("Ampli injoignable : $path", lastError)
    }
}
