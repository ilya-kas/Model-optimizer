package drawing.model

import AMBIENT
import DIFFUSE
import SPECULAR
import drawing.MyCanvas
import drawing.frameHeight
import drawing.frameWidth
import logic.entity.currentLight
import logic.entity.math.ScreenDot
import logic.entity.math.Vector
import logic.entity.model.Model
import logic.entity.model.ScreenModel
import logic.entity.model.WorldModel
import java.awt.Color

var showGrid = false
var showCornersNormals = false
var showMidNormal = false
var showNormalMap = false
var showBarCoordsMap = false

class VisibleModel(private val model: Model, private val canvas: MyCanvas) {
    private var worldModel = WorldModel(model)
    private var screenModel = ScreenModel(worldModel)

    fun render(){
        screenModel = ScreenModel(worldModel)

        canvas.clear()
        for (i in 0 until model.f.size)
            fillPlane(i)

        if (showGrid) println("сетка рёбер")
        if (showCornersNormals) println("vn в углах плоскостей")
        if (showMidNormal) println("нормаль в центре плоскости")
        if (showNormalMap) println("карта нормалей")
        if (showBarCoordsMap) println("карта координат точки в текстурных координатах")
        println("------------")
    }

    private fun showNormals(num: Int){
        if (showCornersNormals){
            val cornerNormals = arrayOf(
                worldModel.vn[model.f[num][0].vnNum].normalized(),
                worldModel.vn[model.f[num][1].vnNum].normalized(),
                worldModel.vn[model.f[num][2].vnNum].normalized()
            )
            canvas.drawVectorW(cornerNormals[0], worldModel.v[model.f[num][0].vNum], Color.RED)
            canvas.drawVectorW(cornerNormals[1], worldModel.v[model.f[num][1].vNum], Color.RED)
            canvas.drawVectorW(cornerNormals[2], worldModel.v[model.f[num][2].vNum], Color.RED)
        }

        if (showMidNormal){
            val mid = screenModel.calcPlaneMid(num)
            val normal = worldModel.calcDotNormal(screenModel.calcTextureCoords(num, mid.x, mid.y))
            /*if (num == 2) {
                println(normal)
                println(worldModel.calcPlaneNormal(num).normalized())
            }*/
            canvas.drawVectorW(normal, worldModel.calcPlaneMid(num), Color.RED)
        }
    }

    private fun drawGrid(num: Int){
        val c1 = screenModel.v[model.f[num][0].vNum]
        val c2 = screenModel.v[model.f[num][1].vNum]
        val c3 = screenModel.v[model.f[num][2].vNum]
        canvas.ddaLine(c1.x, c1.y, c2.x, c2.y)
        canvas.ddaLine(c1.x, c1.y, c3.x, c3.y)
        canvas.ddaLine(c2.x, c2.y, c3.x, c3.y)
    }

    private fun isPlaneInvisible(num: Int): Boolean {
        val normal = worldModel.calcPlaneNormal(num)
        val corner = worldModel.v[model.f[num][0].vNum]
        val eye = currentCamera - corner
        return eye.scalarMul(normal) <= 0
    }

    private fun getLightMultiplier(textureCoords: Vector): Float {
        val ambient = AMBIENT

        val ray = currentLight.normalized()
        val normal = worldModel.calcDotNormal(textureCoords).normalized()
        //val normal = worldModel.calcPlaneNormal(num).normalized()
        val diffuse = DIFFUSE * ray.scalarMul(normal).coerceAtLeast(0.0).toFloat()

        val reflected = ((ray - normal * (2 * ray.scalarMul(normal))) * -1.0).normalized()
        val eye = (currentCamera - currentTarget).normalized()
        val specular = reflected.scalarMul(eye).coerceAtLeast(0.0).toFloat() * SPECULAR

        return ambient + diffuse + specular
    }

    private fun getColorByCoordsS(num: Int, x: Double, y: Double): Color{
        val textureCoords = screenModel.calcTextureCoords(num, x, y)
        val mult = getLightMultiplier(textureCoords)
        if (showNormalMap) {
            val normal = model.calcDotNormal(textureCoords).normalized()
            return Color((normal.x.toFloat() + 1) / 2, (normal.y.toFloat() + 1) / 2, (normal.z.toFloat() + 1) / 2)
            //return Color(abs(normal.x.toFloat()), abs(normal.y.toFloat()), abs(normal.z.toFloat()))
        } else if (showBarCoordsMap) {
            return Color((textureCoords.x / model.textureImg.width).toFloat(), (textureCoords.y / model.textureImg.height).toFloat(), 0.5f)
        } else {
            val texturePixel = model.calcTextureColor(textureCoords)
            val lightColor = Color(mult,mult,mult)
            return Color((texturePixel.red + lightColor.red)/2, (texturePixel.green + lightColor.green)/2, (texturePixel.blue + lightColor.blue)/2)
            //return texturePixel
        }
    }

    private fun fillPlane(num: Int){
        if (isPlaneInvisible(num))
            return
        val nums = screenModel.sortVerts(num)

        val verts = arrayOf(
            screenModel.v[model.f[num][nums[0]].vNum],
            screenModel.v[model.f[num][nums[1]].vNum],
            screenModel.v[model.f[num][nums[2]].vNum]
        )
        canvas.fillTopTriangle(ScreenDot(verts[0]),
                                ScreenDot(verts[1]),
                                ScreenDot(verts[2])){ x: Double, y: Double ->
            return@fillTopTriangle getColorByCoordsS(num, x, y)
        }
        canvas.fillBottomTriangle(ScreenDot(verts[0]),
                                    ScreenDot(verts[1]),
                                    ScreenDot(verts[2])){ x: Double, y: Double ->
            return@fillBottomTriangle getColorByCoordsS(num, x, y)
        }

        showNormals(num)
        if (showGrid)
            drawGrid(num)
    }
}