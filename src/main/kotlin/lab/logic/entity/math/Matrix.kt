package lab.logic.entity.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Matrix(rows: Int = 0, columns: Int = 0) {
    val values = ArrayList<ArrayList<Double>>()

    init {
        for (row in 0 until rows) {
            values += ArrayList<Double>()
            for (col in 0 until columns)
                values[row] += 0.0
        }
    }

    operator fun times(matrix: Matrix): Matrix {
        val res = Matrix(values.size, if (matrix.values.isNotEmpty()) matrix.values[0].size else 0)
        for (row in res.values.indices) {
            for (col in res.values[row].indices) {
                var cell = 0.0
                for (i in matrix.values.indices)
                    cell += values[row][i] * matrix.values[i][col]

                res.values[row][col] = cell
            }
        }
        return res
    }

    override fun toString(): String {
        val builder = java.lang.StringBuilder()
        for (row in values)
            builder.append(row.toString()).append("\n")
        return builder.toString()
    }

    companion object {
        fun getTranslationMatrix(translation: Vector): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(1.0, 0.0, 0.0, translation.x)
                values[1] = arrayListOf(0.0, 1.0, 0.0, translation.y)
                values[2] = arrayListOf(0.0, 0.0, 1.0, translation.z)
                values[3] = arrayListOf(0.0, 0.0, 0.0, 1.0)
            }
        }

        fun getScaleMatrix(scale: Vector): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(scale.x, 0.0, 0.0, 0.0)
                values[1] = arrayListOf(0.0, scale.y, 0.0, 0.0)
                values[2] = arrayListOf(0.0, 0.0, scale.z, 0.0)
                values[3] = arrayListOf(0.0, 0.0, 0.0, 1.0)
            }
        }

        fun getRotateXMatrix(angle: Double): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(1.0, 0.0, 0.0, 0.0)
                values[1] = arrayListOf(0.0, cos(angle), -sin(angle), 0.0)
                values[2] = arrayListOf(0.0, sin(angle), cos(angle), 0.0)
                values[3] = arrayListOf(0.0, 0.0, 0.0, 1.0)
            }
        }

        fun getRotateYMatrix(angle: Double): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(cos(angle), 0.0, sin(angle), 0.0)
                values[1] = arrayListOf(0.0, 1.0, 0.0, 0.0)
                values[2] = arrayListOf(-sin(angle), 0.0, cos(angle), 0.0)
                values[3] = arrayListOf(0.0, 0.0, 0.0, 1.0)
            }
        }

        fun getRotateZMatrix(angle: Double): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(cos(angle), -sin(angle), 0.0, 0.0)
                values[1] = arrayListOf(sin(angle), cos(angle), 0.0, 0.0)
                values[2] = arrayListOf(0.0, 0.0, 1.0, 0.0)
                values[3] = arrayListOf(0.0, 0.0, 0.0, 1.0)
            }
        }

        fun getCamMatrix(camera: Vector, target: Vector): Matrix {
            val up = Vector(y = 1.0)
            val zAxis = (camera - target).normalized()
            val xAxis = (zAxis * up).normalized()
            val yAxis = xAxis * zAxis
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(xAxis.x, xAxis.y, xAxis.z, -(xAxis.scalarMul(camera)))
                values[1] = arrayListOf(yAxis.x, yAxis.y, yAxis.z, -(yAxis.scalarMul(camera)))
                values[2] = arrayListOf(zAxis.x, zAxis.y, zAxis.z, -(zAxis.scalarMul(camera)))
                values[3] = arrayListOf(0.0, 0.0, 0.0, 1.0)
            }
        }

        fun getOrthographicCamMatrix(width: Double, height: Double, zNear: Double, zFar: Double): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(2 / width, 0.0, 0.0, 0.0)
                values[1] = arrayListOf(0.0, 2 / height, 0.0, 0.0)
                values[2] = arrayListOf(0.0, 0.0, 1 / (zNear - zFar), zNear / (zNear - zFar))
                values[3] = arrayListOf(0.0, 0.0, 0.0, 1.0)
            }
        }

        fun getPerspectiveCamMatrix(width: Double, height: Double, zNear: Double, zFar: Double): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(2 * zNear / width, 0.0, 0.0, 0.0)
                values[1] = arrayListOf(0.0, 2 * zNear / height, 0.0, 0.0)
                values[2] = arrayListOf(0.0, 0.0, zFar / (zNear - zFar), zNear * zFar / (zNear - zFar))
                values[3] = arrayListOf(0.0, 0.0, -1.0, 0.0)
            }
        }

        fun getFOVCamMatrix(fov: Double, aspect: Double, zNear: Double, zFar: Double): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(1.0/(aspect * tan(fov/2)), 0.0, 0.0, 0.0)
                values[1] = arrayListOf(0.0, 1.0/ tan(fov/2), 0.0, 0.0)
                values[2] = arrayListOf(0.0, 0.0, zFar / (zNear - zFar), zNear * zFar / (zNear - zFar))
                values[3] = arrayListOf(0.0, 0.0, -1.0, 0.0)
            }
        }

        fun getViewportMatrix(width: Double, height: Double): Matrix {
            return Matrix(4, 4).apply {
                values[0] = arrayListOf(width/2, 0.0, 0.0, width/2)
                values[1] = arrayListOf(0.0, -height/2, 0.0, height/2)
                values[2] = arrayListOf(0.0, 0.0, 1.0, 0.0)
                values[3] = arrayListOf(0.0, 0.0, 0.0, 1.0)
            }
        }
    }
}