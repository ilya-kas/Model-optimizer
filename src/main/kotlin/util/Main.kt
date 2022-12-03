import drawing.AppFrame
import logic.entity.math.Vector
import logic.entity.model.Model
import util.ModelLoader

fun main(){
    val model = ModelLoader().load(FILE)
    AppFrame(model)
}