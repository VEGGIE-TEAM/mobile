package Interface

import Data.DLoginUser
import retrofit2.Call
import retrofit2.http.*

interface InterfaceLoginUser {

    @Headers("Content-Type: application/json")
    @PUT("users")
    fun updatePassword(@Body user: DLoginUser): Call<DLoginUser>

    @Headers("Content-Type: application/json")
    @POST("users")
    fun registerUser(@Body user: DLoginUser): Call<Void>
}
