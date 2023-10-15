package Api

import Data.DDatabase
import Interface.InterfaceDatabasePOST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIDatabase {

    private val BASE_URL = "http://192.168.133.19:3934/api/"
    private val retrofit: Retrofit
    private val apiService: InterfaceDatabasePOST

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(InterfaceDatabasePOST::class.java)
    }

    fun sendDataToDatabase(DDatabase: DDatabase, callback: (Boolean) -> Unit) {
        val call: Call<Void> = apiService.sendDeteksiResult(DDatabase)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                callback(false)
            }
        })
    }
}

