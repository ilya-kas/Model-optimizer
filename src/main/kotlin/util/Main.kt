import drawing.AppFrame
import logic.ModelOptimizer
import logic.ModelLoader

fun main(){
    val model = ModelLoader().load(FILE)
    ModelOptimizer.optimize(model, 0.95)
    AppFrame(model)
}