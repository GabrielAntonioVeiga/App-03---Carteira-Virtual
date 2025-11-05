package ufpr.veiga.carteiravirtual.network

import retrofit2.converter.gson.GsonConverterFactory
import ufpr.veiga.carteiravirtual.api.AwesomeApiService
import retrofit2.Retrofit


object RetrofitClient {
    private const val BASE_URL = "https://economia.awesomeapi.com.br/json/"

    val awesomeApi: AwesomeApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(AwesomeApiService::class.java)
    }
}