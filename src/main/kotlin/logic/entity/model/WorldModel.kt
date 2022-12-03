package logic.entity.model

import logic.entity.WorldManager
import logic.entity.math.Vector

class WorldModel(model: Model): Model() {
    private var worldMatrix = WorldManager.getWorldMatrix()

    init {
        for (vert in model.v)
            v += vert * worldMatrix

        for (t in model.vt)
            vt += t

        for (vert in model.vn)
            vn += vert * worldMatrix

        for (_f in model.f)
            f += _f
    }

    fun projectM(v: Vector): Vector = v * worldMatrix
}