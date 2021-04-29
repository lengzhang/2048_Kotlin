package com.lengzhang.android.lz2048.gameengine

import android.util.Log
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

private const val TAG = "GameEngine"

class GameEngine(private val delegated: GameEngineDelegate) {

    private val random = Random()

    private var grid: ArrayList<Transition?> =
        Array<Transition?>(GameEngineConstants.TILE_COUNT) { null }.toCollection(ArrayList())

    private var step = 0
    private var score = 0
    private var emptyCount = GameEngineConstants.TILE_COUNT
    private var maxTile = GameEngineConstants.BLANK_VALUE

    fun newGame() {
        initGrid()
        this.step = 0
        this.score = 0
        this.emptyCount = GameEngineConstants.TILE_COUNT
        this.maxTile = GameEngineConstants.BLANK_VALUE

        addTile(2)
        addTile(2)

        this.delegated.applyGame(this.grid, this.step, this.score)
    }

    fun move(dir: Moves) {
        var tmpScore = this.score

        val changed = when (dir) {
            Moves.UP -> moveUp()
            Moves.RIGHT -> moveRight()
            Moves.DOWN -> moveDown()
            Moves.LEFT -> moveLeft()
        }

        if (changed) {
            addTile()
            this.delegated.applyGame(this.grid, this.step, this.score)
        }
    }

    private fun slideTiles(vararg indexes: Int): Boolean {
        var moved = false
        val tmpArr = intArrayOf(*indexes)

        var i = 0 // Indicate the index of empty spot
        for (j in tmpArr.indices) {
            if (grid[tmpArr[i]] != null) {
                i++
                continue
            } else if (grid[tmpArr[j]] == null) continue
            else {
                // Do slide
                grid[tmpArr[i]] =
                    grid[tmpArr[j]]?.let {
                        if (it.type == TransitionTypes.MERGE) {
                            Transition(
                                type = TransitionTypes.MERGE,
                                value = it.value,
                                pos = i,
                                posA = it.posA,
                                posB = it.posB
                            )
                        } else {
                            Transition(
                                type = TransitionTypes.MOVE,
                                value = it.value,
                                pos = i,
                                posA = j
                            )
                        }
                    }
                grid[tmpArr[j]] = null
                moved = true
                i++
            }
        }
        return moved
    }

    private fun compactTiles(vararg indexes: Int): Boolean {
        var compacted = false
        val tmpArr = intArrayOf(*indexes)

        for (i in 0 until (tmpArr.size - 1)) {
            if (grid[tmpArr[i]] != null
                && (grid[tmpArr[i]]?.value ?: 1) == (grid[tmpArr[i + 1]]?.value ?: -1)
            ) {
                val a = grid[tmpArr[i]] as Transition
                val b = grid[tmpArr[i + 1]] as Transition
                val value = a.value * 2
                grid[tmpArr[i]] = Transition(
                    type = TransitionTypes.MERGE,
                    value = value,
                    pos = i,
                    posA = a.posA,
                    posB = b.posA
                )
                grid[tmpArr[i + 1]] = null
                this.score += value
                if (value > maxTile) maxTile = value
                compacted = true
                emptyCount += 1
            }
        }
        return compacted
    }

    private fun slideUp(): Boolean {
        val a = slideTiles(0, 4, 8, 12)
        val b = slideTiles(1, 5, 9, 13)
        val c = slideTiles(2, 6, 10, 14)
        val d = slideTiles(3, 7, 11, 15)
        return (a || b || c || d)
    }

    private fun compactUp(): Boolean {
        val a = compactTiles(0, 4, 8, 12)
        val b = compactTiles(1, 5, 9, 13)
        val c = compactTiles(2, 6, 10, 14)
        val d = compactTiles(3, 7, 11, 15)
        return (a || b || c || d)
    }

    private fun slideRight(): Boolean {
        val a = slideTiles(3, 2, 1, 0)
        val b = slideTiles(7, 6, 5, 4)
        val c = slideTiles(11, 10, 9, 8)
        val d = slideTiles(15, 14, 13, 12)
        return (a || b || c || d)
    }

    private fun compactRight(): Boolean {
        val a = compactTiles(3, 2, 1, 0)
        val b = compactTiles(7, 6, 5, 4)
        val c = compactTiles(11, 10, 9, 8)
        val d = compactTiles(15, 14, 13, 12)
        return (a || b || c || d)
    }

    private fun slideDown(): Boolean {
        val a = slideTiles(12, 8, 4, 0)
        val b = slideTiles(13, 9, 5, 1)
        val c = slideTiles(14, 10, 6, 2)
        val d = slideTiles(15, 11, 7, 3)
        return (a || b || c || d)
    }

    private fun compactDown(): Boolean {
        val a = compactTiles(12, 8, 4, 0)
        val b = compactTiles(13, 9, 5, 1)
        val c = compactTiles(14, 10, 6, 2)
        val d = compactTiles(15, 11, 7, 3)
        return (a || b || c || d)
    }

    private fun slideLeft(): Boolean {
        val a = slideTiles(0, 1, 2, 3)
        val b = slideTiles(4, 5, 6, 7)
        val c = slideTiles(8, 9, 10, 11)
        val d = slideTiles(12, 13, 14, 15)
        return (a || b || c || d)
    }

    private fun compactLeft(): Boolean {
        val a = compactTiles(0, 1, 2, 3)
        val b = compactTiles(4, 5, 6, 7)
        val c = compactTiles(8, 9, 10, 11)
        val d = compactTiles(12, 13, 14, 15)
        return (a || b || c || d)
    }

    private fun moveUp(): Boolean {
        val a = slideUp()
        val b = compactUp()
        val c = slideUp()
        return (a || b || c)
    }

    private fun moveRight(): Boolean {
        val a = slideRight()
        val b = compactRight()
        val c = slideRight()
        return (a || b || c)
    }

    private fun moveDown(): Boolean {
        val a = slideDown()
        val b = compactDown()
        val c = slideDown()
        return (a || b || c)
    }

    private fun moveLeft(): Boolean {
        val a = slideLeft()
        val b = compactLeft()
        val c = slideLeft()
        return (a || b || c)
    }

    private fun initGrid() {
        if (this.grid.size != GameEngineConstants.TILE_COUNT) {
            this.grid =
                Array<Transition?>(GameEngineConstants.TILE_COUNT) { null }.toCollection(ArrayList())
        } else {
            for (i in 0 until GameEngineConstants.TILE_COUNT) this.grid[i] = null
        }
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
            if (grid[i] == null) {
                if (blankCount == pos) {
                    grid[i] = Transition(TransitionTypes.NEW, value, pos)
                    score += value
                    emptyCount--
                    maxTile = max(maxTile, value)
                    return false
                }
                blankCount++
            }
        }
        return false
    }

    fun printGrid(grid: ArrayList<Transition?>? = this.grid) {

        var str = "----------\n---------\n"
        for (i in 0 until GameEngineConstants.ROW_COUNT) {
            for (j in 0 until GameEngineConstants.COLUMN_COUNT) {
                str += "| " + (grid?.get(i * GameEngineConstants.COLUMN_COUNT + j)?.value.toString()) + " "
            }
            str += "|\n"
        }
        str += "---------"
        Log.d(TAG, str)
    }

    companion object {
        enum class Moves { UP, RIGHT, DOWN, LEFT }

        fun toGrid(grid: String?, isNew: Boolean = false): ArrayList<Transition?>? {
            if (grid == null) return null

            val arr = grid.split("|").map { it.toIntOrNull() ?: GameEngineConstants.BLANK_VALUE }

            if (arr.size != GameEngineConstants.TILE_COUNT) return null

            var tileCount = 0

            val arrList = ArrayList<Transition?>()

            for ((pos, value) in arr.withIndex()) {
                if (value == GameEngineConstants.BLANK_VALUE || value < 0) {
                    arrList.add(null)
                } else {
                    tileCount++
                    arrList.add(
                        Transition(
                            if (isNew) TransitionTypes.NEW else TransitionTypes.NONE,
                            value,
                            pos
                        )
                    )
                }
            }

            return if (tileCount < 2) null else arrList
        }

        fun fromGrid(grid: ArrayList<Transition?>?): String? {
            if (grid == null || grid.size != GameEngineConstants.TILE_COUNT) return null

            var str = ""
            for ((i, trans) in grid.withIndex()) {
                if (i != 0) str += "|"
                str += trans?.value?.toString() ?: GameEngineConstants.BLANK_VALUE.toString()
            }
            return str
        }
    }
}