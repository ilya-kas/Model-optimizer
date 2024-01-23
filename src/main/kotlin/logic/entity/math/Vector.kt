package logic.entity.math

import kotlin.math.sqrt

data class Vector(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0, var w: Double = 1.0) {

    constructor(list: List<Double>): this(list[0], list[1], list[2], list[3])

    operator fun minus(target: Vector): Vector {
        return Vector(x - target.x, y - target.y, z - target.z)
    }

    operator fun plus(target: Vector): Vector {
        return Vector(x + target.x, y + target.y, z + target.z)
    }

    operator fun times(c: Double): Vector {
        return Vector(
            x = x*c,
            y = y*c,
            z = z*c
        )
    }

    // vector multiplication
    operator fun times(target: Vector): Vector {
        return Vector(
            x = y * target.z - z * target.y,
            y = z * target.x - x * target.z,
            z = x * target.y - y * target.x
        )
    }

    // vector * matrix multiplication
    operator fun times(target: Matrix): Vector {
        val res = mutableListOf(0.0, 0.0, 0.0, 0.0)
        val asList = this.toList()
        for (row in 0..3)
            for (column in 0..3)
                res[row] += target.values[row][column] * asList[column]
        return Vector(res)
    }

    operator fun div(target: Double): Vector {
        return Vector(x/target, y/target, z/target, w/target)
    }

    // scalar multiplication
    fun scalarMul(target: Vector): Double {
        return x * target.x + y * target.y + z * target.z
    }

    fun normalized(): Vector {
        val length = length()
        if (length == 0.0 || length == 1.0) return this
        val scalar = 1f / length
        return Vector(x*scalar, y*scalar, z*scalar)
    }

    fun length(): Double = sqrt(x * x + y * y + z * z)

    fun toList(): List<Double> = listOf(x,y,z,w)

    fun angleTo(other: Vector): Double = scalarMul(other)/(length() * other.length())
}