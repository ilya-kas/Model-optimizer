package lab.logic.entity.model

import lab.logic.entity.math.Vector
import lab.logic.entity.parts.Corner
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.sqrt

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

    companion object {
        const val UV_SEAM_THRESHOLD = 0.05
        const val UV_TRIANGLE_SPAN = 0.25
    }

    // Добавляет треугольник в модель, если вершины различны и площадь ненулевая.
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

    // Удаляет вершину и сдвигает индексы vNum у всех граней, ссылавшихся на вершины после неё.
    fun removeVertex(index: Int){
        if (index !in v.indices) return
        v.removeAt(index)
        for (face in f)
            for (i in face.indices)
                if (face[i].vNum > index)
                    face[i] = face[i].copy(vNum = face[i].vNum - 1)
    }

    // Строит для каждой вершины список индексов инцидентных граней.
    fun findConnections(): List<List<Int>>{
        val res = ArrayList<ArrayList<Int>>(v.size)
        repeat(v.size) { res += ArrayList<Int>() }
        for (plane in f.indices)
            for (corner in f[plane])
                if (corner.vNum in res.indices && plane !in res[corner.vNum])
                    res[corner.vNum] += plane
        return res
    }

    // Считает геометрическую нормаль плоскости по двум рёбрам треугольника.
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

    // Возвращает центроид треугольника как среднее его трёх вершин.
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

    // Возвращает индексы углов грани, отсортированные по убыванию экранной/мировой Y-координаты.
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

    // Средний косинус угла между средней нормалью и нормалями граней вокруг вершины (1 — всё сонаправлено).
    fun averageNormalAlignment(node: Int, connectionList: List<List<Int>>): Double {
        val connections = connectionList.getOrNull(node) ?: return Double.NEGATIVE_INFINITY
        val faceNormals = connections.map { calcPlaneNormal(it) }.filter { it.length() > 0.0 }
        if (faceNormals.isEmpty()) return Double.NEGATIVE_INFINITY

        var dotN = faceNormals.reduce { a, b -> a + b }
        if (dotN.length() == 0.0) return Double.NEGATIVE_INFINITY
        dotN = dotN.normalized()

        var sumError = 0.0
        for (n in faceNormals)
            sumError += dotN.angleTo(n)
        return sumError / faceNormals.size.toDouble()
    }

    // Минимальный косинус угла между любой парой нормалей граней у вершины (ниже — острее сгиб).
    fun minPairwiseNormalAlignment(node: Int, connectionList: List<List<Int>>): Double {
        val connections = connectionList.getOrNull(node) ?: return Double.NEGATIVE_INFINITY
        val normals = connections.map { calcPlaneNormal(it) }.filter { it.length() > 0.0 }
        if (normals.isEmpty()) return Double.NEGATIVE_INFINITY
        if (normals.size == 1) return 1.0
        var min = Double.POSITIVE_INFINITY
        for (i in normals.indices)
            for (j in i + 1 until normals.size) {
                val cos = normals[i].angleTo(normals[j])
                if (cos < min) min = cos
            }
        return min
    }

    // Проверяет, лежит ли вершина на сгибе: минимальное выравнивание нормалей ниже порога.
    fun isCrease(node: Int, connectionList: List<List<Int>>, angleAccuracy: Double): Boolean =
        minPairwiseNormalAlignment(node, connectionList) < angleAccuracy

    // Возвращает UV-координаты угла или null, если vtNum некорректен.
    fun uvOf(corner: Corner): Vector? = vt.getOrNull(corner.vtNum)

    // Считает расстояние между двумя точками в UV-пространстве.
    fun uvDistance(a: Vector, b: Vector): Double {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    // Проверяет, достаточно ли близки UV двух углов, чтобы считать их одним островом развёртки.
    fun uvsClose(a: Corner, b: Corner): Boolean {
        if (a.vtNum == b.vtNum) return true
        val ua = uvOf(a) ?: return false
        val ub = uvOf(b) ?: return false
        return uvDistance(ua, ub) <= UV_SEAM_THRESHOLD
    }

    // Проверяет, что два угла — одна геометрическая вершина на одном UV-острове.
    fun sameChartVertex(a: Corner, b: Corner): Boolean =
        a.vNum == b.vNum && uvsClose(a, b)

    // Максимальное расстояние в UV между любой парой углов списка.
    fun uvDiameter(corners: List<Corner>): Double {
        val uvs = corners.mapNotNull { uvOf(it) }
        if (uvs.size < 2) return 0.0
        var max = 0.0
        for (i in uvs.indices)
            for (j in i + 1 until uvs.size) {
                val d = uvDistance(uvs[i], uvs[j])
                if (d > max) max = d
            }
        return max
    }

    // Проверяет, есть ли у вершины несколько сильно различающихся UV (текстурный шов).
    fun isVertexUvSeam(node: Int, connectionList: List<List<Int>>): Boolean {
        val uvs = ArrayList<Vector>()
        for (plane in connectionList.getOrNull(node) ?: return false) {
            val face = f.getOrNull(plane) ?: continue
            for (corner in face) {
                if (corner.vNum != node) continue
                uvOf(corner)?.let { uvs += it }
            }
        }
        if (uvs.size < 2) return false
        for (i in uvs.indices)
            for (j in i + 1 until uvs.size)
                if (uvDistance(uvs[i], uvs[j]) > UV_SEAM_THRESHOLD)
                    return true
        return false
    }

    // Проверяет, не пересекает ли новый треугольник чужие острова атласа по сравнению с исходными гранями.
    fun triangleCrossesUv(tri: List<Corner>, originalMax: Double): Boolean {
        val now = uvDiameter(tri)
        val limit = originalMax * 3.0
        return now > maxOf(limit, UV_TRIANGLE_SPAN)
    }

    // Читает нормаль из карты нормалей по текстурным координатам пикселя.
    fun getDotNormal(textureCoords: Vector): Vector{
        val pixel = sample(normalImg, textureCoords)
        return Vector(-1*(pixel.red/255.0 *2 - 1), pixel.green/255.0 *2 - 1, -1*(pixel.blue/255.0 *2 - 1))
    }

    // Читает цвет диффузной текстуры по текстурным координатам пикселя.
    fun calcTextureColor(textureCoords: Vector): Color{
        return sample(textureImg, textureCoords)
    }

    // Берёт цвет пикселя изображения, переводя UV в индексы текселя.
    private fun sample(img: BufferedImage, textureCoords: Vector): Color {
        val x = toTexel(textureCoords.x, textureImg.width, img.width)
        val y = toTexel(textureCoords.y, textureImg.height, img.height)
        return Color(img.getRGB(x, y))
    }

    // Переводит непрерывную текстурную координату в индекс пикселя целевого изображения.
    private fun toTexel(coord: Double, fromSize: Int, toSize: Int): Int {
        if (toSize <= 1 || !coord.isFinite()) return 0
        val t = coord / (fromSize - 1).coerceAtLeast(1)
        return (t * (toSize - 1)).toInt().coerceIn(0, toSize - 1)
    }
}
