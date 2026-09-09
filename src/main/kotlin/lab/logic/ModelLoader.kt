package lab.logic

import MIRROR
import NORMAL
import TEXTURE
import lab.logic.entity.math.Vector
import lab.logic.entity.model.Model
import lab.logic.entity.parts.Corner
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.LinkedList
import javax.imageio.ImageIO

class ModelLoader {
    fun load(path: String): Model {
        val model = Model()

        val reader = BufferedReader(FileReader(path))
        val lines = reader.readLines()
        fillByLines(lines, model)

        model.textureImg = ImageIO.read(File(TEXTURE))
        model.normalImg = ImageIO.read(File(NORMAL))
        model.mirrorImg = ImageIO.read(File(MIRROR))

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
                    model.vt += Vector(args[0].toDouble(), args[1].toDouble())
                }
                "vn" -> {
                    val args = line.split(" ")
                    model.vn += Vector(args[1].toDouble(), args[2].toDouble(), args[3].toDouble())
                }
                "f " -> {
                    val result = ArrayList<Corner>()
                    val args = line.split(" ")
                    for (dot in args) {
                        if (dot == "f") continue
                        val values = dot.split("/").toMutableList()
                        for (i in values.indices)
                            if (values[i] == "")
                                values[i] = "0"
                        val availableData = getAvailableData(values)
                        result += Corner(
                            availableData.x.toInt(),
                            availableData.y.toInt(),
                            availableData.z.toInt()
                        )
                    }
                    if (result.size > 3) {
                        for (i in 1 until result.size - 1)
                            model.f += arrayListOf(result[0], result[i], result[i + 1])
                    }else
                        model.f += result
                }
            }
        }
    }

    companion object {
        fun triangulate(list: List<Int>): List<List<Int>>{
            val res = LinkedList<ArrayList<Int>>()
            for (i in 1 until list.size - 1)
                res += arrayListOf(list[0], list[i], list[i + 1])
            return res
        }
    }
}
