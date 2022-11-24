package logic.entity.model

import logic.entity.math.Vector
import java.io.BufferedReader
import java.io.FileReader

/**
 *  for blender export forward - Z forward, up - Y up
 */
class Model {
    var v = ArrayList<Vector>()   //вершина
    var vt = ArrayList<Vector>()  //вершина текстуры
    var vn = ArrayList<Vector>()  //вершина нормали
    var f = ArrayList<ArrayList<Vector>>()

    constructor(v: ArrayList<Vector>, vt: ArrayList<Vector>, vn: ArrayList<Vector>, f: ArrayList<ArrayList<Vector>>){
        this.v = v;
        this.vt = vt
        this.vn = vn
        this.f = f
    }

    constructor(path: String){
        val reader = BufferedReader(FileReader(path))
        val lines = reader.readLines()
        fillByLines(lines)
    }

    private fun getAvailableData(args: List<String>): Vector{
        if (args.size == 1)
            return Vector(args[0].toDouble() - 1)
        if (args.size == 2)
            return Vector(args[0].toDouble() - 1, args[1].toDouble() - 1)
        if (args.size == 3)
            return Vector(
                args[0].toDouble() - 1,
                args[1].toDouble() - 1,
                args[2].toDouble() - 1
            )
        return Vector()
    }

    private fun fillByLines(data: List<String>){
        for (_line in data) {
            val line = _line.replace("  ", " ")
            if (line.length < 2) continue
            when (line.substring(0, 2)) {
                "v " -> {
                    val args = line.split(" ")
                    v += Vector(args[1].toDouble(), args[2].toDouble(), args[3].toDouble())
                }
                "vt" -> {
                    var args = line.split(" ")
                    args = args.subList(1, args.size)
                    vt += getAvailableData(args)
                }
                "vn" -> {
                    val args = line.split(" ")
                    vn += Vector(args[1].toDouble(), args[2].toDouble(), args[3].toDouble())
                }
                "f " -> {
                    val result = ArrayList<Vector>()
                    val args = line.split(" ")
                    for (dot in args) {
                        if (dot == "f") continue
                        val values = dot.split("/").toMutableList()
                        for (i in values.indices)
                            if (values[i] == "")
                                values[i] = "0"
                        result += getAvailableData(values)
                    }
                    if (result.size > 3) {
                        for (pl in 2 until result.size)
                            f += arrayListOf(result[pl - 2], result[pl - 1], result[pl])
                        f += arrayListOf(result[result.size-2], result[result.size-1], result[0])
                    }else
                        f += result
                }
            }
        }
    }
}