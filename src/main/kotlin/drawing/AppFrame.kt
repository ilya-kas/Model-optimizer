package drawing

import drawing.model.VisibleModel
import logic.currentLight
import logic.entity.math.Vector
import logic.entity.model.Model
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import kotlin.math.PI

var frameWidth = 700
var frameHeight = 700
const val zNear = 0.1
const val zFar = 100.0
const val fov = PI/3
var aspect = 1.0

class AppFrame(model: Model): JFrame() {
    private val canvas = MyCanvas()
    private val visibleModel = VisibleModel(model, canvas)

    init {
        this.size = Dimension(frameWidth, frameHeight)
        this.add(canvas)

        this.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent) {
                frameWidth = event.component.width
                frameHeight = event.component.height
                aspect = frameWidth/frameHeight.toDouble()

                visibleModel.render()
            }

            override fun componentHidden(event: ComponentEvent) {
                isVisible = false
            }

            override fun componentShown(event: ComponentEvent) {
                isVisible = true
            }
        })
        this.isVisible = true

        this.addKeyListener(KeyboardListener(this))
        visibleModel.render()
    }

    fun onModelChanged(){
        visibleModel.render()

        drawWorldVectors()

        canvas.repaint()
        this.repaint()
    }

    private fun drawWorldVectors(){
        canvas.drawVectorW(Vector(1.0), color = Color.RED)
        canvas.drawVectorW(Vector(y=1.0), color = Color.GREEN)
        canvas.drawVectorW(Vector(z=1.0), color = Color.BLUE)

        canvas.drawVectorW(currentLight.normalized(), color = Color.YELLOW)
    }
}

