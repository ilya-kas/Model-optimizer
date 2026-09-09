package lab.logic.entity.math

class ScreenDot(vector: Vector) {
    var x = vector.x.toInt()
    var y = vector.y.toInt()
    var z = vector.z

    override fun toString(): String {
        return "($x, $y, $z)"
    }
}