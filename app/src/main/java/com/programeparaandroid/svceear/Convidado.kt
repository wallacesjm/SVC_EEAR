package com.programeparaandroid.svceear

import kotlin.properties.Delegates

class Convidado {
    lateinit var nome_completo: String
    lateinit var cpf: String
    lateinit var nome_guerra: String
    lateinit var milhao: String
    lateinit var esquadrao: String
    var padrinho by Delegates.notNull<Boolean>()

}