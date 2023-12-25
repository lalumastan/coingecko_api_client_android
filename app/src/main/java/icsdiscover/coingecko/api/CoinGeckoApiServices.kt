package icsdiscover.coingecko.api

import icsdiscover.coingecko.api.model.CoinGeckoPingResponse
import icsdiscover.coingecko.api.model.CoinGeckoTable
import retrofit2.Call
import retrofit2.http.GET

interface CoinGeckoApiServices {
    @GET("coins/markets?vs_currency=usd&per_page=250&page=1")
    fun getCoinGeckoTableList(): Call<List<CoinGeckoTable>>

    @GET("ping")
    fun getCoinGeckoPing(): Call<CoinGeckoPingResponse>
}