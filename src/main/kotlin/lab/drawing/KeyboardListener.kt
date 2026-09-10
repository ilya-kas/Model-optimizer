package lab.drawing

import lab.util.MOVEMENT_STEP
import lab.util.ROTATION_STEP
import lab.drawing.model.currentCamera
import lab.drawing.model.showBarCoordsMap
import lab.drawing.model.showCornersNormals
import lab.drawing.model.showGrid
import lab.drawing.model.showMidNormal
import lab.drawing.model.showNormalMap
import lab.drawing.model.showPlaneAreaMap
import lab.drawing.model.showVertexAngleMap
import lab.logic.currentAngle
import lab.logic.currentPosition
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class KeyboardListener(private val frame: AppFrame) : KeyListener {
    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode){
            //camera
            KeyEvent.VK_Q,
            KeyEvent.VK_W,
            KeyEvent.VK_E,
            KeyEvent.VK_A,
            KeyEvent.VK_S,
            KeyEvent.VK_D -> handleCamera(e.keyCode)


            // edges visualization
            KeyEvent.VK_Z ->{
                showGrid = !showGrid
                frame.onModelChanged()
            }

            // normals visualization
            KeyEvent.VK_R,
            KeyEvent.VK_F -> handleNormals(e.keyCode)

            // normal map visualization
            KeyEvent.VK_T,
            KeyEvent.VK_G -> handleTexture(e.keyCode)

            KeyEvent.VK_Y,
            KeyEvent.VK_H -> handleOptimizerMaps(e.keyCode)
        }
    }

    override fun keyReleased(e: KeyEvent) {
    }

    private fun handleCamera(keycode: Int){
        when (keycode){
            KeyEvent.VK_Q ->{
                currentCamera.z += MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_W ->{
                currentCamera.y += MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_E ->{
                currentCamera.z -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_A ->{
                currentCamera.x -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_S ->{
                currentCamera.y -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_D ->{
                currentCamera.x += MOVEMENT_STEP
                frame.onModelChanged()
            }
        }
    }

    private fun handleNormals(keycode: Int){
        when (keycode){
            KeyEvent.VK_R ->{
                showCornersNormals = !showCornersNormals
                frame.onModelChanged()
            }
            KeyEvent.VK_F ->{
                showMidNormal = !showMidNormal
                frame.onModelChanged()
            }
        }
    }

    private fun handleTexture(keycode: Int){
        when (keycode){
            KeyEvent.VK_T ->{
                showNormalMap = !showNormalMap
                if (showNormalMap) {
                    showBarCoordsMap = false
                    showVertexAngleMap = false
                    showPlaneAreaMap = false
                }
                frame.onModelChanged()
            }
            KeyEvent.VK_G ->{
                showBarCoordsMap = !showBarCoordsMap
                if (showBarCoordsMap) {
                    showNormalMap = false
                    showVertexAngleMap = false
                    showPlaneAreaMap = false
                }
                frame.onModelChanged()
            }
        }
    }

    private fun handleOptimizerMaps(keycode: Int){
        when (keycode){
            KeyEvent.VK_Y ->{
                showVertexAngleMap = !showVertexAngleMap
                if (showVertexAngleMap) {
                    showPlaneAreaMap = false
                    showNormalMap = false
                    showBarCoordsMap = false
                }
                frame.onModelChanged()
            }
            KeyEvent.VK_H ->{
                showPlaneAreaMap = !showPlaneAreaMap
                if (showPlaneAreaMap) {
                    showVertexAngleMap = false
                    showNormalMap = false
                    showBarCoordsMap = false
                }
                frame.onModelChanged()
            }
        }
    }
}

class OldKeyboardListener(private val frame: AppFrame) : KeyListener {
    override fun keyTyped(e: KeyEvent) {
    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode){
            //camera
            KeyEvent.VK_Q ->{
                currentCamera.z += MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_W ->{
                currentCamera.y += MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_E ->{
                currentCamera.z -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_A ->{
                currentCamera.x -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_S ->{
                currentCamera.y -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_D ->{
                currentCamera.x += MOVEMENT_STEP
                frame.onModelChanged()
            }

            //object movement
            KeyEvent.VK_R ->{
                currentPosition.z += MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_T ->{
                currentPosition.y += MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_Y ->{
                currentPosition.z -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_F ->{
                currentPosition.x -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_G ->{
                currentPosition.y -= MOVEMENT_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_H ->{
                currentPosition.x += MOVEMENT_STEP
                frame.onModelChanged()
            }

            //object rotation
            KeyEvent.VK_U ->{
                currentAngle.z -= ROTATION_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_I ->{
                currentAngle.x += ROTATION_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_O ->{
                currentAngle.z += ROTATION_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_J ->{
                currentAngle.y -= ROTATION_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_K ->{
                currentAngle.x -= ROTATION_STEP
                frame.onModelChanged()
            }
            KeyEvent.VK_L ->{
                currentAngle.y += ROTATION_STEP
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
                showCornersNormals = false
                frame.onModelChanged()
            }
            KeyEvent.VK_V ->{
                showCornersNormals = true
                frame.onModelChanged()
            }

            // normal map visualization
            KeyEvent.VK_B ->{
                showNormalMap = false
                frame.onModelChanged()
            }
            KeyEvent.VK_N ->{
                showNormalMap = true
                frame.onModelChanged()
            }
        }
    }

    override fun keyReleased(e: KeyEvent) {
    }
}