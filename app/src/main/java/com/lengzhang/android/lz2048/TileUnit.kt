package com.lengzhang.android.lz2048

import android.animation.ValueAnimator
import android.view.animation.OvershootInterpolator

private const val DEFAULT_DURATION = 1000L

class TileUnit(private val gameView: GameView) {

    var x = 0F
    var y = 0F

    var row = 0
    var col = 0
    var value = 0

    var size = 0F

    fun setData(pos: Int, value: Int) {
        this.value = value
        this.row = pos / 4
        this.col = pos % 4
        this.x = gameView.marginSize * (this.col + 1) + gameView.tileSize * (this.col + 0.5F)
        this.y = gameView.marginSize * (this.row + 1) + gameView.tileSize * (this.row + 0.5F)
    }

    fun enterAnimation(duration: Long = DEFAULT_DURATION) {
        val animate = ValueAnimator.ofFloat(0F, gameView.tileSize).apply {
            this.duration = duration
            interpolator = OvershootInterpolator()
            addUpdateListener {
                size = animatedValue as Float
                if (size < gameView.tileSize) gameView.refresh()
            }
        }
        animate.start()
    }

    fun moveToByRow(toRow: Int) {
        val toY = gameView.marginSize * (toRow + 1) + gameView.tileSize * (toRow + 0.5F)
        val animate = ValueAnimator.ofFloat(y, toY).apply {
            duration = DEFAULT_DURATION
            interpolator = OvershootInterpolator()
            addUpdateListener {
                y = animatedValue as Float
                gameView.refresh()
            }
        }
        animate.start()
    }

    fun moveToByCol(toCol: Int) {
        val toX = gameView.marginSize * (toCol + 1) + gameView.tileSize * (toCol + 0.5F)
        val animate = ValueAnimator.ofFloat(x, toX).apply {
            duration = DEFAULT_DURATION
            interpolator = OvershootInterpolator()
            addUpdateListener {
                x = animatedValue as Float
                gameView.refresh()
            }
        }
        animate.start()
    }
}