package Api

import Interface.InterfaceLoginUser
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APILoginUser {
    object RetrofitClient {
        const val BASE_URL = "http://10.219.39.30:3935/"

        val instance: InterfaceLoginUser by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(InterfaceLoginUser::class.java)
        }
    }
}