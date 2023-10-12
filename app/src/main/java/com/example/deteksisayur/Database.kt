import com.example.deteksisayur.Data
import com.example.deteksisayur.DatabasePOST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Database {

    private val BASE_URL = "http://10.2.4.6:3934/api/"
    private val retrofit: Retrofit
    private val apiService: DatabasePOST

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(DatabasePOST::class.java)
    }

    fun sendDataToDatabase(data: Data, callback: (Boolean) -> Unit) {
        val call: Call<Void> = apiService.sendDeteksiResult(data)

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

