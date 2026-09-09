import lab.drawing.AppFrame
import lab.logic.ModelLoader
import lab.logic.ModelOptimizer

fun main(){
    val model = ModelLoader().load(FILE)
    ModelOptimizer.optimize(model, 0.95)
    AppFrame(model)
}