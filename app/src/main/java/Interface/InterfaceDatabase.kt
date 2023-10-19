package Interface

import Data.DDatabase
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface InterfaceDatabase {
    @Multipart
    @POST("api/data")
    fun postKirimServer(
        @PartMap deteksiData: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part gambar: MultipartBody.Part
    ): Call<DDatabase>

    @Headers("Content-Type: application/json")
    @GET("/api/data")
    fun getData(): Call<List<DDatabase>>
}
