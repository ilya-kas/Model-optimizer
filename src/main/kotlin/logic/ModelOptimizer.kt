package logic

import logic.entity.model.Model
import java.util.LinkedList

object ModelOptimizer {
    /**
     * accuracy: 0 - all collapse, 1 - all stay
     * maxNodes: how many nodes can be removed. if 0 - then all over accuracy limit
     */
    fun optimize(model: Model, accuracy: Double, maxNodes: Long = 0){
        var connectionList = findConnections(model)
        var (node, error) = findBestNode(model, connectionList)
        var nodesRemoved = 0L
        while (error >= accuracy && (maxNodes == 0L || nodesRemoved < maxNodes)){
            removeNode(node, model, connectionList)
            nodesRemoved++

            connectionList = findConnections(model)
            val pair = findBestNode(model, connectionList)
            node = pair.first
            error = pair.second
        }
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
        for (plane in model.f.indices)
            for (corner in model.f[plane])
                res[corner.vNum] += plane
        return res
    }

    private fun findError(node: Int, model: Model, connectionList: List<List<Int>>): Double{
        var sumError = 0.0
        val dotN = model.vn[node] //todo fix v[i] != vn[i]
        for (plane in connectionList[node]){
            val n = model.calcPlaneNormal(plane)
            sumError += dotN.angleTo(n)
        }
        return sumError / connectionList[node].size.toDouble()
    }

    private fun removeNode(node: Int, model: Model, connectionList: List<List<Int>>){
        val pairs = LinkedList<Pair<Int, Int>>() //delete planes
        for (plane in connectionList[node])
            pairs += deletePlane(plane, model, node)

        var prev = pairs[0].first  //connect left ribs in a big plane
        var next = pairs[0].second
        val chain = LinkedList<Int>()
        chain += prev
        chain += next
        while (chain.size < connectionList[node].size)
            for (rib in pairs){
                if (next == rib.first && prev != rib.second){
                    chain += rib.second
                    prev = next
                    next = rib.second
                    break
                }
                if (next == rib.second && prev != rib.first){
                    chain += rib.first
                    prev = next
                    next = rib.first
                    break
                }
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