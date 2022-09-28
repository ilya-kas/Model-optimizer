package drawing

import logic.WorldManager
import logic.entity.Model
import logic.entity.Matrix
import logic.entity.Vector
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import kotlin.math.PI


var frameWidth = 600
var frameHeight = 600
val zNear = 0.1
val zFar = 1.0
val fov = PI/3
var aspect = 1.0

val rotationStep = PI/32
val movementStep = 2.0

class AppFrame: JFrame() {
    private val canvas = MyCanvas()
    private var lastModel: Model? = null

    init {
        this.size = Dimension(frameWidth, frameHeight)
        this.add(canvas)

        this.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(event: ComponentEvent) {
                frameWidth = event.component.width
                frameHeight = event.component.height
                aspect = frameWidth/frameHeight.toDouble()

                lastModel?.let { draw(it) }
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
    }

    fun onModelChanged(){
        draw(lastModel!!)
    }

    fun draw(model: Model){
        lastModel = model

        val vertices = WorldManager.projectModel(model)

        val planes = ArrayList<Vector>()
        for (plane in model.f)
            planes += Vector(plane[0].x, plane[1].x, plane[2].x)
        canvas.updateLines(vertices, planes)

        this.repaint()
        canvas.repaint()
    }
}

