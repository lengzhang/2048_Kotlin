package com.lengzhang.android.lz2048

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.lifecycle.LifecycleOwner
import com.lengzhang.android.lz2048.gameengine.GameEngine
import com.lengzhang.android.lz2048.gameengine.Transition
import com.lengzhang.android.lz2048.gameengine.TransitionTypes
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
    private var tileUnits: ArrayList<TileUnit> = ArrayList()

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
    var boardSize = 0F
    var tileSize = 0F
    var density = 0F
    var rectRadius = 0F
    var marginSize = 0F

    init {
        isSizeReady = false
        this.post {
            this.boardSize = min(this.width, this.height).toFloat()
            this.density = getContext().resources.displayMetrics.density
            this.marginSize = 10 * this.density
            this.tileSize = (this.boardSize - 5 * this.marginSize) / 4
            this.rectRadius = 4 * this.density
            isSizeReady = true
            updateTileUnits()
        }

        gameViewModel.transitions.observe(owner) { transitions ->
            this.transitions = transitions

            if (!isSizeReady) return@observe
            updateTileUnits()
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
        super.onDraw(canvas)
        canvas?.apply {
            drawBackgroundBoard(canvas)
            drawTransitions(canvas)
        }
    }

    private fun updateTileUnits(transitions: List<Transition> = this.transitions) {
        this.tileUnits.clear()
        transitions.forEach { transition ->
            when (transition.type) {
                TransitionTypes.MOVE -> {
                    val tileUnit = TileUnit(this)
                    tileUnit.setData(transition.posA ?: transition.pos, transition.value)
                    val toRow = transition.pos / 4
                    val toCol = transition.pos % 4

                    if (toRow == tileUnit.row) tileUnit.moveToByCol(toCol)
                    else tileUnit.moveToByRow(toRow)

                    tileUnits.add(tileUnit)
                }
//                TransitionTypes.MERGE -> {
//
//                }
                else -> {
                    val tileUnit = TileUnit(this)
                    tileUnit.setData(transition.pos, transition.value)
                    tileUnit.enterAnimation(
                        if (transition.type == TransitionTypes.NEW) 1000L
                        else 0L
                    )
                    tileUnits.add(tileUnit)
                }
            }

        }
        this.refresh()
    }


    fun refresh() {
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
        Log.d(TAG, "drawTransitions")
        this.tileUnits.forEach { tileUnit ->
            Log.d(TAG, tileUnit.value.toString())
            canvas.apply {
                save()

                translate(tileUnit.x, tileUnit.y)
                // Draw Cell
                val cellPaint = Paint()
                cellPaint.color = colorMapper[tileUnit.value]?.backgroundColor ?: 0
                drawRoundRect(
                    -tileUnit.size / 2,
                    -tileUnit.size / 2,
                    tileUnit.size / 2,
                    tileUnit.size / 2,
                    rectRadius,
                    rectRadius,
                    cellPaint
                )

                // Draw Text
                val textPaint = Paint()
                textPaint.color = colorMapper[tileUnit.value]?.textColor ?: 0

                textPaint.textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    30F,
                    resources.displayMetrics
                )
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = Typeface.DEFAULT_BOLD
                val top = textPaint.fontMetrics.top
                val bottom = textPaint.fontMetrics.bottom
                drawText(
                    tileUnit.value.toString(),
                    0F,
                    -(top + bottom) / 2,
                    textPaint
                )

                restore()
            }
        }

    }
}