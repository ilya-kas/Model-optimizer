package util

import logic.entity.math.Vector
import logic.entity.model.Model
import java.io.BufferedReader
import java.io.FileReader

class ModelLoader {
    fun load(path: String): Model{
        val model = Model()

        val reader = BufferedReader(FileReader(path))
        val lines = reader.readLines()
        fillByLines(lines, model)

        return model
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

    private fun fillByLines(data: List<String>, model: Model){
        for (_line in data) {
            val line = _line.replace("  ", " ")
            if (line.length < 2) continue
            when (line.substring(0, 2)) {
                "v " -> {
                    val args = line.split(" ")
                    model.v += Vector(args[1].toDouble(), args[2].toDouble(), args[3].toDouble())
                }
                "vt" -> {
                    var args = line.split(" ")
                    args = args.subList(1, args.size)
                    model.vt += getAvailableData(args)
                }
                "vn" -> {
                    val args = line.split(" ")
                    model.vn += Vector(args[1].toDouble(), args[2].toDouble(), args[3].toDouble())
                }
                "f " -> {
                    val result = ArrayList<Model.Corner>()
                    val args = line.split(" ")
                    for (dot in args) {
                        if (dot == "f") continue
                        val values = dot.split("/").toMutableList()
                        for (i in values.indices)
                            if (values[i] == "")
                                values[i] = "0"
                        val availableData = getAvailableData(values)
                        result += Model.Corner(
                            availableData.x.toInt(),
                            availableData.y.toInt(),
                            availableData.z.toInt()
                        )
                    }
                    if (result.size > 3) {
                        for (pl in 2 until result.size)
                            model.f += arrayListOf(result[pl - 2], result[pl - 1], result[pl])
                        model.f += arrayListOf(result[result.size-2], result[result.size-1], result[0])
                    }else
                        model.f += result
                }
            }
        }
    }
}