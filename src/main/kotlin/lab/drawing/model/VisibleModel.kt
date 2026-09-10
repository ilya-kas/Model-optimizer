package lab.drawing.model

import lab.util.ALPHA
import lab.util.AMBIENT
import lab.util.DIFFUSE
import lab.util.SPECULAR
import lab.drawing.MyCanvas
import lab.logic.OptimizerDebug
import lab.logic.PlaneAreaOptimizer
import lab.logic.PlanesAngleOptimizer
import lab.logic.currentLight
import lab.logic.entity.math.ScreenDot
import lab.logic.entity.math.Vector
import lab.logic.entity.model.Model
import lab.logic.entity.model.ScreenModel
import lab.logic.entity.model.WorldModel
import java.awt.Color

var showGrid = false
var showCornersNormals = false
var showMidNormal = false
var showNormalMap = false
var showBarCoordsMap = false
var showVertexAngleMap = false
var showPlaneAreaMap = false

class VisibleModel(private val model: Model, private val canvas: MyCanvas) {
    private var worldModel = WorldModel(model)
    private var screenModel = ScreenModel(worldModel)
    private var vertexColors: Array<Color> = emptyArray()
    private var planeColors: Array<Color> = emptyArray()
    private var vertexColorsReady = false
    private var planeColorsReady = false

    fun render(){
        worldModel = WorldModel(model)
        screenModel = ScreenModel(worldModel)
        if (showVertexAngleMap) {
            if (!vertexColorsReady) {
                vertexColors = heatColors(PlanesAngleOptimizer.debugVertices(model), highValueIsGreen = true)
                vertexColorsReady = true
            }
        } else vertexColorsReady = false
        if (showPlaneAreaMap) {
            if (!planeColorsReady) {
                planeColors = heatColors(PlaneAreaOptimizer.debugPlanes(model), highValueIsGreen = false)
                planeColorsReady = true
            }
        } else planeColorsReady = false

        canvas.clear()
        for (i in 0 until model.f.size)
            fillPlane(i)
        if (showVertexAngleMap)
            drawVertexDots()

        if (showGrid) println("сетка рёбер")
        if (showCornersNormals) println("vn в углах плоскостей")
        if (showMidNormal) println("нормаль в центре плоскости")
        if (showNormalMap) println("карта нормалей")
        if (showBarCoordsMap) println("карта координат точки в текстурных координатах")
        if (showVertexAngleMap) println("карта углов вершин (Y): зелёный — малый угол, красный — большой, синий — доп. условия")
        if (showPlaneAreaMap) println("карта площадей плоскостей (H): зелёный — малая площадь, красный — большая, синий — доп. условия")
        println("------------")
    }

    private fun heatColors(debug: Array<OptimizerDebug>, highValueIsGreen: Boolean): Array<Color> {
        val blocked = Color(0f, 0f, 1f)
        var min = Double.POSITIVE_INFINITY
        var max = Double.NEGATIVE_INFINITY
        for (item in debug) {
            if (item.extraBlocked || !item.value.isFinite()) continue
            if (item.value < min) min = item.value
            if (item.value > max) max = item.value
        }
        return Array(debug.size) { i ->
            val item = debug[i]
            if (item.extraBlocked || !item.value.isFinite()) blocked
            else {
                val t = if (!min.isFinite() || max == min) 0.5
                else (item.value - min) / (max - min)
                val redWeight = (if (highValueIsGreen) 1.0 - t else t).coerceIn(0.0, 1.0)
                Color(redWeight.toFloat(), (1.0 - redWeight).toFloat(), 0f)
            }
        }
    }

    private fun drawVertexDots(){
        for (i in screenModel.v.indices) {
            val p = screenModel.v[i]
            if (p.w <= 0.0) continue
            val color = vertexColors.getOrElse(i) { Color.BLACK }
            canvas.drawDot(p.x, p.y, p.z, color)
        }
    }

    private fun showNormals(num: Int){
        if (showCornersNormals){
            val cornerNormals = arrayOf(
                worldModel.vn[model.f[num][0].vnNum].normalized(),
                worldModel.vn[model.f[num][1].vnNum].normalized(),
                worldModel.vn[model.f[num][2].vnNum].normalized()
            )
            canvas.drawVectorW(cornerNormals[0] * (1.0 / 3.0), worldModel.v[model.f[num][0].vNum], Color.RED)
            canvas.drawVectorW(cornerNormals[1] * (1.0 / 3.0), worldModel.v[model.f[num][1].vNum], Color.RED)
            canvas.drawVectorW(cornerNormals[2] * (1.0 / 3.0), worldModel.v[model.f[num][2].vNum], Color.RED)
        }

        if (showMidNormal){
            val mid = screenModel.calcPlaneMid(num)
            val normal = worldModel.projectM(model.getDotNormal(screenModel.calcTextureCoords(num, mid.x, mid.y))).normalized()
            canvas.drawVectorW(normal * (1.0 / 3.0), worldModel.calcPlaneMid(num), Color.RED)
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
        val normal = worldModel.projectM(model.getDotNormal(textureCoords)).normalized()
        val diffuse = DIFFUSE * ray.scalarMul(normal).coerceAtLeast(0.0).toFloat()

        val reflected = ((ray - normal * (2 * ray.scalarMul(normal))) * -1.0).normalized()
        val eye = (currentCamera - currentTarget).normalized()
        val angle = reflected.scalarMul(eye).coerceAtLeast(0.0).toFloat()
        var mult = 1f
        for (i in 1 .. ALPHA)
            mult *= angle
        val specular = mult * SPECULAR

        return (ambient + diffuse + specular).coerceIn(0f, 1f)
    }

    private fun getColorByCoordsS(num: Int, x: Double, y: Double): Color{
        if (showPlaneAreaMap)
            return planeColors.getOrElse(num) { Color.BLACK }

        val textureCoords = screenModel.calcTextureCoords(num, x, y)
        val mult = getLightMultiplier(textureCoords)
        if (showNormalMap) {
            val normal = worldModel.projectM(model.getDotNormal(textureCoords)).normalized()
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
        canvas.fillTopTriangle(
                                ScreenDot(verts[0]),
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