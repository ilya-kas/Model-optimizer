import lab.drawing.AppFrame
import lab.logic.ModelLoader
import lab.logic.PlaneAreaOptimizer
import lab.logic.PlanesAngleOptimizer

fun main(){
    val model = ModelLoader().load(FILE)
    PlanesAngleOptimizer.optimize(model, 0.95)
    PlaneAreaOptimizer.optimize(model, 0.95)
    AppFrame(model)
}