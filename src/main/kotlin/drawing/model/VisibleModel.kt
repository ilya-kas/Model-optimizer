package drawing.model

import drawing.MyCanvas
import drawing.frameHeight
import drawing.frameWidth
import logic.entity.math.Matrix
import logic.entity.math.Vector
import logic.entity.math.ScreenDot
import logic.entity.model.Model
import logic.entity.model.Plane
import java.awt.Color
import kotlin.math.abs

var showGrid = true
var showNormals = false

class VisibleModel(private val model: Model, private val canvas: MyCanvas) {
    private var worldMatrix = WorldManager.getWorldMatrix()
    private var presentedWorldMatrix = Camera.getCameraMatrix() * worldMatrix
    private var viewportMatrix = Matrix.getViewportMatrix(frameWidth.toDouble(), frameHeight.toDouble())

    fun render(){
        worldMatrix = WorldManager.getWorldMatrix()
        presentedWorldMatrix = Camera.getCameraMatrix() * worldMatrix
        viewportMatrix = Matrix.getViewportMatrix(frameWidth.toDouble(), frameHeight.toDouble())

        val vertices = ArrayList<Vector>()
        for (v in model.v) {
            var newbie = v * presentedWorldMatrix
            newbie /= newbie.w
            newbie *= viewportMatrix
            vertices += newbie
            //println("${newbie.x} ${newbie.y} ${newbie.z} ${newbie.w}")
        }
        val planes = ArrayList<Plane>()
        for (plane in model.f){
            val v1 = vertices[plane[0].x.toInt()]
            val v2 = vertices[plane[1].x.toInt()]
            val v3 = vertices[plane[2].x.toInt()]
            planes += Plane(listOf(v1, v2, v3), planes.size)
        }

        canvas.clear()
        for (plane in planes) {
            fillPlane(plane)
            if (showNormals)
                showNormal(plane)
        }
        if (showGrid)
            for (plane in planes) {
                canvas.ddaLine(plane.x[0], plane.y[0], plane.x[1], plane.y[1])
                canvas.ddaLine(plane.x[0], plane.y[0], plane.x[2], plane.y[2])
                canvas.ddaLine(plane.x[2], plane.y[2], plane.x[1], plane.y[1])
            }
    }

    private fun showNormal(_plane: Plane){
        val projectedVerts = getProjectedPlane(model.f[_plane.num])
        val plane = Plane(projectedVerts, _plane.num)
        canvas.drawVector(calcNormal(model.f[_plane.num]).normalized(), plane.getMid(), Color.RED)
    }

    private fun getProjectedPlane(plane: List<Vector>): List<Vector>{
        val verts = arrayListOf(
            model.v[plane[0].x.toInt()],
            model.v[plane[1].x.toInt()],
            model.v[plane[2].x.toInt()]
        )
        val projectedVerts = ArrayList<Vector>()
        for (i in 0..2)
            projectedVerts += verts[i] * worldMatrix
        return projectedVerts
    }

    private fun calcNormal(plane: List<Vector>): Vector {
        val projectedVerts = getProjectedPlane(plane)

        val ribs = arrayOf(
            Vector(1.0, -1.0, 1.0),
            projectedVerts[1] - projectedVerts[0],
            projectedVerts[2] - projectedVerts[0]
        )
        val x = ribs[0].x * (ribs[1].y * ribs[2].z - ribs[1].z * ribs[2].y)
        val y = ribs[0].y * (ribs[1].x * ribs[2].z - ribs[1].z * ribs[2].x)
        val z = ribs[0].z * (ribs[1].x * ribs[2].y - ribs[1].y * ribs[2].x)
        val res = Vector(x, y, z)

        /*println(projectedVerts[0])
        println(projectedVerts[1])
        println(projectedVerts[2])
        println(res)*/
        return res
    }

    private fun isPlaneInvisible(plane: Plane): Boolean{
        val normal = calcNormal(model.f[plane.num])
        val eye = currentCamera - currentTarget
        val res = eye.scalarMul(normal) < 0
        //println(eye.scalarMul(normal))
        return res
    }

    private fun getLightMultiplier(plane: Plane): Double {
        val ray = currentLight.normalized()
        val normal = calcNormal(model.f[plane.num]).normalized()
        val res = ray.scalarMul(normal)
        return res
    }

    private fun fillPlane(plane: Plane){
        if (isPlaneInvisible(plane))
            return
        plane.sort()

        val lightMultiplier = (getLightMultiplier(plane) * 0.5 + 0.5).toFloat()
        val color = Color(lightMultiplier,lightMultiplier,lightMultiplier)

        canvas.fillTopTriangle(ScreenDot(plane.getCorner(0)), ScreenDot(plane.getCorner(1)), ScreenDot(plane.getCorner(2)), color)
        canvas.fillBottomTriangle(ScreenDot(plane.getCorner(0)), ScreenDot(plane.getCorner(1)), ScreenDot(plane.getCorner(2)), color)
    }
}