package ufpr.veiga.carteiravirtual.repository

import ufpr.veiga.carteiravirtual.api.AwesomeApiService
import ufpr.veiga.carteiravirtual.network.RetrofitClient
import java.util.Objects

class AwesomeApiRepositoryImpl(private val apiService: AwesomeApiService) : AwesomeRepository {

    override suspend fun obterCotacao(origem: String, destino: String): Double {
        val cotacao = apiService.getCotacao(origem, destino)

        val cotacaoDetalhe = cotacao[origem + destino]

        if (Objects.isNull(cotacaoDetalhe)) {
            throw IllegalArgumentException("Cotação direta de $origem para $destino não encontrada")
        }

        val taxaEmString = cotacaoDetalhe?.bid

        return taxaEmString?.toDoubleOrNull() ?: throw IllegalArgumentException("Taxa inválida: $taxaEmString")
    }
}