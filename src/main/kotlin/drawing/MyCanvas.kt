package drawing

import drawing.model.Camera
import logic.entity.math.Matrix
import logic.entity.math.ScreenDot
import logic.entity.math.Vector
import java.awt.Canvas
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

class MyCanvas: Canvas(){
    private var zBuffer = Array(frameHeight){ Array(frameWidth){Double.POSITIVE_INFINITY} }
    private var image = BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB)

    override fun paint(g: Graphics) {
        super.paint(g)
        g.drawImage(image, 0, 0, null)
    }

    fun clear(){
        image = BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()  //fill background with black color
        graphics.paint = Color(0, 0, 0)
        graphics.fillRect(0, 0, image.width, image.height)

        zBuffer = Array(frameHeight){ Array(frameWidth){Double.POSITIVE_INFINITY} }
    }

    fun ddaLine(x1: Double, y1: Double, x2: Double, y2: Double, color: Color = Color.WHITE){
        val steps = round(max(abs(x2-x1), abs(y2-y1))).toInt()

        var x = x1
        var y = y1
        if (0<=round(x) && round(x)<image.width && 0<=round(y) && round(y)<image.height)
            image.setRGB(round(x).toInt(),round(y).toInt(), Color.WHITE.rgb)
        for (i in 0 until steps){
            x += (x2-x1)/steps
            y += (y2-y1)/steps
            if (0<=round(x) && round(x)<image.width && 0<=round(y) && round(y)<image.height)
                image.setRGB(round(x).toInt(),round(y).toInt(), color.rgb)
        }
    }

    /**
     * input - vector in World coordinates
     */
    fun drawVectorW(vector: Vector, offset: Vector = Vector(), color: Color){
        val viewportMatrix = Matrix.getViewportMatrix(frameWidth.toDouble(), frameHeight.toDouble())

        var projectedVector = (vector + offset) * Camera.getCameraMatrix()
        projectedVector /= projectedVector.w
        projectedVector *= viewportMatrix

        var projectedOffset = offset * Camera.getCameraMatrix()
        projectedOffset /= projectedOffset.w
        projectedOffset *= viewportMatrix

        ddaLine(projectedOffset.x, projectedOffset.y, projectedVector.x, projectedVector.y, color)
    }

    fun fillTopTriangle(a: ScreenDot, b: ScreenDot, c: ScreenDot, colorFun: (Double, Double) -> Color){
        var xl: Int
        var xr: Int
        var zl: Double
        var zr: Double
        for (Y in a.y until b.y) { // bottom half-triangle
            xl = a.x + ((b.x-a.x) * ((Y-a.y)/(b.y-a.y.toDouble()))).toInt()
            xr = a.x + ((c.x-a.x) * ((Y-a.y)/(c.y-a.y.toDouble()))).toInt()
            zl = a.z+ (b.z-a.z) * ((Y-a.y)/(b.y-a.y))
            zr = a.z+ (c.z-a.z) * ((Y-a.y)/(c.y-a.y))
            fillLine(xl, xr, zl, zr, Y, colorFun)
        }
    }

    fun fillBottomTriangle(a: ScreenDot, b: ScreenDot, c: ScreenDot, colorFun: (Double, Double) -> Color){
        var xl: Int
        var xr: Int
        var zl: Double
        var zr: Double
        for (Y in b.y until c.y) { // bottom half-triangle
            xl = b.x+ ((c.x-b.x) * ((Y-b.y)/(c.y-b.y.toDouble()))).toInt()
            xr = a.x+ ((c.x-a.x) * ((Y-a.y)/(c.y-a.y.toDouble()))).toInt()
            zl = b.z+ (c.z-b.z) * ((Y-b.y)/(c.y-b.y))
            zr = a.z+ (c.z-a.z) * ((Y-a.y)/(c.y-a.y))
            fillLine(xl, xr, zl, zr, Y, colorFun)
        }
    }

    private fun fillLine(_xl: Int, _xr: Int, zl: Double, zr: Double, Y: Int, colorFun: (Double, Double) -> Color){
        var nz: Double

        var xl = _xl
        var xr = _xr
        if (xl > xr){
            val t = xr
            xr = xl
            xl = t
        }
        for (X in xl..xr)
            if (0 <= X && X <image.width && 0 <= Y && Y <image.height) {
                nz = zl + (zr - zl) * ((X - xl.toDouble()) / (xr - xl))
                if (zBuffer.size < frameHeight)
                    Thread.sleep(20)
                if (zBuffer[X][Y] > nz) {
                    val color = colorFun(X.toDouble(), Y.toDouble())

                    image.setRGB(X, Y, color.rgb)
                    zBuffer[X][Y] = nz
                }
            }
    }
}