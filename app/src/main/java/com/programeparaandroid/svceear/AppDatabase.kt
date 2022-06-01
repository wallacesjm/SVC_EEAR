package com.programeparaandroid.svceear

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [VisitanteDB::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): DAO
}