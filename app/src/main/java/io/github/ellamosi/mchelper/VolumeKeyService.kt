package io.github.ellamosi.mchelper

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Service d'accessibilité : intercepte les touches volume de la télécommande Shield
 * et les envoie à l'ampli. Le volume de la Shield ne bouge donc pas (le son n'est jamais atténué).
 * À activer une fois par ADB (voir LISEZMOI.md).
 */
class VolumeKeyService : AccessibilityService() {

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val direction = when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> 1
            KeyEvent.KEYCODE_VOLUME_DOWN -> -1
            else -> return false
        }
        if (!Config.volumeRelay || Config.ip.isEmpty()) return false
        // Appui et répétitions (touche maintenue) : un cran à chaque fois. Le relâchement est juste absorbé.
        if (event.action == KeyEvent.ACTION_DOWN) VolumeRelay.press(direction)
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}
