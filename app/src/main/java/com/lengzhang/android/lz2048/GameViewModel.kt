package com.lengzhang.android.lz2048

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lengzhang.android.lz2048.database.Game
import com.lengzhang.android.lz2048.gameengine.GameEngine
import com.lengzhang.android.lz2048.gameengine.GameEngineDelegate
import com.lengzhang.android.lz2048.gameengine.Transition
import kotlin.math.max

private const val TAG = "GameViewModel"

class GameViewModel : ViewModel(), GameEngineDelegate {
    private val gameEngine: GameEngine = GameEngine(this)
    private val gameRepository: GameRepository = GameRepository.get()

    var games: LiveData<List<Game>> = gameRepository.getGames()

    var currentGame: Game? = null
        set(value) {
            field = value
            if (value != null) {
                this.step.value = value.step
                this.score.value = value.score
                this.bestScore.value = max(this.bestScore.value ?: 0, value.score)
            }
        }

    val step: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val score: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val bestScore: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val transitions: MutableLiveData<List<Transition>> by lazy { MutableLiveData<List<Transition>>() }

    fun newGame() {
        val (grid, step, score) = this.gameEngine.newGame()
        if (currentGame != null) gameRepository.deleteGame(currentGame!!)
        @Suppress("UNCHECKED_CAST")
        if (grid is IntArray && step is Int && score is Int) {
            currentGame = Game(grid = grid as List<Int>, step = step, score = score)
        }
        gameRepository.addGame(currentGame!!)
    }

    fun loadGame(game: Game) {
        currentGame = game.apply {
            gameEngine.loadGame(grid, step, score)
        }
    }

    fun move(dir: GameEngine.Companion.Moves) {
        this.gameEngine.move(dir)
    }

    override fun applyGame(step: Int, score: Int) {
        currentGame = currentGame?.apply {
            this.step = step
            this.score = score
            this.grid = this@GameViewModel.gameEngine.getGrid()
            this@GameViewModel.gameRepository.updateGame(this)
        }

        this.transitions.value = this.gameEngine.getTransitions()

        Log.d(TAG, this.gameEngine.getGridFormatedString())
        Log.d(TAG, this.gameEngine.getGrid().toString())
        Log.d(TAG, this.gameEngine.getTransitions().toString())
    }

    override fun userWin() {
        TODO("Not yet implemented")
    }

    override fun userLose() {
        TODO("Not yet implemented")
    }

}