package com.lengzhang.android.lz2048

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lengzhang.android.lz2048.gameengine.GameEngine
import com.lengzhang.android.lz2048.gameengine.GameEngineDelegate
import com.lengzhang.android.lz2048.gameengine.Transition

private const val TAG = "GameViewModel"

class GameViewModel : ViewModel(), GameEngineDelegate {
    private var gameEngine: GameEngine = GameEngine(this)

    val step: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val score: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val bestScore: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val grid: MutableLiveData<ArrayList<Transition?>> by lazy { MutableLiveData<ArrayList<Transition?>>() }

    val transitions: MutableLiveData<List<Transition>> by lazy { MutableLiveData<List<Transition>>() }


    init {
        newGame()
    }

    fun newGame() {
        this.gameEngine.newGame()
    }

    fun move(dir: GameEngine.Companion.Moves) {
        this.gameEngine.move(dir)
    }

    override fun applyGame(step: Int, score: Int) {
        this.step.value = step
        this.score.value = score


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