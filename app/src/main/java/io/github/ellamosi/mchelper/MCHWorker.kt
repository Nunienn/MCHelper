package io.github.ellamosi.mchelper

import android.util.Log
import io.github.ellamosi.mchelper.actions.MCHAction
import java.util.concurrent.LinkedBlockingQueue

class MCHWorker : Runnable {
    companion object {
        private const val TAG = "MCHWorker"
    }

    private val state = MCDeviceState()
    private val powerController = PowerController(state)
    private val inputController = InputController(state)
    private val volumeController = VolumeController(state)

    private val requestQueue = LinkedBlockingQueue<MCHAction>()

    override fun run() {
        try {
            while (true) {
                val action = requestQueue.take()
                try {
                    action.execute(powerController, inputController, volumeController)
                } catch (e: InterruptedException) {
                    throw e
                } catch (e: Exception) {
                    // Une erreur (ampli injoignable…) ne doit plus arrêter le service.
                    Log.e(TAG, "Action ${action.javaClass.simpleName} en échec", e)
                }
            }
        } catch (e: InterruptedException) {}
    }

    fun enqueueAction(action: MCHAction) {
        requestQueue.put(action)
    }
}
