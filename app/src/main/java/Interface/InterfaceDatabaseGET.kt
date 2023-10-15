package Interface

import Data.DDatabase
import retrofit2.Call
import retrofit2.http.GET

interface InterfaceDatabaseGET {
    @GET("data")
    fun getData(): Call<List<DDatabase>>
}