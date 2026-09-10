package lab.util

import lab.drawing.AppFrame
import lab.logic.ModelLoader
import lab.logic.PlaneAreaOptimizer
import lab.logic.PlanesAngleOptimizer

fun main(){
    val loader = ModelLoader()
    val model = loader.load(FILE)
    PlanesAngleOptimizer.optimize(model, 0.7)
    PlaneAreaOptimizer.optimize(model, 0.9, 0.9)
    loader.save(model, FILE.removeSuffix(".obj") + "-optimized.obj")
    AppFrame(model)
}