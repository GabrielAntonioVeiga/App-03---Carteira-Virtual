package ufpr.veiga.carteiravirtual.repository

interface AwesomeRepository {

    suspend fun obterCotacao(origem: String, destino: String): Double

}