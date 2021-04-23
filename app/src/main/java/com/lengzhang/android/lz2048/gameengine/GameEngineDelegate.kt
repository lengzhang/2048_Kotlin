package com.lengzhang.android.lz2048.gameengine

interface GameEngineDelegate {
    fun applyGame(grid: ArrayList<Transition?>? = null, step: Int? = null, score: Int? = null)
    fun userWin()
    fun userLose()
}