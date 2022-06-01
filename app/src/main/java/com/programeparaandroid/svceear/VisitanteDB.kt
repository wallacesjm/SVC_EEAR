package com.programeparaandroid.svceear

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitantes")
data class VisitanteDB(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "nome_completo") val nome_completo: String?,
    @ColumnInfo(name = "cpf") val cpf: String?,
    @ColumnInfo(name = "nome_guerra") val nome_guerra: String?,
    @ColumnInfo(name = "milhao") val milhao: String?,
    @ColumnInfo(name = "esquadrao") val esquadrao: String?
)
