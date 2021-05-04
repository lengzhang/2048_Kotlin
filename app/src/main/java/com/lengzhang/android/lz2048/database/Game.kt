package com.lengzhang.android.lz2048.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

enum class GameStatus { PLAYING, WIN, LOSE }

@Entity(tableName = "games")
data class Game(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var grid: List<Int>,
    var step: Int,
    var score: Int,
    @ColumnInfo(name = "created_at") val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at") var updatedAt: Date = Date(),
    var status: GameStatus = GameStatus.PLAYING
) {
    companion object {

        fun isEqual(a: Game, b: Game) = when {
            a.grid != b.grid -> false
            a.step != b.step -> false
            a.score != b.step -> false
            else -> true
        }
    }
}