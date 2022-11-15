package drawing

import drawing.model.*
import movementStep
import rotationStep
import java.awt.RenderingHints.Key
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class KeyboardListener(private val frame: AppFrame) : KeyListener {
    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode){
            //camera
            KeyEvent.VK_Q ->{
                currentCamera.z += movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_W ->{
                currentCamera.y += movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_E ->{
                currentCamera.z -= movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_A ->{
                currentCamera.x -= movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_S ->{
                currentCamera.y -= movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_D ->{
                currentCamera.x += movementStep
                frame.onModelChanged()
            }

            //object movement
            KeyEvent.VK_R ->{
                currentPosition.z += movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_T ->{
                currentPosition.y += movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_Y ->{
                currentPosition.z -= movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_F ->{
                currentPosition.x -= movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_G ->{
                currentPosition.y -= movementStep
                frame.onModelChanged()
            }
            KeyEvent.VK_H ->{
                currentPosition.x += movementStep
                frame.onModelChanged()
            }

            //object rotation
            KeyEvent.VK_U ->{
                currentAngle.z -= rotationStep
                frame.onModelChanged()
            }
            KeyEvent.VK_I ->{
                currentAngle.x += rotationStep
                frame.onModelChanged()
            }
            KeyEvent.VK_O ->{
                currentAngle.z += rotationStep
                frame.onModelChanged()
            }
            KeyEvent.VK_J ->{
                currentAngle.y -= rotationStep
                frame.onModelChanged()
            }
            KeyEvent.VK_K ->{
                currentAngle.x -= rotationStep
                frame.onModelChanged()
            }
            KeyEvent.VK_L ->{
                currentAngle.y += rotationStep
                frame.onModelChanged()
            }

            // edges visualization
            KeyEvent.VK_Z ->{
                showGrid = false
                frame.onModelChanged()
            }
            KeyEvent.VK_X ->{
                showGrid = true
                frame.onModelChanged()
            }

            // normals visualization
            KeyEvent.VK_C ->{
                showNormals = false
                frame.onModelChanged()
            }
            KeyEvent.VK_V ->{
                showNormals = true
                frame.onModelChanged()
            }
        }
    }

    override fun keyReleased(e: KeyEvent) {
    }
}