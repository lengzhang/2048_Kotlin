package com.lengzhang.android.lz2048.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY created_at DESC")
    fun getGames(): LiveData<List<Game>>

    @Query("SELECT * FROM games WHERE id=(:id)")
    fun getGame(id: UUID): LiveData<Game?>

    @Insert
    fun addGame(game: Game)

    @Update
    fun update(game: Game)

    @Delete
    fun delete(game: Game)
}