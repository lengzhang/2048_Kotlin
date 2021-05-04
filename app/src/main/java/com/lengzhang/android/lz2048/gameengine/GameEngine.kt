package com.lengzhang.android.lz2048.gameengine

import android.util.Log
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

private const val TAG = "GameEngine"

class GameEngine(private val delegated: GameEngineDelegate) {

    private val random = Random()

    private var transitions: ArrayList<Transition?> =
        Array<Transition?>(GameEngineConstants.TILE_COUNT) { null }.toCollection(ArrayList())

    private var step = 0
    private var score = 0
    private var emptyCount = GameEngineConstants.TILE_COUNT
    private var maxTile = GameEngineConstants.BLANK_VALUE

    fun newGame(): Array<Any> {
        this.step = 0
        this.score = 0
        this.emptyCount = GameEngineConstants.TILE_COUNT
        this.maxTile = GameEngineConstants.BLANK_VALUE

        this.transitions =
            Array<Transition?>(GameEngineConstants.TILE_COUNT) { null }.toCollection(ArrayList())

        addTile()
        addTile()

        this.delegated.applyGame(this.step, this.score)
        return arrayOf(this.getGrid(), this.step, this.score)
    }

    fun loadGame(grid: List<Int>, step: Int, score: Int) {
        Log.d(TAG, "loadGame")
        this.step = step
        this.score = score
        this.emptyCount = GameEngineConstants.TILE_COUNT
        this.maxTile = GameEngineConstants.BLANK_VALUE

        this.transitions =
            Array<Transition?>(GameEngineConstants.TILE_COUNT) { null }.toCollection(ArrayList())
        var i = 0
        while (i < transitions.size && i < grid.size) {
            if (grid[i] > 0) {
                this.transitions[i] = Transition(TransitionTypes.NEW, grid[i], i)
                this.emptyCount--
                this.maxTile = max(this.maxTile, grid[i])
            }
            i++
        }

        this.delegated.applyGame(this.step, this.score)
    }

    fun move(dir: Moves) {
        Log.d(TAG, dir.toString())
        val changed = when (dir) {
            Moves.UP -> moveUp()
            Moves.RIGHT -> moveRight()
            Moves.DOWN -> moveDown()
            Moves.LEFT -> moveLeft()
        }

        if (changed) {
            addTile()
            Log.d(TAG, this.transitions.toString())
            Log.d(TAG, "${this.emptyCount} ${this.maxTile}")
            this.step++
            this.delegated.applyGame(this.step, this.score)
        }

        if (this.maxTile == GameEngineConstants.TARGET_VALUE) this.delegated.userWin()
        else if (!isMovable()) this.delegated.userLose()
    }

    private fun addTile(seedValue: Int? = -1): Boolean {
        if (emptyCount <= 0) return false

        val value = when (seedValue) {
            2 -> 2
            4 -> 4
            else -> {
                if (random.nextInt(100) > GameEngineConstants.RANDOM_RATIO) 4
                else 2
            }
        }

        val pos = random.nextInt(emptyCount)

        var blankCount = 0
        for (i in 0 until GameEngineConstants.TILE_COUNT) {
            if (transitions[i] == null) {
                if (blankCount == pos) {
                    transitions[i] = Transition(TransitionTypes.NEW, value, i)
                    score += value
                    emptyCount--
                    maxTile = max(maxTile, value)
                    return true
                }
                blankCount++
            }
        }
        return false
    }

    private fun moveUp(): Boolean {
        val a = moveTiles(0, 4, 8, 12)
        val b = moveTiles(1, 5, 9, 13)
        val c = moveTiles(2, 6, 10, 14)
        val d = moveTiles(3, 7, 11, 15)
        return (a || b || c || d)
    }

    private fun moveRight(): Boolean {
        val a = moveTiles(3, 2, 1, 0)
        val b = moveTiles(7, 6, 5, 4)
        val c = moveTiles(11, 10, 9, 8)
        val d = moveTiles(15, 14, 13, 12)
        return (a || b || c || d)
    }

    private fun moveDown(): Boolean {
        val a = moveTiles(12, 8, 4, 0)
        val b = moveTiles(13, 9, 5, 1)
        val c = moveTiles(14, 10, 6, 2)
        val d = moveTiles(15, 11, 7, 3)
        return (a || b || c || d)
    }

    private fun moveLeft(): Boolean {
        val a = moveTiles(0, 1, 2, 3)
        val b = moveTiles(4, 5, 6, 7)
        val c = moveTiles(8, 9, 10, 11)
        val d = moveTiles(12, 13, 14, 15)
        return (a || b || c || d)
    }

    private fun moveTiles(vararg indexes: Int): Boolean {
        var changed = false

        val indexArr = intArrayOf(*indexes)
        val tmpArr = ArrayList<Transition>()
        // Move
        for (index in indexArr) {
            val transition = this.transitions[index] ?: continue
            tmpArr.add(Transition(TransitionTypes.NONE, transition.value, -1, index))
        }

        // Merge
        var j = 1
        while (j < tmpArr.size) {
            if (tmpArr[j - 1].value == tmpArr[j].value) {
                tmpArr[j - 1].value *= 2
                tmpArr[j - 1].posB = tmpArr[j].posA
                tmpArr.removeAt(j)
            }
            j++
        }

        // Update Grid
        Log.d(TAG, tmpArr.toString())
        for ((i, targetIndex) in indexArr.withIndex()) {
            if (i >= tmpArr.size) {
                this.transitions[targetIndex] = null
                continue
            }
            val transition = tmpArr[i].apply {
                type = when {
                    posB != null -> TransitionTypes.MERGE
                    posA == targetIndex -> TransitionTypes.NONE
                    else -> TransitionTypes.MOVE
                }
                pos = targetIndex
            }
            if (transition.type == TransitionTypes.MERGE) {
                this.score += transition.value
                emptyCount++
                maxTile = max(maxTile, transition.value)
            }
            if (transition.type != TransitionTypes.NONE) changed = true
            this.transitions[targetIndex] = transition
        }
        return changed
    }

    private fun isMovable(): Boolean {
        if (this.emptyCount > 0) return true

        /* Check follow tiles
        | x | x | x |  |
        | x | x | x |  |
        | x | x | x |  |
        | x | x | x |  |
         */
        for (i in 0 until GameEngineConstants.ROW_COUNT) {
            for (j in 0 until GameEngineConstants.COLUMN_COUNT - 1) {
                val index = i * GameEngineConstants.COLUMN_COUNT + j
                if (transitions[index]?.value ?: -1 == transitions[index + 1]?.value ?: -2) return true
                if (i == GameEngineConstants.ROW_COUNT - 1) continue
                if (transitions[index]?.value ?: -1 == transitions[index + GameEngineConstants.COLUMN_COUNT]?.value ?: -2) return true
            }
        }

        return false
    }

    fun getGridFormattedString(): String {
        var str = "----------\n---------\n"
        for (i in 0 until GameEngineConstants.ROW_COUNT) {
            for (j in 0 until GameEngineConstants.COLUMN_COUNT) {
                str += "| " + (this.transitions[i * GameEngineConstants.COLUMN_COUNT + j]?.value.toString()) + " "
            }
            str += "|\n"
        }
        str += "---------"
        return str
    }

    fun getGrid(): List<Int> {
        return this.transitions.map {
            it?.value ?: GameEngineConstants.BLANK_VALUE
        }
    }

    fun getTransitions(): List<Transition> {
        return this.transitions.filterNotNull()
    }


    companion object {
        enum class Moves { UP, RIGHT, DOWN, LEFT }
    }
}