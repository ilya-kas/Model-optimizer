package logic.entity

import java.io.BufferedReader
import java.io.FileReader

class Model {
    val v = ArrayList<Vector>()   //вершина
    val vt = ArrayList<Vector>()  //вершина текстуры
    val vn = ArrayList<Vector>()  //вершина нормали
    val f = ArrayList<ArrayList<Vector>>()

    constructor(data: List<String>){
        fillByLines(data)
    }

    constructor(path: String){
        val reader = BufferedReader(FileReader(path))
        val lines = reader.readLines()
        fillByLines(lines)
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
                    val args = line.split(" ")
                    vt += Vector(args[1].toDouble(), args[2].toDouble(), args[3].toDouble())
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
                        val values = dot.split("/")
                        if (values.size == 1)
                            result += Vector(values[0].toDouble() - 1)
                        if (values.size == 2)
                            result += Vector(values[0].toDouble() - 1, values[1].toDouble() - 1)
                        if (values.size == 3)
                            result += Vector(
                                values[0].toDouble() - 1,
                                values[1].toDouble() - 1,
                                values[2].toDouble() - 1
                            )
                    }
                    f += result
                }
            }
        }
    }
}