package com.lengzhang.android.lz2048

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import com.lengzhang.android.lz2048.database.GameStatus
import kotlin.math.max

class Header @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var gameViewModel: GameViewModel? = null

    private var stepBlock: Block
    private var scoreBlock: Block
    private var bestBlock: Block

    private var newGameButton: Button

    private var howToPlayTextView: TextView
    private lateinit var howToPlayDialogBuilder: AlertDialog.Builder

    init {
        LayoutInflater.from(context).inflate(R.layout.header, this)
        createHowToPlayDialogBuilder(context)

        stepBlock = findViewById(R.id.block_step)
        scoreBlock = findViewById(R.id.block_score)
        bestBlock = findViewById(R.id.block_best)

        newGameButton = findViewById(R.id.new_game_button)

        howToPlayTextView = findViewById(R.id.how_to_play)
        howToPlayTextView.setOnClickListener {
            howToPlayDialogBuilder.show()
        }
    }

    fun attachGameViewModel(gameViewModel: GameViewModel, owner: LifecycleOwner) {
        this.gameViewModel = gameViewModel
        gameViewModel.attachHeader(this)

        newGameButton.setOnClickListener {
            gameViewModel.newGame()
        }

        gameViewModel.currentGame.observe(owner) { game ->
            game?.apply {
                stepBlock.value = step
                scoreBlock.value = score
                bestBlock.value = max(gameViewModel.bestScore, score)

                if (status != GameStatus.PLAYING) {
                    showGameStatusDialog(status)
                }
            }
        }
    }

    fun showGameStatusDialog(status: GameStatus) {
        if (status != GameStatus.PLAYING) {
            AlertDialog.Builder(context)
                .setTitle(
                    (if (status == GameStatus.WIN) "You Win!!"
                    else "You Lose!!")
                )
                .setMessage("Please start a NEW GAME.")
                .setPositiveButton(R.string.new_game) { _, _ ->
                    this.gameViewModel?.newGame()
                }
                .show()
                .getButton(AlertDialog.BUTTON_POSITIVE).apply {
                    backgroundTintList = ColorStateList.valueOf(
                        ResourcesCompat.getColor(resources, R.color.new_game_bg, null)
                    )
                    setTextColor(Color.WHITE)

                    this.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }

        }
    }

    private fun createHowToPlayDialogBuilder(context: Context) {
        howToPlayDialogBuilder = AlertDialog.Builder(context)
        howToPlayDialogBuilder.setTitle(resources.getString(R.string.how_to_play_dialog_title))
        howToPlayDialogBuilder.setMessage(resources.getString(R.string.how_to_play_dialog_message))
    }
}