package lab.logic

import lab.logic.entity.model.Model
import lab.logic.entity.parts.Corner

object PlanesAngleOptimizer {
    /**
     * accuracy: 0 - all collapse, 1 - all stay
     * maxNodes: how many nodes can be removed. if 0 - then all over accuracy limit
     */
    fun optimize(model: Model, accuracy: Double, maxNodes: Long = 0){
        if (model.v.isEmpty()) return

        val vertsBefore = model.v.size
        val facesBefore = model.f.size

        println("optimize start: $vertsBefore vertices, $facesBefore planes, accuracy=$accuracy")

        var connectionList = findConnections(model)
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

            connectionList = findConnections(model)
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

        println("optimize done: removed $nodesRemoved vertices")
        println("vertices: $vertsBefore -> ${model.v.size}")
        println("planes: $facesBefore -> ${model.f.size}")
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

    private fun findConnections(model: Model): List<List<Int>>{
        val res = ArrayList<ArrayList<Int>>(model.v.size)
        repeat(model.v.size) { res += ArrayList<Int>() }
        for (plane in model.f.indices)
            for (corner in model.f[plane])
                if (corner.vNum in res.indices && plane !in res[corner.vNum])
                    res[corner.vNum] += plane
        return res
    }

    private fun findError(node: Int, model: Model, connectionList: List<List<Int>>): Double{
        if (!canCollapse(node, model, connectionList)) return Double.NEGATIVE_INFINITY

        val connections = connectionList[node]
        val faceNormals = connections.map { model.calcPlaneNormal(it) }.filter { it.length() > 0.0 }
        if (faceNormals.isEmpty()) return Double.NEGATIVE_INFINITY

        var dotN = faceNormals.reduce { a, b -> a + b }
        if (dotN.length() == 0.0) return Double.NEGATIVE_INFINITY
        dotN = dotN.normalized()

        var sumError = 0.0
        for (n in faceNormals)
            sumError += dotN.angleTo(n)
        return sumError / faceNormals.size.toDouble()
    }

    private fun canCollapse(node: Int, model: Model, connectionList: List<List<Int>>): Boolean{
        val planes = connectionList.getOrNull(node) ?: return false
        if (planes.size < 3) return false
        val edges = planes.mapNotNull { remainingEdge(model.f.getOrNull(it), node) }
        if (edges.size != planes.size) return false
        if (!isClosedRing(edges)) return false
        return buildChain(edges, planes.size) != null
    }

    private fun removeNode(node: Int, model: Model, connectionList: List<List<Int>>){
        val planes = connectionList[node]
        val preview = planes.mapNotNull { remainingEdge(model.f.getOrNull(it), node) }
        val chain = buildChain(preview, planes.size) ?: return

        val pairs = ArrayList<Pair<Corner, Corner>>()
        for (plane in planes.sortedDescending())
            deletePlane(plane, model, node)?.let { pairs += it }
        if (pairs.isEmpty()) return

        for (tri in ModelLoader.triangulate(chain))
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

    private fun buildChain(pairs: List<Pair<Corner, Corner>>, expected: Int): List<Corner>?{
        if (pairs.isEmpty() || expected < 3) return null

        var prev = pairs[0].first
        var next = pairs[0].second
        val chain = ArrayList<Corner>(expected)
        chain += prev
        chain += next
        while (chain.size < expected) {
            var progressed = false
            for (rib in pairs){
                if (next.vNum == rib.first.vNum && prev.vNum != rib.second.vNum){
                    chain += rib.second
                    prev = next
                    next = rib.second
                    progressed = true
                    break
                }
                if (next.vNum == rib.second.vNum && prev.vNum != rib.first.vNum){
                    chain += rib.first
                    prev = next
                    next = rib.first
                    progressed = true
                    break
                }
            }
            if (!progressed) return null
        }

        val first = chain.first().vNum
        val last = chain.last().vNum
        val closes = pairs.any {
            (it.first.vNum == last && it.second.vNum == first) ||
            (it.second.vNum == last && it.first.vNum == first)
        }
        if (!closes) return null
        if (chain.map { it.vNum }.toSet().size != chain.size) return null
        return chain
    }
}
