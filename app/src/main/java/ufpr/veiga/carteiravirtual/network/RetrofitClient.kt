package ufpr.veiga.carteiravirtual.network

import retrofit2.converter.gson.GsonConverterFactory
import ufpr.veiga.carteiravirtual.api.AwesomeApiService
import retrofit2.Retrofit


object RetrofitClient {
    private const val BASE_URL = "https://economia.awesomeapi.com.br/json/"

    val awesomeApi: AwesomeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AwesomeApiService::class.java)
    }
}