package ufpr.veiga.carteiravirtual.api

import retrofit2.http.GET
import retrofit2.http.Path
import ufpr.veiga.carteiravirtual.dto.CotacaoResponse

interface AwesomeApiService {

    @GET("last/{from}-{to}")
    suspend fun getCotacao(@Path("from") moedaOrigem: String, @Path("to") moedaDestino: String): Map<String, CotacaoResponse>
}