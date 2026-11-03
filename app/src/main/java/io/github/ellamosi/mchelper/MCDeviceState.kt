package io.github.ellamosi.mchelper

import org.json.JSONObject

/** État de l'ampli, relu à chaque demande (pas de cache : évite les décisions sur un état périmé). */
class MCDeviceState {
    fun status(): JSONObject = MCApi.getStatus()
    fun power(): String = status().optString("power")
    fun input(): String = status().optString("input")
}
