package io.github.ellamosi.mchelper.actions

import android.util.Log
import io.github.ellamosi.mchelper.*

class TurnOff : MCHAction() {
    override fun execute(power: PowerController, input: InputController, volume: VolumeController) {
        try { ScreenController.raise() } catch (e: Exception) { Log.w("TurnOff", "Écran", e) }
        // N'éteint que si l'ampli est sur l'entrée de la Shield (ne coupe pas une écoute de musique).
        if (Config.autoOff && input.isOnTvInput()) power.turnOff()
    }
}
