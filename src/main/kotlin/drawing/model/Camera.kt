package drawing.model

import drawing.aspect
import drawing.fov
import drawing.zFar
import drawing.zNear
import logic.entity.math.Matrix
import logic.entity.math.Vector

var currentCamera = Vector(-10.0, 10.0, -10.0)
var currentTarget = Vector(0.0, 0.0, 0.0)

object Camera {
    fun getCameraMatrix(): Matrix{
        val camMatrix = Matrix.getCamMatrix(currentCamera, currentTarget)
        val orthoMatrix = Matrix.getFOVCamMatrix(fov, aspect, zNear, zFar)
        return orthoMatrix * camMatrix
    }
}