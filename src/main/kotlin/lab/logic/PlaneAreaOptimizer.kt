package lab.logic

import lab.logic.entity.math.Vector
import lab.logic.entity.model.Model
import lab.logic.entity.parts.Corner
import kotlin.math.sqrt

object PlaneAreaOptimizer {
    /**
     * accuracy: 0 - all collapse, 1 - all stay (area)
     * angleAccuracy: 0 - all collapse, 1 - all stay (same metric as PlanesAngleOptimizer)
     * maxNodes: how many planes can be removed. if 0 - then all over accuracy limit
     *
     * Collapses a triangle into its centroid if its area
     * is at most ((1 - accuracy) * modelDiagonal)^2,
     * the neighborhood is flatter than angleAccuracy,
     * and the vertices are not on a UV seam or crease.
     * Remapped corners keep a single averaged vt/vn of the same UV island.
     */
    fun optimize(model: Model, accuracy: Double, angleAccuracy: Double, maxNodes: Long = 0){
        if (model.v.isEmpty() || model.f.isEmpty()) return

        val vertsBefore = model.v.size
        val facesBefore = model.f.size
        val diagonal = modelDiagonal(model)
        val scale = (1.0 - accuracy) * diagonal
        val threshold = scale * scale

        println("area optimize start: $vertsBefore vertices, $facesBefore planes, accuracy=$accuracy, angleAccuracy=$angleAccuracy, threshold=$threshold")

        val skipped = HashSet<Int>()
        var planesRemoved = 0L
        while (maxNodes == 0L || planesRemoved < maxNodes){
            val (plane, area) = findBestPlane(model, skipped, angleAccuracy)
            if (plane < 0 || area > threshold) break

            val facesNow = model.f.size
            val vertsNow = model.v.size
            if (!collapsePlane(plane, model)) {
                skipped += plane
                continue
            }

            if (model.f.size == facesNow && model.v.size == vertsNow) {
                skipped += plane
                continue
            }

            skipped.clear()
            planesRemoved++
            if (planesRemoved % 1000L == 0L) {
                println("removed $planesRemoved: ${model.v.size} vertices, ${model.f.size} planes, area=$area")
            }
        }

        println("area optimize done: removed ${removedPercent(planesRemoved, facesBefore)}% planes")
        println("vertices: $vertsBefore -> ${model.v.size} (removed ${removedPercent((vertsBefore - model.v.size).toLong(), vertsBefore)}%)")
        println("planes: $facesBefore -> ${model.f.size} (removed ${removedPercent((facesBefore - model.f.size).toLong(), facesBefore)}%)")
    }

    private fun findBestPlane(model: Model, skipped: Set<Int>, angleAccuracy: Double): Pair<Int, Double>{
        val connectionList = model.findConnections()
        var bestNum = -1
        var bestArea = Double.POSITIVE_INFINITY
        for (i in model.f.indices){
            if (i in skipped || !canCollapse(i, model, connectionList, angleAccuracy)) continue
            val area = planeArea(model, i)
            if (area < bestArea){
                bestNum = i
                bestArea = area
            }
        }
        return bestNum to bestArea
    }

    private fun canCollapse(
        plane: Int,
        model: Model,
        connectionList: List<List<Int>>,
        angleAccuracy: Double
    ): Boolean{
        val face = model.f.getOrNull(plane) ?: return false
        if (face.size != 3) return false
        val verts = face.map { it.vNum }
        if (verts.any { it !in model.v.indices }) return false
        if (verts.toSet().size != 3) return false
        for (v in verts) {
            if (model.isVertexUvSeam(v, connectionList)) return false
            if (model.isCrease(v, connectionList, angleAccuracy)) return false
            if (model.averageNormalAlignment(v, connectionList) < angleAccuracy) return false
        }
        return true
    }

    private fun hasTopology(plane: Int, model: Model): Boolean{
        val face = model.f.getOrNull(plane) ?: return false
        if (face.size != 3) return false
        val verts = face.map { it.vNum }
        if (verts.any { it !in model.v.indices }) return false
        return verts.toSet().size == 3
    }

    private fun onSameIsland(model: Model, island: List<Corner>, corner: Corner): Boolean {
        if (island.any { it.vtNum == corner.vtNum }) return true
        val uv = model.uvOf(corner) ?: return true
        return island.any { other ->
            val otherUv = model.uvOf(other) ?: return@any false
            model.uvDistance(uv, otherUv) <= Model.UV_SEAM_THRESHOLD
        }
    }

    private fun collapsePlane(plane: Int, model: Model): Boolean{
        if (!hasTopology(plane, model)) return false

        val island = model.f[plane].toList()
        val a = island[0].vNum
        val b = island[1].vNum
        val c = island[2].vNum
        val collapsed = setOf(a, b, c)

        val pa = model.v[a]
        val pb = model.v[b]
        val pc = model.v[c]
        val mid = Vector(
            (pa.x + pb.x + pc.x) / 3.0,
            (pa.y + pb.y + pc.y) / 3.0,
            (pa.z + pb.z + pc.z) / 3.0
        )
        val midIndex = model.v.size
        model.v += mid
        val (vtIndex, vnIndex) = addAverageAttributes(model, island)

        val toDelete = ArrayList<Int>()
        for (i in model.f.indices) {
            val corners = model.f[i]
            for (j in corners.indices) {
                val corner = corners[j]
                if (corner.vNum !in collapsed || !onSameIsland(model, island, corner)) continue
                corners[j] = corner.copy(
                    vNum = midIndex,
                    vtNum = if (vtIndex >= 0) vtIndex else corner.vtNum,
                    vnNum = if (vnIndex >= 0) vnIndex else corner.vnNum
                )
            }
            if (corners.map { it.vNum }.toSet().size < 3)
                toDelete += i
        }

        for (i in toDelete.sortedDescending())
            model.f.removeAt(i)

        for (index in collapsed.sortedDescending()) {
            val used = model.f.any { faceCorners -> faceCorners.any { it.vNum == index } }
            if (!used)
                model.removeVertex(index)
        }

        return true
    }

    private fun addAverageAttributes(model: Model, face: List<Corner>): Pair<Int, Int>{
        var tx = 0.0
        var ty = 0.0
        var vtCount = 0
        var nx = 0.0
        var ny = 0.0
        var nz = 0.0
        var vnCount = 0
        for (corner in face) {
            if (corner.vtNum in model.vt.indices) {
                tx += model.vt[corner.vtNum].x
                ty += model.vt[corner.vtNum].y
                vtCount++
            }
            if (corner.vnNum in model.vn.indices) {
                val n = model.vn[corner.vnNum]
                nx += n.x
                ny += n.y
                nz += n.z
                vnCount++
            }
        }
        val vtIndex = if (vtCount > 0) {
            model.vt += Vector(tx / vtCount, ty / vtCount)
            model.vt.lastIndex
        } else -1
        val vnIndex = if (vnCount > 0) {
            model.vn += Vector(nx / vnCount, ny / vnCount, nz / vnCount)
            model.vn.lastIndex
        } else -1
        return vtIndex to vnIndex
    }

    private fun planeArea(model: Model, num: Int): Double{
        val face = model.f.getOrNull(num) ?: return Double.POSITIVE_INFINITY
        if (face.size != 3) return Double.POSITIVE_INFINITY
        if (face.any { it.vNum !in model.v.indices }) return Double.POSITIVE_INFINITY
        val a = model.v[face[0].vNum]
        val b = model.v[face[1].vNum]
        val c = model.v[face[2].vNum]
        return 0.5 * ((b - a) * (c - a)).length()
    }

    private fun modelDiagonal(model: Model): Double{
        val first = model.v.first()
        var minX = first.x
        var maxX = first.x
        var minY = first.y
        var maxY = first.y
        var minZ = first.z
        var maxZ = first.z
        for (p in model.v) {
            if (p.x < minX) minX = p.x
            if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y
            if (p.y > maxY) maxY = p.y
            if (p.z < minZ) minZ = p.z
            if (p.z > maxZ) maxZ = p.z
        }
        val dx = maxX - minX
        val dy = maxY - minY
        val dz = maxZ - minZ
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun removedPercent(removed: Long, original: Int): String {
        if (original <= 0) return "0.0"
        return "%.1f".format(removed * 100.0 / original)
    }
}
