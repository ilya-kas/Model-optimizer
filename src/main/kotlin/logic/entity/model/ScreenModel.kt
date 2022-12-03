package logic.entity.model

import drawing.frameHeight
import drawing.frameWidth
import drawing.model.Camera
import logic.entity.math.Matrix
import logic.entity.math.Vector
import kotlin.math.abs

class ScreenModel(model: WorldModel): Model() {
    private val cameraMatrix = Camera.getCameraMatrix()
    private var viewportMatrix = Matrix.getViewportMatrix(frameWidth.toDouble(), frameHeight.toDouble())

    init {
        for (vert in model.v) {
            var newbie = vert * cameraMatrix
            val w = newbie.w
            newbie /= newbie.w
            newbie *= viewportMatrix
            newbie.w = w
            v += newbie
        }

        for (t in model.vt)
            vt += t

        for (norm in model.vn){
            var newbie = norm * cameraMatrix
            newbie /= newbie.w
            newbie *= viewportMatrix
            vn += newbie
        }

        for (_f in model.f)
            f += _f

        textureImg = model.textureImg
        normalImg = model.normalImg
        mirrorImg = model.mirrorImg
    }

    fun projectW(v: Vector): Vector{
        var newbie = v * cameraMatrix
        newbie /= newbie.w
        newbie *= viewportMatrix
        return newbie
    }

    fun calcTextureCoords(num: Int, x: Double, y: Double): Vector{
        val barCoords = calcBarycentricCoords(num, x, y)
        val corn = arrayOf(
            f[num][0],
            f[num][1],
            f[num][2]
        )
        val resx = vt[corn[0].vtNum].x/v[corn[0].vNum].w * barCoords.x +
                vt[corn[1].vtNum].x/v[corn[1].vNum].w * barCoords.y +
                vt[corn[2].vtNum].x/v[corn[2].vNum].w * barCoords.z
        val resy = vt[corn[0].vtNum].y/v[corn[0].vNum].w * barCoords.x +
                vt[corn[1].vtNum].y/v[corn[1].vNum].w * barCoords.y +
                vt[corn[2].vtNum].y/v[corn[2].vNum].w * barCoords.z
        val resx1 = 1/v[corn[0].vNum].w * barCoords.x + 1/v[corn[1].vNum].w * barCoords.y + 1/v[corn[2].vNum].w * barCoords.z
        val resy1 = 1/v[corn[0].vNum].w * barCoords.x + 1/v[corn[1].vNum].w * barCoords.y + 1/v[corn[2].vNum].w * barCoords.z
        return Vector(resx/resx1 * (textureImg.width-1), (1-resy/resy1) * (textureImg.height-1))
    }

    private fun calcBarycentricCoords(num: Int, x: Double, y: Double): Vector{
        val verts = arrayOf(
            v[f[num][0].vNum],
            v[f[num][1].vNum],
            v[f[num][2].vNum]
        )
        val res = Vector()

        // alpha0
        var xl = verts[1].x - x
        var xr = verts[2].x - x
        var yl = verts[1].y - y
        var yr = verts[2].y - y
        val area0 = abs(xl * yr - yl * xr)

        // alpha1
        xl = verts[2].x - x
        xr = verts[0].x - x
        yl = verts[2].y - y
        yr = verts[0].y - y
        val area1 = abs(xl * yr - yl * xr)

        // alpha2
        xl = verts[0].x - x
        xr = verts[1].x - x
        yl = verts[0].y - y
        yr = verts[1].y - y
        val area2 = abs(xl * yr - yl * xr)

        val totalArea = area0 + area1 + area2
        val alpha0 = area0 / totalArea
        res.x = alpha0
        val alpha1 = area1 / totalArea
        res.y = alpha1
        val alpha2 = area2 / totalArea
        res.z = alpha2

        return res
    }
}