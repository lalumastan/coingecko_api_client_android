package icsdiscover.coingecko.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import icsdiscover.coingecko.api.CoinGeckoApiServices
import icsdiscover.coingecko.api.model.CoinGeckoPingResponse
import icsdiscover.coingecko.api.model.CoinGeckoTable
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class CoinGeckoViewModel : ViewModel() {
    private var mutableLiveData = MutableLiveData<List<CoinGeckoTable>>()

    private val okHttpClientBuilder = OkHttpClient.Builder()

    private fun jsonServicebuilder(): Retrofit.Builder {
        return Retrofit.Builder().baseUrl("https://api.coingecko.com/api/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientBuilder.build())
    }

    private fun <T> buildJsonService(serviceType: Class<T>): T {
        return jsonServicebuilder().build().create(serviceType)
    }

    private val destinationService = buildJsonService(CoinGeckoApiServices::class.java)

    init {
        refreshCoinGeckoList()
    }

    val liveData: MutableLiveData<List<CoinGeckoTable>> = mutableLiveData

    private fun readFromCache(): List<CoinGeckoTable> {
        val file = File(CoinGeckoFragment.ACTIVITY!!.filesDir, CoinGeckoFragment.FILE_NAME)
        val fileReader = FileReader(file)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close() // This responce will have Json Format String
        val arr = Gson().fromJson(stringBuilder.toString(), Array<CoinGeckoTable>::class.java)
        return listOf(*arr)
    }

    private fun writeToCache(list: List<CoinGeckoTable>) {
        try {
            val gson = Gson()
            val file = File(CoinGeckoFragment.ACTIVITY!!.filesDir, CoinGeckoFragment.FILE_NAME)
            val fileWriter = FileWriter(file)
            val bufferedWriter = BufferedWriter(fileWriter)
            bufferedWriter.write(gson.toJson(list))
            bufferedWriter.close()
        } catch (e: Exception) {
            e.message?.let { it1 -> Log.e("onCreateView", it1) }
        }
    }

    fun refreshCoinGeckoList() {
        mutableLiveData.apply {
            destinationService.getCoinGeckoPing().enqueue(object :
                Callback<CoinGeckoPingResponse> {

                override fun onResponse(
                    call: Call<CoinGeckoPingResponse>,
                    responseCoinGeckoPing: Response<CoinGeckoPingResponse>
                ) {
                    if (responseCoinGeckoPing.isSuccessful) {
                        var vl: String = responseCoinGeckoPing.body()!!.gecko_says

                        if (vl.isNotEmpty()) {
                            destinationService.getCoinGeckoTableList().enqueue(object :
                                Callback<List<CoinGeckoTable>> {

                                override fun onResponse(
                                    call: Call<List<CoinGeckoTable>>,
                                    responseCoinGeckoTableList: Response<List<CoinGeckoTable>>
                                ) {
                                    if (responseCoinGeckoTableList.isSuccessful) {
                                        //var list: List<CoinGeckoTable> = responseCoinGeckoTableList.body()!!
                                        value = responseCoinGeckoTableList.body()

                                        value?.let { writeToCache(it) }
                                    } else {
                                        Log.e(
                                            "TransformViewModel",
                                            "getCoinGeckoTableList failed"
                                        )
                                        value = readFromCache()
                                    }
                                }

                                override fun onFailure(
                                    call: Call<List<CoinGeckoTable>>,
                                    t: Throwable
                                ) {
                                    Log.e(
                                        "TransformViewModel",
                                        "getCoinGeckoTableList failed.  Loading from local cache ..."
                                    )
                                    value = readFromCache()
                                }
                            })
                        }
                    } else {
                        Log.e("TransformViewModel", "getCoinGeckoPing failed")
                        value = readFromCache()
                    }
                }

                override fun onFailure(
                    call: Call<CoinGeckoPingResponse>,
                    t: Throwable
                ) {
                    Log.e(
                        "TransformViewModel",
                        "getCoinGeckoPing failed.  Loading from local cache ..."
                    )
                    value = readFromCache()
                }
            })
        }
    }
}