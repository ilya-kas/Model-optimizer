import drawing.AppFrame
import logic.entity.Model

const val path = "cube 2.obj"

fun main(){
    val model = Model(path)
    val frame = AppFrame()
    frame.draw(model)
}