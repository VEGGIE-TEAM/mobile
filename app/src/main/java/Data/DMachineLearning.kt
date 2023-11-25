package Data

import Interface.InterfaceMachineLearning
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class APIMachineLearning(private val context: Context, private val callback: (String) -> Unit) {

    private val BASE_URL = "https://1g1kc00d-3936.asse.devtunnels.ms/"
    private val retrofit: Retrofit
    private val apiService: InterfaceMachineLearning
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(InterfaceMachineLearning::class.java)
    }

    fun sendImageToServer(bitmap: Bitmap) {
        val file = File.createTempFile("temp_image", null, context.cacheDir)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestBody)

        val call = apiService.uploadImage(body)

        call.enqueue(object : Callback<DMachineLearning> {
            override fun onResponse(call: Call<DMachineLearning>, response: Response<DMachineLearning>) {
                if (response.isSuccessful) {
                    val hasilDeteksi = response.body()?.predicted_class
                    mainHandler.post { callback(hasilDeteksi ?: "") }
                } else {
                    mainHandler.post {
                        Toast.makeText(context, "Gagal menerima respons deteksi", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<DMachineLearning>, t: Throwable) {
                mainHandler.post {
                    Toast.makeText(context, "Gagal mengirim permintaan deteksi", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}

data class DMachineLearning(
    val predicted_class: String
)

data class sayurdeteksi(
    val bentuk: String,
    val warna: String,
    val label: String
)

data class bentuk(
    val panjang: Double,
    val lebar: Double
)

data class warna(
    val red: String,
    val green: String,
    val blue: String
)