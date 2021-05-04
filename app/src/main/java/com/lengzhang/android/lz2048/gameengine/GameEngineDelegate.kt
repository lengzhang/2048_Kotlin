package com.lengzhang.android.lz2048.gameengine

interface GameEngineDelegate {
    fun applyGame(step: Int, score: Int)
    fun userWin()
    fun userLose()
}