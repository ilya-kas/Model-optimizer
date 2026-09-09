package lab.logic

import lab.logic.entity.model.Model
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

        var connectionList = findConnections(model)
        var (node, error) = findBestNode(model, connectionList)
        var nodesRemoved = 0L
        while (error >= accuracy && (maxNodes == 0L || nodesRemoved < maxNodes)){
            val facesNow = model.f.size
            val vertsNow = model.v.size
            removeNode(node, model, connectionList)
            nodesRemoved++

            if (model.f.size == facesNow && model.v.size == vertsNow) break

            connectionList = findConnections(model)
            val pair = findBestNode(model, connectionList)
            node = pair.first
            error = pair.second
        }

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
        val connections = connectionList.getOrNull(node) ?: return Double.NEGATIVE_INFINITY
        if (connections.isEmpty()) return Double.NEGATIVE_INFINITY

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

    private fun removeNode(node: Int, model: Model, connectionList: List<List<Int>>){
        val pairs = LinkedList<Pair<Int, Int>>() //delete planes
        for (plane in connectionList[node])
            pairs += deletePlane(plane, model, node)
        if (pairs.isEmpty()) return

        var prev = pairs[0].first  //connect left ribs in a big plane
        var next = pairs[0].second
        val chain = LinkedList<Int>()
        chain += prev
        chain += next
        while (chain.size < connectionList[node].size) {
            var progressed = false
            for (rib in pairs){
                if (next == rib.first && prev != rib.second){
                    chain += rib.second
                    prev = next
                    next = rib.second
                    progressed = true
                    break
                }
                if (next == rib.second && prev != rib.first){
                    chain += rib.first
                    prev = next
                    next = rib.first
                    progressed = true
                    break
                }
            }
            if (!progressed) break
        }

        val planes = ModelLoader.triangulate(chain)
        for (plane in planes)
            model.addPlane(plane)
    }

    private fun deletePlane(plane: Int, model: Model, node: Int): Pair<Int, Int>{
        //todo
        return 0 to 0
    }
}
