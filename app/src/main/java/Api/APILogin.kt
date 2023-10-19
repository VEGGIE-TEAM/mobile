package Api

import Interface.InterfaceLogin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APILogin {
    object RetrofitClient {
        const val BASE_URL = "http://192.168.43.191:3935/"

        val instance: InterfaceLogin by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(InterfaceLogin::class.java)
        }
    }
}