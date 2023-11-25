
package com.example.deteksisayur

import Data.APILogin
import Data.DLoginUser
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivityDaftarAkun : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_daftar_akun)

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextConfirmPassword = findViewById<EditText>(R.id.editTextConfirm)

        val buttonDaftar = findViewById<Button>(R.id.loginUser)
        buttonDaftar.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()

            if (password != confirmPassword) {
                Toast.makeText(this@MainActivityDaftarAkun, "Konfirmasi kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = DLoginUser(username, password)

            val call = APILogin.RetrofitClient.instance.registerUser(user)
            val baseUrl = APILogin.RetrofitClient.BASE_URL
            val jsonMediaType = "application/json; charset=utf-8"
            val requestBody = RequestBody.create(MediaType.parse(jsonMediaType), Gson().toJson(user))
            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivityDaftarAkun, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@MainActivityDaftarAkun, MainActivityLogin::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivityDaftarAkun, "Gagal mendaftar. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivityDaftarAkun, "Gagal mendaftar. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}