package com.lengzhang.android.lz2048

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.lengzhang.android.lz2048.gameengine.GameEngine
import com.lengzhang.android.lz2048.gameengine.Transition
import kotlin.math.abs

private const val TAG = "GameView"
private const val MIN_SWIPE_DISTANCE = 200 * 200

class GameView constructor(
    context: Context,
    private val gameViewModel: GameViewModel,
    owner: LifecycleOwner
) : View(context) {

    private var transitions: ArrayList<Transition> = ArrayList()

    private var x1 = 0f
    private var y1 = 0f

    init {
        this.post {
            Log.d(
                TAG,
                "${
                    this.width / getContext().resources.displayMetrics.density
                } ${this.height / getContext().resources.displayMetrics.density}"
            )
        }

        gameViewModel.grid.observe(owner) {
            transitions.clear()
            for (transition in it) {
                if (transition != null) transitions.add(transition)
            }
            this.refresh()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent ACTION_DOWN")
                x1 = event.x
                y1 = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "onTouchEvent ACTION_UP")
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
        Log.d(TAG, "dragging")
    }

    private fun refresh() {
        this.invalidate()
    }

}