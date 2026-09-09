/*
package logic.entity.parts

import logic.entity.math.Vector
import logic.entity.model.Model

class Plane(private val model: Model, val corners: List<Corner>) {    

    fun calcPlaneNormal(): Vector {
        val ribs = arrayOf(
            Vector(1.0, -1.0, 1.0),
            v[1] - v[0],
            v[2] - v[0]
        )
        val x = ribs[0].x * (ribs[1].y * ribs[2].z - ribs[1].z * ribs[2].y)
        val y = ribs[0].y * (ribs[1].x * ribs[2].z - ribs[1].z * ribs[2].x)
        val z = ribs[0].z * (ribs[1].x * ribs[2].y - ribs[1].y * ribs[2].x)
        return Vector(x, y, z)
    }

    fun calcPlaneMid(): Vector{
        val x = arrayListOf(v[0].x, v[1].x, v[2].x)
        val y = arrayListOf(v[0].y, v[1].y, v[2].y)
        val z = arrayListOf(v[0].z, v[1].z, v[2].z)

        return Vector(x.sum()/3, y.sum()/3, z.sum()/3)
    }

    fun sortVerts(): ArrayList<Int>{
        val res = arrayListOf(0, 1, 2)
        for (i in 0..2)
            for (j in i..2)
                if (v[res[i]].y > v[res[j]].y){
                    val z = res[i]
                    res[i] = res[j]
                    res[j] = z
                }
        return res
    }
}*/
