package com.lengzhang.android.lz2048

import com.lengzhang.android.lz2048.gameengine.Transition
import com.lengzhang.android.lz2048.gameengine.TransitionTypes
import kotlin.math.abs

private const val DEFAULT_DURATION = 12L

private const val TAG = "TileUnit"

class TileUnit(transition: Transition, private val gameView: GameView) {

    data class TileState(
        val value: Int,
        val toX: Float,
        val toY: Float,
        var fromX: Float,
        var fromY: Float,
        val deltaX: Float,
        val deltaY: Float,
        var scale: Float,
        val deltaScale: Float
    )

    private val type = transition.type
    var done = false

    // Target
    var target: TileState

    // A - used for TransitionTypes.Move and TransitionTypes.Merger
    var tileA: TileState? = null

    // B - used for TransitionTypes.Merger
    var tileB: TileState? = null

    init {
        val (x, y) = getCoordinateFromPosition(transition.pos)

        when (type) {
            TransitionTypes.NEW -> {
                target = TileState(
                    value = transition.value,
                    toX = x,
                    toY = y,
                    fromX = x,
                    fromY = y,
                    deltaX = 0F,
                    deltaY = 0F,
                    scale = 0F,
                    deltaScale = 1F / DEFAULT_DURATION
                )
            }
            TransitionTypes.MERGE -> {
                target = TileState(transition.value, x, y, x, y, 0F, 0F, 0F, 1F / DEFAULT_DURATION)
                if (transition.posA != null) {
                    val (xA, yA) = getCoordinateFromPosition(transition.posA!!)
                    tileA = TileState(
                        value = transition.value / 2,
                        toX = x,
                        toY = y,
                        fromX = xA,
                        fromY = yA,
                        deltaX = abs(xA - x) / DEFAULT_DURATION,
                        deltaY = abs(yA - y) / DEFAULT_DURATION,
                        scale = 1F,
                        deltaScale = 1F / DEFAULT_DURATION
                    )
                }
                if (transition.posB != null) {
                    val (xB, yB) = getCoordinateFromPosition(transition.posB!!)
                    tileB = TileState(
                        value = transition.value / 2,
                        toX = x,
                        toY = y,
                        fromX = xB,
                        fromY = yB,
                        deltaX = abs(xB - x) / DEFAULT_DURATION,
                        deltaY = abs(yB - y) / DEFAULT_DURATION,
                        scale = 1F,
                        deltaScale = 1F / DEFAULT_DURATION
                    )
                }
            }
            else -> {
                var fromX = x
                var fromY = y
                var deltaX = 0F
                var deltaY = 0F
                if (transition.posA != null) {
                    val (xA, yA) = getCoordinateFromPosition(transition.posA!!)
                    fromX = xA
                    fromY = yA
                    deltaX = abs(fromX - x) / DEFAULT_DURATION
                    deltaY = abs(fromY - y) / DEFAULT_DURATION
                }
                target = TileState(transition.value, x, y, fromX, fromY, deltaX, deltaY, 1F, 0F)
            }
        }
        done = false
    }

    fun next() {
        if (done) return
        else {
            when (type) {
                TransitionTypes.NEW -> {
                    this.target.apply {
                        if (scale == 1F) done = true
                        else {
                            scale += deltaScale
                            if (scale > 1F) scale = 1F
                        }
                    }
                }
                TransitionTypes.MOVE -> {
                    this.target.apply {
                        if (fromX == toX && fromY == toY) done = true
                        else if (fromX != toX) {
                            fromX = moveClose(fromX, toX, deltaX)
                        } else {
                            fromY = moveClose(fromY, toY, deltaY)
                        }
                    }
                }
                TransitionTypes.MERGE -> {
                    if (this.tileA != null || this.tileB != null) {
                        this.tileA?.apply {
                            if (fromX != toX) fromX = moveClose(fromX, toX, deltaX)
                            if (fromY != toY) fromY = moveClose(fromY, toY, deltaY)
                        }
                        this.tileB?.apply {
                            if (fromX != toX) fromX = moveClose(fromX, toX, deltaX)
                            if (fromY != toY) fromY = moveClose(fromY, toY, deltaY)
                        }

                        if (
                            this.tileA != null
                            && this.tileA!!.fromX == this.tileA!!.toX
                            && this.tileA!!.fromY == this.tileA!!.toY
                            && this.tileB != null
                            && this.tileB!!.fromX == this.tileB!!.toX
                            && this.tileB!!.fromY == this.tileB!!.toY
                        ) {
                            this.tileA?.apply {
                                scale -= deltaScale
                                if (scale < 0F) scale = 0F
                            }
                            this.tileB?.apply {
                                scale -= deltaScale
                                if (scale < 0F) scale = 0F
                            }
                        }

                        if (this.tileA?.scale == 0F) this.tileA = null
                        if (this.tileB?.scale == 0F) this.tileB = null
                    } else {
                        this.target.apply {
                            if (scale == 1F) done = true
                            else {
                                scale += deltaScale
                                if (scale > 1F) scale = 1F
                            }
                        }
                    }
                }
                else -> {
                    done = true
                }
            }
        }
    }

    private fun getCoordinateFromPosition(pos: Int): Array<Float> {
        val row = pos / 4
        val col = pos % 4
        val x = gameView.marginSize * (col + 1) + gameView.tileSize * (col + 0.5F)
        val y = gameView.marginSize * (row + 1) + gameView.tileSize * (row + 0.5F)
        return arrayOf(x, y)
    }

    private fun moveClose(from: Float, to: Float, delta: Float): Float {
        var coord = from
        if (from < to) {
            coord = from + delta
            if (coord > to) coord = to
        } else if (from > to) {
            coord = from - delta
            if (coord < to) coord = to
        }
        return coord
    }
}