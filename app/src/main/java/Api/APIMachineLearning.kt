package Api

import android.graphics.Bitmap
import android.util.Base64
import Data.DMachineLearning
import Interface.InterfaceMachineLearningPOST
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class APIMachineLearning {

        private val BASE_URL = "http://10.2.4.6:5000/"
        private val retrofit: Retrofit
        private val apiService: InterfaceMachineLearningPOST

        init {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit.create(InterfaceMachineLearningPOST::class.java)
        }

    fun kirimDataGambarKeAPI(
        gambar: Bitmap,
        hasilDeteksi: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val base64Image = bitmapToBase64(gambar)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), base64Image)

        val call: Call<DMachineLearning> = apiService.predict(requestBody)

        call.enqueue(object : Callback<DMachineLearning> {
            override fun onResponse(call: Call<DMachineLearning>, response: Response<DMachineLearning>) {
                if (response.isSuccessful) {
                    val hasilPrediksi = response.body()?.result
                    if (hasilPrediksi != null) {
                        callback(true, hasilPrediksi)
                    } else {
                        callback(false, null)
                    }
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(call: Call<DMachineLearning>, t: Throwable) {
                callback(false, null)
            }
        })
    }


    private fun bitmapToBase64(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
}
