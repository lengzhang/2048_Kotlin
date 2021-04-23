package com.lengzhang.android.lz2048

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lengzhang.android.lz2048.gameengine.GameEngine
import com.lengzhang.android.lz2048.gameengine.GameEngineDelegate
import com.lengzhang.android.lz2048.gameengine.Transition

class GameViewModel : ViewModel(), GameEngineDelegate {
    private var gameEngine: GameEngine = GameEngine(this)

    val step: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val score: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val bestScore: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val grid: MutableLiveData<ArrayList<Transition?>> by lazy { MutableLiveData<ArrayList<Transition?>>() }


    init {
        newGame()

    }

    fun newGame() {
        this.gameEngine.newGame()
    }

    override fun applyGame(grid: ArrayList<Transition?>?, step: Int?, score: Int?) {
        if (step != null) this.step.value = step
        if (score != null) this.score.value = score
        if (grid != null) this.grid.value = grid

        this.gameEngine.printGrid()
    }

    override fun userWin() {
        TODO("Not yet implemented")
    }

    override fun userLose() {
        TODO("Not yet implemented")
    }

}