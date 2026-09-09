package lab.logic

import lab.logic.entity.model.Model
import lab.logic.entity.parts.Corner
import java.util.LinkedList

object ModelOptimizer {
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
        var (node, error) = findBestNode(model, connectionList)
        var nodesRemoved = 0L
        while (error >= accuracy && (maxNodes == 0L || nodesRemoved < maxNodes)){
            val facesNow = model.f.size
            val vertsNow = model.v.size
            removeNode(node, model, connectionList)

            if (model.f.size == facesNow && model.v.size == vertsNow) break

            nodesRemoved++
            if (nodesRemoved % 100L == 0L) {
                println("removed $nodesRemoved: ${model.v.size} vertices, ${model.f.size} planes, error=$error")
            }

            connectionList = findConnections(model)
            val pair = findBestNode(model, connectionList)
            node = pair.first
            error = pair.second
        }

        println("optimize done: removed ${nodesRemoved/model.v.size*100}% of vertices")
        println("vertices: $vertsBefore -> ${model.v.size}")
        println("planes: $facesBefore -> ${model.f.size}")
    }

    private fun findBestNode(model: Model, connectionList: List<List<Int>>): Pair<Int, Double>{
        var bestNum = 0
        var bestError = findError(bestNum, model, connectionList)
        for (i in 1 until model.v.size){
            val error = findError(i, model, connectionList)
            if (error > bestError){
                bestNum = i
                bestError = error
            }
        }
        return bestNum to bestError
    }

    private fun findConnections(model: Model): List<List<Int>>{
        val res = ArrayList<LinkedList<Int>>(model.v.size)
        repeat(model.v.size) { res += LinkedList<Int>() }
        for (plane in model.f.indices)
            for (corner in model.f[plane])
                if (corner.vNum in res.indices)
                    res[corner.vNum] += plane
        return res
    }

    private fun findError(node: Int, model: Model, connectionList: List<List<Int>>): Double{
        if (!canCollapse(node, model, connectionList)) return Double.NEGATIVE_INFINITY

        val connections = connectionList[node].distinct()
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
        val planes = connectionList.getOrNull(node)?.distinct() ?: return false
        if (planes.size < 3) return false
        val edges = planes.mapNotNull { remainingEdge(model.f.getOrNull(it), node) }
        if (edges.size != planes.size) return false
        if (!isClosedRing(edges)) return false
        return buildChain(edges, planes.size) != null
    }

    private fun removeNode(node: Int, model: Model, connectionList: List<List<Int>>){
        val planes = connectionList[node].distinct()
        val preview = planes.mapNotNull { remainingEdge(model.f.getOrNull(it), node) }
        val chain = buildChain(preview, planes.size) ?: return

        val pairs = LinkedList<Pair<Corner, Corner>>()
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
