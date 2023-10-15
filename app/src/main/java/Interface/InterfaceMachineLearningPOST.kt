package Interface

import Data.DMachineLearning
import retrofit2.Call
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface InterfaceMachineLearningPOST {
     @POST("/predict")
     fun predict(@Body image: RequestBody): Call<DMachineLearning>
}
