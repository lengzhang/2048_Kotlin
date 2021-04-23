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

/*


    @TypeConverter
    fun toGrid(grid: String?): ArrayList<Int>? {
        val arr = grid?.split("|")?.map { it.toInt() } ?: return null
        return ArrayList(arr)
    }

    @TypeConverter
    fun fromGrid(grid: ArrayList<Int>?): String? {
        return grid?.joinToString(
            separator = "|"
        )
    }

 */