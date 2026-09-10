package lab.logic

import lab.logic.entity.model.Model
import lab.logic.entity.parts.Corner

data class OptimizerDebug(val extraBlocked: Boolean, val value: Double)

object PlanesAngleOptimizer {
    /**
     * accuracy: 0 - all collapse, 1 - all stay
     * maxNodes: how many nodes can be removed. if 0 - then all over accuracy limit
     *
     * Does not collapse UV seams or creases. The hole is filled from remaining
     * corners matched by (v, vt), not by geometry alone.
     */
    fun optimize(model: Model, accuracy: Double, maxNodes: Long = 0){
        if (model.v.isEmpty()) return

        val vertsBefore = model.v.size
        val facesBefore = model.f.size

        println("optimize start: $vertsBefore vertices, $facesBefore planes, accuracy=$accuracy")

        var connectionList = model.findConnections()
        var errors = DoubleArray(model.v.size) { findError(it, model, connectionList) }
        var (node, error) = findBestNode(errors)
        var nodesRemoved = 0L
        while (error >= accuracy && (maxNodes == 0L || nodesRemoved < maxNodes)){
            val neighbors = neighborVertices(node, model, connectionList)
            val facesNow = model.f.size
            val vertsNow = model.v.size
            removeNode(node, model, connectionList)

            if (model.f.size == facesNow && model.v.size == vertsNow) {
                errors[node] = Double.NEGATIVE_INFINITY
                val pair = findBestNode(errors)
                node = pair.first
                error = pair.second
                continue
            }

            nodesRemoved++
            if (nodesRemoved % 1000L == 0L) {
                println("removed $nodesRemoved: ${model.v.size} vertices, ${model.f.size} planes, error=$error")
            }

            connectionList = model.findConnections()
            errors = dropIndex(errors, node)
            for (n in neighbors) {
                val mapped = if (n > node) n - 1 else n
                if (mapped in errors.indices)
                    errors[mapped] = findError(mapped, model, connectionList)
            }

            val pair = findBestNode(errors)
            node = pair.first
            error = pair.second
        }

        println("optimize done: removed ${removedPercent(nodesRemoved, vertsBefore)}% vertices")
        println("vertices: $vertsBefore -> ${model.v.size} (removed ${removedPercent((vertsBefore - model.v.size).toLong(), vertsBefore)}%)")
        println("planes: $facesBefore -> ${model.f.size} (removed ${removedPercent((facesBefore - model.f.size).toLong(), facesBefore)}%)")
    }

    fun debugVertices(model: Model): Array<OptimizerDebug> {
        val connectionList = model.findConnections()
        return Array(model.v.size) { node ->
            val blocked = !canCollapse(node, model, connectionList)
            val value = if (blocked) Double.NaN
            else minOf(
                model.averageNormalAlignment(node, connectionList),
                model.minPairwiseNormalAlignment(node, connectionList)
            )
            OptimizerDebug(blocked, value)
        }
    }

    private fun findBestNode(errors: DoubleArray): Pair<Int, Double>{
        var bestNum = 0
        var bestError = errors[0]
        for (i in 1 until errors.size){
            if (errors[i] > bestError){
                bestNum = i
                bestError = errors[i]
            }
        }
        return bestNum to bestError
    }

    private fun dropIndex(errors: DoubleArray, removed: Int): DoubleArray{
        val next = DoubleArray(errors.size - 1)
        for (i in 0 until removed)
            next[i] = errors[i]
        for (i in removed + 1 until errors.size)
            next[i - 1] = errors[i]
        return next
    }

    private fun neighborVertices(node: Int, model: Model, connectionList: List<List<Int>>): Set<Int>{
        val res = HashSet<Int>()
        for (plane in connectionList.getOrNull(node) ?: return res) {
            val face = model.f.getOrNull(plane) ?: continue
            for (corner in face)
                if (corner.vNum != node)
                    res += corner.vNum
        }
        return res
    }

    private fun findError(node: Int, model: Model, connectionList: List<List<Int>>): Double{
        if (!canCollapse(node, model, connectionList)) return Double.NEGATIVE_INFINITY

        val average = model.averageNormalAlignment(node, connectionList)
        val crease = model.minPairwiseNormalAlignment(node, connectionList)
        return minOf(average, crease)
    }

    private fun canCollapse(node: Int, model: Model, connectionList: List<List<Int>>): Boolean{
        val planes = connectionList.getOrNull(node) ?: return false
        if (planes.size < 3) return false
        if (model.isVertexUvSeam(node, connectionList)) return false
        val edges = planes.mapNotNull { remainingEdge(model.f.getOrNull(it), node) }
        if (edges.size != planes.size) return false
        if (!isClosedRing(edges)) return false
        val chain = buildChain(edges, planes.size, model) ?: return false
        val originalMax = planes.maxOf { model.uvDiameter(model.f[it]) }
        return ModelLoader.triangulate(chain).none { model.triangleCrossesUv(it, originalMax) }
    }

    private fun removeNode(node: Int, model: Model, connectionList: List<List<Int>>){
        val planes = connectionList[node]
        val preview = planes.mapNotNull { remainingEdge(model.f.getOrNull(it), node) }
        val chain = buildChain(preview, planes.size, model) ?: return
        val originalMax = planes.maxOf { model.uvDiameter(model.f[it]) }
        val tris = ModelLoader.triangulate(chain)
        if (tris.any { model.triangleCrossesUv(it, originalMax) }) return

        val pairs = ArrayList<Pair<Corner, Corner>>()
        for (plane in planes.sortedDescending())
            deletePlane(plane, model, node)?.let { pairs += it }
        if (pairs.isEmpty()) return

        for (tri in tris)
            model.addPlane(tri)

        model.removeVertex(node)
    }

    private fun deletePlane(plane: Int, model: Model, node: Int): Pair<Corner, Corner>?{
        if (plane !in model.f.indices) return null
        val edge = remainingEdge(model.f[plane], node) ?: return null
        model.f.removeAt(plane)
        return edge
    }

    private fun remainingEdge(face: List<Corner>?, node: Int): Pair<Corner, Corner>?{
        if (face == null || face.size != 3) return null
        val i = face.indexOfFirst { it.vNum == node }
        if (i < 0) return null
        return face[(i + 1) % 3] to face[(i + 2) % 3]
    }

    private fun isClosedRing(edges: List<Pair<Corner, Corner>>): Boolean{
        if (edges.size < 3) return false
        val degree = HashMap<Int, Int>()
        for ((a, b) in edges) {
            if (a.vNum == b.vNum) return false
            degree[a.vNum] = (degree[a.vNum] ?: 0) + 1
            degree[b.vNum] = (degree[b.vNum] ?: 0) + 1
        }
        return degree.values.all { it == 2 }
    }

    private fun buildChain(pairs: List<Pair<Corner, Corner>>, expected: Int, model: Model): List<Corner>?{
        if (pairs.isEmpty() || expected < 3) return null

        var prev = pairs[0].first
        var next = pairs[0].second
        val chain = ArrayList<Corner>(expected)
        chain += prev
        chain += next
        while (chain.size < expected) {
            var progressed = false
            for (rib in pairs){
                if (model.sameChartVertex(next, rib.first) &&
                    !model.sameChartVertex(prev, rib.second)
                ){
                    chain += rib.second
                    prev = next
                    next = rib.second
                    progressed = true
                    break
                }
                if (model.sameChartVertex(next, rib.second) &&
                    !model.sameChartVertex(prev, rib.first)
                ){
                    chain += rib.first
                    prev = next
                    next = rib.first
                    progressed = true
                    break
                }
            }
            if (!progressed) return null
        }

        val first = chain.first()
        val last = chain.last()
        val closes = pairs.any {
            (model.sameChartVertex(it.first, last) &&
                model.sameChartVertex(it.second, first)) ||
            (model.sameChartVertex(it.second, last) &&
                model.sameChartVertex(it.first, first))
        }
        if (!closes) return null
        if (chain.map { it.vNum }.toSet().size != chain.size) return null
        return chain
    }

    private fun removedPercent(removed: Long, original: Int): String {
        if (original <= 0) return "0.0"
        return "%.1f".format(removed * 100.0 / original)
    }
}
