package com.lengzhang.android.lz2048.database

import androidx.room.TypeConverter
import java.util.*

class GameTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? = millisSinceEpoch?.let {
        Date(it)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid: String?): UUID? = UUID.fromString(uuid)

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? = list?.joinToString(",")

    @TypeConverter
    fun toIntList(listString: String?): List<Int>? {
        val tmpList = listString?.split(",")
        return tmpList?.map {
            it.toInt()
        }
    }
}