package lab.logic.entity.model

import lab.logic.WorldManager
import lab.logic.entity.math.Vector

class WorldModel(model: Model): Model() {
    private var worldMatrix = WorldManager.getWorldMatrix()

    init {
        for (vert in model.v)
            v += vert * worldMatrix

        for (t in model.vt)
            vt += t

        for (vert in model.vn)
            vn += Vector(vert.x, vert.y, vert.z, 0.0) * worldMatrix

        for (_f in model.f)
            f += _f

        textureImg = model.textureImg
        normalImg = model.normalImg
        mirrorImg = model.mirrorImg
    }

    fun projectM(v: Vector): Vector = Vector(v.x, v.y, v.z, 0.0) * worldMatrix
}