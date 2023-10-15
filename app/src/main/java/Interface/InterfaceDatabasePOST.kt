package Interface

import Data.DDatabase
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface InterfaceDatabasePOST {
    @Headers("Content-Type: application/json")
    @POST("/api/DDatabase")
    fun sendDeteksiResult(@Body DDatabase: DDatabase): Call<Void>
}
