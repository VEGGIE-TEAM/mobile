package Interface

import Data.DMachineLearning
import okhttp3.MultipartBody
import retrofit2.Call
import okhttp3.RequestBody
import retrofit2.http.*

interface InterfaceMachineLearning {
     @Multipart
     @POST("predict")
     fun uploadImage(@Part image: MultipartBody.Part): Call<DMachineLearning>
}
