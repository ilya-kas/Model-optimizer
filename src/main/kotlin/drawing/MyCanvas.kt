package drawing

import logic.entity.Vector
import java.awt.Canvas
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.*
import java.util.Collections.swap
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

class MyCanvas: Canvas(){
    private var planes: List<Vector> = ArrayList()
    private var image = BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB)
    private var zBuffer = ArrayList<Array<Double>>()
    private var colorData = Vector()

    override fun paint(g: Graphics) {
        super.paint(g)
        g.drawImage(image, 0, 0, null)
    }

    fun updateLines(v: List<Vector>, planes: List<Vector>){
        this.planes = planes
        image = BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB)
        zBuffer = ArrayList()
        for (i in 0 until frameHeight)
            zBuffer += Array(frameWidth){Double.MAX_VALUE}

        val graphics: Graphics2D = image.createGraphics()
        graphics.paint = Color(0, 0, 0)
        graphics.fillRect(0, 0, image.width, image.height)

        for (plane in planes) {
            val v1 = v[plane.x.toInt()]
            val v2 = v[plane.y.toInt()]
            val v3 = v[plane.z.toInt()]
            val x = mutableListOf(v1.x, v2.x, v3.x)
            val y = mutableListOf(v1.y, v2.y, v3.y)
            val z = mutableListOf(v1.z, v2.z, v3.z)
            fillPlane(x, y, z)
        }
    }

    private fun fillPlane(x: List<Double>, y: List<Double>, z: List<Double>){
        sortDots(x,y,z)

        val deltas = getDeltas(x, y)
        var dx13 = deltas.x
        var dx12 = deltas.y
        var dx23 = deltas.z
        var _dx13 = dx13
        if (dx13 > dx12) {
            val z1 = dx13
            dx13 = dx12
            dx12 = z1
        }


        var wx1 = x[0]
        var wx2 = wx1
        val averageZ = (z[0]+z[1]+z[2])/3

        val color = Color(colorData.x.toInt(), colorData.y.toInt(), colorData.z.toInt())
        for (i in y[0].toInt() until y[1].toInt()) {
            for (j in wx1.toInt()..wx2.toInt())
                if (0 <= j && j <image.width && 0 <= i && i <image.height)
                    //if (zBuffer[j][j] > averageZ) {
                        image.setRGB(j, i, color.rgb)
                    //    zBuffer[j][i] = averageZ
                    //}
            wx1 += dx13
            wx2 += dx12
        }

        if (y[0] == y[1]){
            wx1 = x[0]
            wx2 = x[1]
        }
        if (_dx13 < dx23){
            val z1 = _dx13
            _dx13 = dx23
            dx23 = z1
        }
        for (i in y[1].toInt()..y[2].toInt()) {
            for (j in wx1.toInt()..wx2.toInt())
                if (0 <= j && j <image.width && 0 <= i && i <image.height)
                    //if (zBuffer[j][j] > averageZ) {
                        image.setRGB(j, i, color.rgb)
                    //    zBuffer[j][i] = averageZ
                    //}
            wx1 += _dx13
            wx2 += dx23
        }

        colorData.x += 3
        colorData.x %= 255
        colorData.y += 3
        colorData.y %= 255
        colorData.z += 3
        colorData.z %= 255
    }

    private fun sortDots(x: List<Double>, y: List<Double>, z: List<Double>){
        //sort
        if (y[1] < y[0]) {
            swap(x, 0, 1)
            swap(y, 0, 1)
            swap(z, 0, 1)
        }
        if (y[2] < y[0]) {
            swap(x, 0, 2)
            swap(y, 0, 2)
            swap(z, 0, 2)
        }
        if (y[1] > y[2]) {
            swap(x, 1, 2)
            swap(y, 1, 2)
            swap(z, 1, 2)
        }
    }

    private fun getDeltas(x: List<Double>, y: List<Double>): Vector{
        //dx
        var dx13 = 0.0
        var dx12 = 0.0
        var dx23 = 0.0

        if (y[2] != y[0])
            dx13 = (x[2]-x[0]) / (y[2]-y[0])
        if (y[1] != y[0])
            dx12 = (x[1]-x[0]) / (y[1]-y[0])
        if (y[2] != y[1])
            dx23 = (x[2]-x[1]) / (y[2]-y[1])

        return Vector(dx13, dx12, dx23)
    }
}