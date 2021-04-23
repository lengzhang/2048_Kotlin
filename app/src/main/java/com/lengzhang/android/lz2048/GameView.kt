package com.lengzhang.android.lz2048

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.lengzhang.android.lz2048.gameengine.GameEngine
import com.lengzhang.android.lz2048.gameengine.GameEngineConstants
import com.lengzhang.android.lz2048.gameengine.Transition
import kotlin.math.abs

private const val TAG = "GameView"
private const val MIN_SWIPE_DISTANCE = 200 * 200

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var gameViewModel: GameViewModel
    private lateinit var gameBoard: FrameLayout

    private var x1 = 0f
    private var y1 = 0f

    init {
        LayoutInflater.from(context).inflate(R.layout.game_view, this)
        gameBoard = this.findViewById(R.id.game_board)
        this.post {
            Log.d(
                TAG,
                "${
                    gameBoard.width / getContext().resources.displayMetrics.density
                } ${gameBoard.height / getContext().resources.displayMetrics.density}"
            )
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


    fun attachGameViewModel(gameViewModel: GameViewModel, owner: LifecycleOwner) {
        this.gameViewModel = gameViewModel

        this.gameViewModel.grid.observe(owner) { draw(it) }
    }

    private fun draw(grid: ArrayList<Transition?>) {


        var str = "---------\n"
        for (i in 0 until GameEngineConstants.ROW_COUNT) {
            for (j in 0 until GameEngineConstants.COLUMN_COUNT) {
                str += "| " + (grid?.get(i * GameEngineConstants.COLUMN_COUNT + j)?.value.toString()) + " "
            }
            str += "|\n"
        }
        str += "---------"

        val textView = TextView(this.context)
        textView.text = str
        gameBoard.removeAllViews()
        gameBoard.addView(textView)
    }

}