package com.programeparaandroid.svceear

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
@Dao
interface DAO {
    @Query("SELECT * FROM visitantes")
    fun getAll(): List<VisitanteDB>

    @Query("SELECT * FROM visitantes WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<VisitanteDB>

    @Query("SELECT * FROM visitantes WHERE nome_completo LIKE :first AND " +
            "cpf LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): VisitanteDB

    @Query("SELECT * FROM visitantes WHERE cpf LIKE :first LIMIT 1")
    fun findByCPF(first: String): VisitanteDB

    @Insert
    fun insertAll(vararg users: VisitanteDB)

    @Delete
    fun delete(user: VisitanteDB)
}