import drawing.AppFrame
import logic.entity.math.Vector
import logic.entity.model.Model
import util.ModelLoader

fun main(){
    val model = ModelLoader().load(FILE)
    /*val model = Model(
        arrayListOf(
            Vector(),
            Vector(y=1.0),
            Vector(x=1.0)
        ),
        arrayListOf(),
        arrayListOf(),
        arrayListOf(
            arrayListOf(Vector(), Vector(1.0), Vector(2.0))
        )
    )*/
    AppFrame(model)
}