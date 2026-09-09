package lab.logic.entity.model

import lab.logic.entity.math.Vector
import lab.logic.entity.parts.Corner
import java.awt.Color
import java.awt.image.BufferedImage

/**
 *  for blender export forward - Z forward, up - Y up
 */
open class Model {
    var v = ArrayList<Vector>()   //вершина
    var vt = ArrayList<Vector>()  //вершина текстуры
    var vn = ArrayList<Vector>()  //вершина нормали
    var f = ArrayList<ArrayList<Corner>>()
    lateinit var textureImg: BufferedImage
    lateinit var normalImg: BufferedImage
    lateinit var mirrorImg: BufferedImage

    fun getDotNormal(textureCoords: Vector): Vector{
        val pixel = Color(normalImg.getRGB(textureCoords.x.toInt(), textureCoords.y.toInt()))
        return Vector(-1*(pixel.red/255.0 *2 - 1), pixel.green/255.0 *2 - 1, -1*(pixel.blue/255.0 *2 - 1))
    }

    fun calcTextureColor(textureCoords: Vector): Color{
        return Color(textureImg.getRGB(textureCoords.x.toInt(), textureCoords.y.toInt()))
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

    fun sortVerts(num: Int): ArrayList<Int>{
        val res = arrayListOf(0, 1, 2)
        for (i in 0..2)
            for (j in i..2)
                if (v[f[num][res[i]].vNum].y > v[f[num][res[j]].vNum].y){
                    val z = res[i]
                    res[i] = res[j]
                    res[j] = z
                }
        return res
    }

    fun calcPlaneNormal(num: Int): Vector {
        val ribs = arrayOf(
            Vector(1.0, -1.0, 1.0),
            v[f[num][1].vNum] - v[f[num][0].vNum],
            v[f[num][2].vNum] - v[f[num][0].vNum]
        )
        val x = ribs[0].x * (ribs[1].y * ribs[2].z - ribs[1].z * ribs[2].y)
        val y = ribs[0].y * (ribs[1].x * ribs[2].z - ribs[1].z * ribs[2].x)
        val z = ribs[0].z * (ribs[1].x * ribs[2].y - ribs[1].y * ribs[2].x)
        return Vector(x, y, z)
    }

    fun addPlane(plane: List<Corner>){
        if (plane.size != 3) return
        val a = plane[0]
        val b = plane[1]
        val c = plane[2]
        if (a.vNum == b.vNum || b.vNum == c.vNum || a.vNum == c.vNum) return
        if (a.vNum !in v.indices || b.vNum !in v.indices || c.vNum !in v.indices) return
        val area = (v[b.vNum] - v[a.vNum]) * (v[c.vNum] - v[a.vNum])
        if (area.length() == 0.0) return
        f += arrayListOf(a, b, c)
    }

    fun removeVertex(index: Int){
        if (index !in v.indices) return
        v.removeAt(index)
        for (face in f)
            for (i in face.indices)
                if (face[i].vNum > index)
                    face[i] = face[i].copy(vNum = face[i].vNum - 1)
    }
}