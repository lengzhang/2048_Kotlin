package com.lengzhang.android.lz2048

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner

class Header @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

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
        newGameButton.setOnClickListener {
            gameViewModel.newGame()
        }

        gameViewModel.step.observe(owner) {
            stepBlock.value = it
        }

        gameViewModel.score.observe(owner) {
            scoreBlock.value = it
        }
    }

    private fun createHowToPlayDialogBuilder(context: Context) {
        howToPlayDialogBuilder = AlertDialog.Builder(context)
        howToPlayDialogBuilder.setTitle(resources.getString(R.string.how_to_play_dialog_title))
        howToPlayDialogBuilder.setMessage(resources.getString(R.string.how_to_play_dialog_message))
    }
}