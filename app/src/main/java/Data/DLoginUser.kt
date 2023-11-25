package Data

import Interface.InterfaceLogin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APILogin{
    object RetrofitClient {
        const val BASE_URL = "https://1g1kc00d-3935.asse.devtunnels.ms/"

        val instance: InterfaceLogin by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(InterfaceLogin::class.java)
        }
    }
}

data class DLoginUser(
    val username: String,
    val password: String
)
