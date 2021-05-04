package com.lengzhang.android.lz2048

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lengzhang.android.lz2048.database.Game
import com.lengzhang.android.lz2048.database.GameStatus
import com.lengzhang.android.lz2048.gameengine.GameEngine
import com.lengzhang.android.lz2048.gameengine.GameEngineDelegate
import com.lengzhang.android.lz2048.gameengine.Transition
import java.util.*
import kotlin.math.max

private const val TAG = "GameViewModel"

class GameViewModel : ViewModel(), GameEngineDelegate {
    private val gameEngine: GameEngine = GameEngine(this)
    private val gameRepository: GameRepository = GameRepository.get()
    private var header: Header? = null

    var games: LiveData<List<Game>> = gameRepository.getGames()

    val currentGame: MutableLiveData<Game?> by lazy { MutableLiveData<Game?>() }

    var bestScore: Int = 0

    val transitions: MutableLiveData<List<Transition>> by lazy { MutableLiveData<List<Transition>>() }


    fun attachHeader(header: Header) {
        this.header = header
    }

    fun newGame() {
        if (currentGame.value != null) {
            if (currentGame.value!!.status == GameStatus.PLAYING) {
                gameRepository.deleteGame(currentGame.value!!)
            }
            currentGame.value = null
        }
        this.gameEngine.newGame()
    }

    fun loadGame(game: Game) {
        currentGame.value = game
        game.apply {
            gameEngine.loadGame(grid, step, score)
        }
    }

    fun setBestScore(games: List<Game>) {
        var bestScore = 0
        games.forEach { game -> bestScore = max(bestScore, game.score) }
        this.bestScore = bestScore
    }

    fun move(dir: GameEngine.Companion.Moves) {
        if (currentGame.value != null) {
            if (currentGame.value!!.status == GameStatus.PLAYING) {
                this.gameEngine.move(dir)
            } else if (this.header != null) {
                this.header!!.showGameStatusDialog(this.currentGame.value!!.status)
            }
        }
    }

    override fun applyGame(step: Int, score: Int) {
        if (currentGame.value == null) {
            val game = Game(
                step = step,
                score = score,
                grid = this@GameViewModel.gameEngine.getGrid()
            )
            this.gameRepository.addGame(game)
            currentGame.value = game
            games = gameRepository.getGames()
        } else {
            currentGame.value = currentGame.value!!.apply {
                this.step = step
                this.score = score
                this.grid = this@GameViewModel.gameEngine.getGrid()
                this.updatedAt = Date()
                this@GameViewModel.gameRepository.updateGame(this)
            }
        }

        this.transitions.value = this.gameEngine.getTransitions()

        Log.d(TAG, this.gameEngine.getGridFormattedString())
        Log.d(TAG, this.gameEngine.getGrid().toString())
        Log.d(TAG, this.gameEngine.getTransitions().toString())
    }

    override fun userWin() {
        currentGame.value = currentGame.value?.apply {
            this.status = GameStatus.WIN
            this@GameViewModel.gameRepository.updateGame(this)
        }
    }

    override fun userLose() {
        currentGame.value = currentGame.value?.apply {
            this.status = GameStatus.LOSE
            this@GameViewModel.gameRepository.updateGame(this)
        }
    }

}

//4,4,64,128,128,64,128,64,64,128,64,128,128,64,128,64