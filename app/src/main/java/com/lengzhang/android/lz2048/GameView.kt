package com.lengzhang.android.lz2048

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.lifecycle.LifecycleOwner
import com.lengzhang.android.lz2048.gameengine.GameEngine
import com.lengzhang.android.lz2048.gameengine.Transition
import kotlin.math.abs
import kotlin.math.min

private const val TAG = "GameView"
private const val MIN_SWIPE_DISTANCE = 200 * 200

private class TileColor(val backgroundColor: Int, val textColor: Int)

class GameView constructor(
    context: Context,
    private val gameViewModel: GameViewModel,
    owner: LifecycleOwner
) : View(context) {

    private var x1 = 0F
    private var y1 = 0F

    private var transitions: List<Transition> = ArrayList()

    private val colorMapper: Map<Int, TileColor> = mapOf(
        2 to TileColor(
            getColor(resources, R.color.t2_bg, null),
            getColor(resources, R.color.t2_tx, null)
        ),
        4 to TileColor(
            getColor(resources, R.color.t4_bg, null),
            getColor(resources, R.color.t4_tx, null)
        ),
        8 to TileColor(
            getColor(resources, R.color.t8_bg, null),
            getColor(resources, R.color.t8_tx, null)
        ),
        16 to TileColor(
            getColor(resources, R.color.t16_bg, null),
            getColor(resources, R.color.t16_tx, null)
        ),
        32 to TileColor(
            getColor(resources, R.color.t32_bg, null),
            getColor(resources, R.color.t32_tx, null)
        ),
        64 to TileColor(
            getColor(resources, R.color.t64_bg, null),
            getColor(resources, R.color.t64_tx, null)
        ),
        128 to TileColor(
            getColor(resources, R.color.t128_bg, null),
            getColor(resources, R.color.t128_tx, null)
        ),
        256 to TileColor(
            getColor(resources, R.color.t256_bg, null),
            getColor(resources, R.color.t256_tx, null)
        ),
        512 to TileColor(
            getColor(resources, R.color.t512_bg, null),
            getColor(resources, R.color.t512_tx, null)
        ),
        1024 to TileColor(
            getColor(resources, R.color.t1024_bg, null),
            getColor(resources, R.color.t1024_tx, null)
        ),
        2048 to TileColor(
            getColor(resources, R.color.t2048_bg, null),
            getColor(resources, R.color.t2048_tx, null)
        ),
    )

    private var isSizeReady = false
    private var boardSize = 0F
    private var tileSize = 0F
    private var density = 0F
    private var rectRadius = 0F
    private var marginSize = 0F

    init {
        isSizeReady = false
        this.post {
            this.boardSize = min(this.width, this.height).toFloat()
            this.density = getContext().resources.displayMetrics.density
            this.marginSize = 10 * this.density
            this.tileSize = (this.boardSize - 5 * this.marginSize) / 4
            this.rectRadius = 4 * this.density
            isSizeReady = true
            this.refresh()
        }

        gameViewModel.transitions.observe(owner) {
            transitions = it
            if (isSizeReady) this.refresh()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val diffX = event.x - x1
                val diffY = event.y - y1
                if (diffX * diffX + diffY * diffY > MIN_SWIPE_DISTANCE) {
                    gameViewModel.move(
                        if (abs(diffX) > abs(diffY)) {
                            if (diffX > 0) GameEngine.Companion.Moves.RIGHT
                            else GameEngine.Companion.Moves.LEFT
                        } else {
                            if (diffY > 0) GameEngine.Companion.Moves.DOWN
                            else GameEngine.Companion.Moves.UP
                        }
                    )
                }
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            drawBackgroundBoard(canvas)
            drawTransitions(canvas)
        }
        super.onDraw(canvas)
        Log.d(TAG, "dragging")
    }


    private fun refresh() {
        this.invalidate()
    }

    private fun drawBackgroundBoard(canvas: Canvas) {
        val paint = Paint()
        canvas.apply {
            save()
            paint.color = getColor(resources, R.color.bg, null)
            drawRoundRect(
                0F,
                0F,
                this@GameView.boardSize,
                this@GameView.boardSize,
                this@GameView.rectRadius,
                this@GameView.rectRadius,
                paint
            )

            paint.color = getColor(resources, R.color.empt_bg, null)
            for (i in 0 until 4) {
                for (j in 0 until 4) {
                    val dx = this@GameView.marginSize * (j + 1) + this@GameView.tileSize * j
                    val dy = this@GameView.marginSize * (i + 1) + this@GameView.tileSize * i
                    drawRoundRect(
                        dx,
                        dy,
                        dx + this@GameView.tileSize,
                        dy + this@GameView.tileSize,
                        this@GameView.rectRadius,
                        this@GameView.rectRadius,
                        paint
                    )
                }
            }
            restore()
        }
    }

    private fun drawTransitions(canvas: Canvas) {
        val paint = Paint()
        val halfSize = this.tileSize / 2
        transitions.forEach { transition ->
            canvas.apply {
                save()


                val row = transition.pos / 4
                val col = transition.pos % 4
                val x = this@GameView.marginSize * (col + 1) + this@GameView.tileSize * (col + 0.5F)
                val y = this@GameView.marginSize * (row + 1) + this@GameView.tileSize * (row + 0.5F)

                translate(x, y)
                // Draw Cell
                paint.color = colorMapper[transition.value]?.backgroundColor ?: 0
                drawRoundRect(
                    -halfSize,
                    -halfSize,
                    halfSize,
                    halfSize,
                    rectRadius,
                    rectRadius,
                    paint
                )

                // Draw Text
                paint.color = colorMapper[transition.value]?.textColor ?: 0

                paint.textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    30F,
                    resources.displayMetrics
                )
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.DEFAULT_BOLD
                val top = paint.fontMetrics.top
                val bottom = paint.fontMetrics.bottom
                drawText(transition.value.toString(), 0F, -(top + bottom) / 2, paint)

                restore()
            }
        }
    }
}