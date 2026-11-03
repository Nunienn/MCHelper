package io.github.ellamosi.mchelper.actions

import io.github.ellamosi.mchelper.*

class TurnOn : MCHAction() {
    override fun execute(power: PowerController, input: InputController, volume: VolumeController) {
        if (!power.turnOn()) return
        input.setInput()
        volume.capStartVolume()
    }
}
