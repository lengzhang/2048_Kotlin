package com.lengzhang.android.lz2048

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.util.TypedValue
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
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
    private val gameViewModel: GameViewModel
) :
    SurfaceView(context), SurfaceHolder.Callback {

    private val gameThread: GameThread

    private var x1 = 0F
    private var y1 = 0F

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

    var isDrawing = false
    var boardSize = 0F
    var tileSize = 0F
    var density = 0F
    var rectRadius = 0F
    var marginSize = 0F

    init {
        holder.addCallback((this))
        holder.setFormat(PixelFormat.TRANSLUCENT)
        gameThread = GameThread(holder, this)
        this.post {
            this.boardSize = min(this.width, this.height).toFloat()
            this.density = getContext().resources.displayMetrics.density
            this.marginSize = 10 * this.density
            this.tileSize = (this.boardSize - 5 * this.marginSize) / 4
            this.rectRadius = 4 * this.density
            isDrawing = true

            gameViewModel.transitions.observe(context as LifecycleOwner) { transitions ->
                updateTileUnits(transitions)
            }

            gameThread.start()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread.setRunning(true)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        while (retry) {
            try {
                gameThread.setRunning(false)
                gameThread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            retry = false
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isDrawing) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    x1 = event.x
                    y1 = event.y
                    performClick()
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
                }
            }
        }
        return true
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (canvas == null) return
        drawBackgroundBoard(canvas)
        isDrawing = !drawTiles(canvas)
    }

    private fun updateTileUnits(transitions: List<Transition>) {
        this.tileUnits.clear()
        transitions.forEach { transition ->
            when (transition.type) {
                else -> {
                    this.tileUnits.add(TileUnit(transition, this@GameView))
                }
            }
        }
        isDrawing = true
    }

    private fun drawBackgroundBoard(canvas: Canvas) {
        canvas.drawColor(getColor(resources, R.color.bg, null))

        val paint = Paint()
        paint.color = getColor(resources, R.color.board_bg, null)
        canvas.drawRoundRect(
            0F,
            0F,
            this@GameView.boardSize,
            this@GameView.boardSize,
            this@GameView.rectRadius,
            this@GameView.rectRadius,
            paint
        )
        paint.color = getColor(resources, R.color.empty_bg, null)

        for (i in 0 until 4) {
            for (j in 0 until 4) {
                val dx = this@GameView.marginSize * (j + 1) + this@GameView.tileSize * j
                val dy = this@GameView.marginSize * (i + 1) + this@GameView.tileSize * i
                canvas.drawRoundRect(
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
    }

    private fun drawTiles(canvas: Canvas): Boolean {
        var isDone = true

        this.tileUnits.forEach { tileUnit ->
            // Draw Target
            if (tileUnit.target.scale > 0F) drawTile(canvas, tileUnit.target)
            // Draw Tile A
            if (tileUnit.tileA != null) drawTile(canvas, tileUnit.tileA!!)
            // Draw Tile B
            if (tileUnit.tileB != null) drawTile(canvas, tileUnit.tileB!!)

            if (!tileUnit.done) {
                isDone = false
                tileUnit.next()
            }
        }


        return isDone
    }

    private fun drawTile(canvas: Canvas, tileState: TileUnit.TileState) {
        canvas.apply {
            save()
            translate(tileState.fromX, tileState.fromY)

            // Draw Cell
            val cellPaint = Paint()
            cellPaint.color = colorMapper[tileState.value]?.backgroundColor ?: 0
            drawRoundRect(
                -(tileSize * tileState.scale) / 2,
                -(tileSize * tileState.scale) / 2,
                (tileSize * tileState.scale) / 2,
                (tileSize * tileState.scale) / 2,
                rectRadius,
                rectRadius,
                cellPaint
            )

            // Draw Text
            val textPaint = Paint()
            textPaint.color = colorMapper[tileState.value]?.textColor ?: 0

            textPaint.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                30F * tileState.scale,
                resources.displayMetrics
            )
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.DEFAULT_BOLD
            val top = textPaint.fontMetrics.top
            val bottom = textPaint.fontMetrics.bottom
            drawText(
                tileState.value.toString(),
                0F,
                -(top + bottom) / 2,
                textPaint
            )
            restore()
        }
    }
}