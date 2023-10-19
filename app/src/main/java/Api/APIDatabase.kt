package Api

import Data.DDatabase
import Interface.InterfaceDatabase
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Part
import retrofit2.http.PartMap

class APIDatabase {
    object APIClient {
        private const val BASE_URL =
            "http://192.168.43.191:3934/" // Sesuaikan dengan URL server Anda

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api: InterfaceDatabase by lazy {
            retrofit.create(InterfaceDatabase::class.java)
        }
    }
}

