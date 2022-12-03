package logic.entity

import SCALE
import logic.entity.math.Matrix
import logic.entity.math.Vector

var currentPosition = Vector(0.0, 0.0, 0.0)
var currentScale = Vector(SCALE, SCALE, SCALE)
var currentAngle = Vector(0.0, 0.0, 0.0)
var currentLight = Vector(-10.0, 10.0, -10.0)

object WorldManager {
    fun getWorldMatrix(): Matrix{
        return Matrix.getTranslationMatrix(currentPosition) *
               Matrix.getRotateXMatrix(currentAngle.x) *
               Matrix.getRotateYMatrix(currentAngle.y) *
               Matrix.getRotateZMatrix(currentAngle.z) *
               Matrix.getScaleMatrix(currentScale)
    }
}