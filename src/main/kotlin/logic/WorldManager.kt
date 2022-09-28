package logic

import drawing.*
import logic.entity.Matrix
import logic.entity.Model
import logic.entity.Vector

var currentPosition = Vector(10.0, 0.0, 10.0)
var currentScale = Vector(5.0, 5.0, 5.0)
var currentAngle = Vector(0.0, 0.0, 0.0)
var currentCamera = Vector(0.0, 10.0, 0.0)
var currentTarget = Vector(10.0, 0.0, 10.0)

object WorldManager {
    fun projectModel(model: Model): ArrayList<Vector>{
        val vertices = ArrayList<Vector>()

        val worldMatrix = Matrix.getTranslationMatrix(currentPosition
        ) * Matrix.getRotateXMatrix(currentAngle.x) * Matrix.getRotateYMatrix(currentAngle.y) * Matrix.getRotateZMatrix(currentAngle.z
        ) * Matrix.getScaleMatrix(currentScale)
        val camMatrix = Matrix.getCamMatrix(currentCamera, currentTarget)
        val orthoMatrix = Matrix.getFOVCamMatrix(fov, aspect, zNear, zFar)
        val viewportMatrix = Matrix.getViewportMatrix(frameWidth.toDouble(), frameHeight.toDouble())
        val fullMatrix = orthoMatrix * camMatrix * worldMatrix
        for (v in model.v) {
            var newbie = v * fullMatrix
            newbie /= newbie.w
            newbie *= viewportMatrix
            vertices += newbie
            //println("${newbie.x} ${newbie.y} ${newbie.z} ${newbie.w}")
        }

        return vertices
    }
}