package io.github.ellamosi.mchelper.actions

import android.util.Log
import io.github.ellamosi.mchelper.*

class TurnOn : MCHAction() {
    override fun execute(power: PowerController, input: InputController, volume: VolumeController) {
        // L'écran d'abord : une panne de l'ampli ne doit pas l'empêcher de descendre.
        try { ScreenController.lower() } catch (e: Exception) { Log.w("TurnOn", "Écran", e) }
        if (!power.turnOn()) return
        input.setInput()
        volume.capStartVolume()
    }
}
