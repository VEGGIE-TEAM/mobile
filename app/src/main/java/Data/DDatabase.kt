package Data

import java.io.File
import Interface.InterfaceDatabase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIDatabase{
    object APIClient {
        private const val BASE_URL =
            "https://1g1kc00d-3934.asse.devtunnels.ms/"

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

data class DDatabase(
    val nama_sayur: String,
    val nama_pasar: String,
    val gambar_sayur: File,
    val tanggal_input: String,
    val hasil_deteksi: String
)
