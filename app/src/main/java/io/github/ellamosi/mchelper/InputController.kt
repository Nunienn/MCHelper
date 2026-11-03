package io.github.ellamosi.mchelper

class InputController(private val state: MCDeviceState) {
    fun setInput() {
        if (!isOnTvInput()) MCApi.setInput(Config.input)
    }

    fun isOnTvInput(): Boolean = Config.input == state.input()
}
