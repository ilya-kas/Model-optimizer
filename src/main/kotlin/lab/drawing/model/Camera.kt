package lab.drawing.model

import lab.drawing.aspect
import lab.drawing.fov
import lab.drawing.zFar
import lab.drawing.zNear
import lab.logic.entity.math.Matrix
import lab.logic.entity.math.Vector

var currentCamera = Vector(-10.0, 10.0, -10.0)
var currentTarget = Vector(0.0, 0.0, 0.0)

object Camera {
    fun getCameraMatrix(): Matrix {
        val camMatrix = Matrix.getCamMatrix(currentCamera, currentTarget)
        val orthoMatrix = Matrix.getFOVCamMatrix(fov, aspect, zNear, zFar)
        return orthoMatrix * camMatrix
    }
}