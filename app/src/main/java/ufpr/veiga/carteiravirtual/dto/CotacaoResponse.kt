package ufpr.veiga.carteiravirtual.dto

data class CotacaoResponse(
    val code: String,
    val name: String,
    val high: Double,
    val low: String,
    val bid: String) {}
