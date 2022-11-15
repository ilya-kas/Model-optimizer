package logic.entity.model

import logic.entity.math.Vector
import java.util.Collections

class Plane(val x: List<Double>, val y: List<Double>, val z: List<Double>, val num: Int) {
    constructor(verts: List<Vector>, num: Int): this(
        mutableListOf(verts[0].x, verts[1].x, verts[2].x),
        mutableListOf(verts[0].y, verts[1].y, verts[2].y),
        mutableListOf(verts[0].z, verts[1].z, verts[2].z),
        num
    )

    fun getMid(): Vector {
        return Vector(x.sum()/3, y.sum()/3, z.sum()/3)
    }

    fun getCorner(index: Int): Vector = Vector(x[index], y[index], z[index])

    fun sort(){
        if (y[1] < y[0]) {
            Collections.swap(x, 0, 1)
            Collections.swap(y, 0, 1)
            Collections.swap(z, 0, 1)
        }
        if (y[2] < y[0]) {
            Collections.swap(x, 0, 2)
            Collections.swap(y, 0, 2)
            Collections.swap(z, 0, 2)
        }
        if (y[1] > y[2]) {
            Collections.swap(x, 1, 2)
            Collections.swap(y, 1, 2)
            Collections.swap(z, 1, 2)
        }
    }
}