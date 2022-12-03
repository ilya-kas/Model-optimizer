package logic.entity.model

import logic.entity.math.Vector
import java.io.BufferedReader
import java.io.FileReader

/**
 *  for blender export forward - Z forward, up - Y up
 */
open class Model {
    var v = ArrayList<Vector>()   //вершина
    var vt = ArrayList<Vector>()  //вершина текстуры
    var vn = ArrayList<Vector>()  //вершина нормали
    var f = ArrayList<ArrayList<Corner>>()

    fun calcPlaneNormal(num: Int): Vector{
        val verts = arrayListOf(
            v[f[num][0].vNum],
            v[f[num][1].vNum],
            v[f[num][2].vNum]
        )

        val ribs = arrayOf(
            Vector(1.0, -1.0, 1.0),
            verts[1] - verts[0],
            verts[2] - verts[0]
        )
        val x = ribs[0].x * (ribs[1].y * ribs[2].z - ribs[1].z * ribs[2].y)
        val y = ribs[0].y * (ribs[1].x * ribs[2].z - ribs[1].z * ribs[2].x)
        val z = ribs[0].z * (ribs[1].x * ribs[2].y - ribs[1].y * ribs[2].x)
        return Vector(x, y, z)
    }

    fun calcDotNormal(num: Int, barCoords: Vector): Vector{
        val resx = vn[f[num][0].vnNum].x * barCoords.x + vn[f[num][1].vnNum].x * barCoords.y + vn[f[num][2].vnNum].x * barCoords.z
        val resy = vn[f[num][0].vnNum].y * barCoords.x + vn[f[num][1].vnNum].y * barCoords.y + vn[f[num][2].vnNum].y * barCoords.z
        val resz = vn[f[num][0].vnNum].z * barCoords.x + vn[f[num][1].vnNum].z * barCoords.y + vn[f[num][2].vnNum].z * barCoords.z
        return Vector(resx, resy, resz)
    }

    fun calcPlaneMid(num: Int): Vector{
        val verts = arrayListOf(
            v[f[num][0].vNum],
            v[f[num][1].vNum],
            v[f[num][2].vNum]
        )
        val x = arrayListOf(verts[0].x, verts[1].x, verts[2].x)
        val y = arrayListOf(verts[0].y, verts[1].y, verts[2].y)
        val z = arrayListOf(verts[0].z, verts[1].z, verts[2].z)

        return Vector(x.sum()/3, y.sum()/3, z.sum()/3)
    }

    data class Corner(val vNum: Int, val vtNum: Int, val vnNum: Int)
}