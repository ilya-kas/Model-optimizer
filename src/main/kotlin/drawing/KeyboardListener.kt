package drawing

import logic.currentAngle
import logic.currentCamera
import logic.currentPosition
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class KeyboardListener(private val frame: AppFrame) : KeyListener {
    override fun keyTyped(e: KeyEvent) {
        when (e.keyChar){
            //camera
            'q' ->{
                currentCamera.x += movementStep
                frame.onModelChanged()
            }
            'w' ->{
                currentCamera.y += movementStep
                frame.onModelChanged()
            }
            'e' ->{
                currentCamera.x -= movementStep
                frame.onModelChanged()
            }
            'a' ->{
                currentCamera.z -= movementStep
                frame.onModelChanged()
            }
            's' ->{
                currentCamera.y -= movementStep
                frame.onModelChanged()
            }
            'd' ->{
                currentCamera.z += movementStep
                frame.onModelChanged()
            }

            //object movement
            'r' ->{
                currentPosition.z -= movementStep
                frame.onModelChanged()
            }
            't' ->{
                currentPosition.y += movementStep
                frame.onModelChanged()
            }
            'y' ->{
                currentPosition.z += movementStep
                frame.onModelChanged()
            }
            'f' ->{
                currentPosition.x -= movementStep
                frame.onModelChanged()
            }
            'g' ->{
                currentPosition.y -= movementStep
                frame.onModelChanged()
            }
            'h' ->{
                currentPosition.x += movementStep
                frame.onModelChanged()
            }

            //object rotation
            'u' ->{
                currentAngle.z -= rotationStep
                frame.onModelChanged()
            }
            'i' ->{
                currentAngle.x += rotationStep
                frame.onModelChanged()
            }
            'o' ->{
                currentAngle.z += rotationStep
                frame.onModelChanged()
            }
            'j' ->{
                currentAngle.y -= rotationStep
                frame.onModelChanged()
            }
            'k' ->{
                currentAngle.x -= rotationStep
                frame.onModelChanged()
            }
            'l' ->{
                currentAngle.y += rotationStep
                frame.onModelChanged()
            }
        }
    }

    override fun keyPressed(e: KeyEvent) {
    }

    override fun keyReleased(e: KeyEvent) {
    }
}