package lab.util

import FILE
import lab.drawing.AppFrame
import lab.logic.ModelLoader
import lab.logic.PlaneAreaOptimizer
import lab.logic.PlanesAngleOptimizer

fun main(){
    val model = ModelLoader().load(FILE)
    PlanesAngleOptimizer.optimize(model, 0.97)
    PlaneAreaOptimizer.optimize(model, 0.97, 0.95)
    AppFrame(model)
}