package lab.drawing

import lab.drawing.model.Camera
import lab.logic.entity.math.Matrix
import lab.logic.entity.math.ScreenDot
import lab.logic.entity.math.Vector
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
            image.setRGB(round(x).toInt(),round(y).toInt(), color.rgb)
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

    fun drawDot(x: Double, y: Double, z: Double, color: Color, radius: Int = 2){
        val cx = round(x).toInt()
        val cy = round(y).toInt()
        val nz = z - 1e-4
        for (dy in -radius..radius)
            for (dx in -radius..radius) {
                val px = cx + dx
                val py = cy + dy
                if (px !in 0 until image.width || py !in 0 until image.height) continue
                if (py !in zBuffer.indices || px !in zBuffer[py].indices) continue
                if (zBuffer[py][px] > nz) {
                    image.setRGB(px, py, color.rgb)
                    zBuffer[py][px] = nz
                }
            }
    }

    fun fillTopTriangle(a: ScreenDot, b: ScreenDot, c: ScreenDot, colorFun: (Double, Double) -> Color){
        var xl: Int
        var xr: Int
        var zl: Double
        var zr: Double
        for (Y in a.y until b.y) { // top half-triangle
            xl = a.x + ((b.x-a.x) * ((Y-a.y)/(b.y-a.y.toDouble()))).toInt()
            xr = a.x + ((c.x-a.x) * ((Y-a.y)/(c.y-a.y.toDouble()))).toInt()
            zl = a.z + (b.z-a.z) * ((Y-a.y)/(b.y-a.y.toDouble()))
            zr = a.z + (c.z-a.z) * ((Y-a.y)/(c.y-a.y.toDouble()))
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
            zl = b.z+ (c.z-b.z) * ((Y-b.y)/(c.y-b.y.toDouble()))
            zr = a.z+ (c.z-a.z) * ((Y-a.y)/(c.y-a.y.toDouble()))
            fillLine(xl, xr, zl, zr, Y, colorFun)
        }
    }

    private fun fillLine(_xl: Int, _xr: Int, _zl: Double, _zr: Double, Y: Int, colorFun: (Double, Double) -> Color){
        var xl = _xl
        var xr = _xr
        var zl = _zl
        var zr = _zr
        if (xl > xr){
            val t = xr
            xr = xl
            xl = t
            val tz = zr
            zr = zl
            zl = tz
        }
        val dx = xr - xl
        for (X in xl..xr)
            if (0 <= X && X < image.width && 0 <= Y && Y < image.height
                && Y in zBuffer.indices && X in zBuffer[Y].indices
            ) {
                val nz = if (dx == 0) zl else zl + (zr - zl) * ((X - xl.toDouble()) / dx)
                if (zBuffer[Y][X] > nz) {
                    val color = colorFun(X.toDouble(), Y.toDouble())

                    image.setRGB(X, Y, color.rgb)
                    zBuffer[Y][X] = nz
                }
            }
    }
}