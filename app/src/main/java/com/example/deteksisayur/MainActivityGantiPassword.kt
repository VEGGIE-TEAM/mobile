package com.example.deteksisayur

import Api.APILogin
import Interface.InterfaceLogin
import Data.DLoginUser
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivityGantiPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ganti_password)

        val passwordBaru = findViewById<EditText>(R.id.editTextPasswordBaru)
        val confirmPassword = findViewById<EditText>(R.id.editTextKonfirmasiPasswordBaru)

        val buttonGanti = findViewById<Button>(R.id.lanjut)

        val receivedIntent = intent
        val username = receivedIntent.getStringExtra("USERNAME") ?: ""

        buttonGanti.setOnClickListener {
            val password = passwordBaru.text.toString()
            val konfirmasiPassword = confirmPassword.text.toString()
            val baseUrl = APILogin.RetrofitClient.BASE_URL

            if (password == konfirmasiPassword) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(InterfaceLogin::class.java)

                val user = DLoginUser(username, password)
                val call = service.updatePassword(user)
                call.enqueue(object : Callback<DLoginUser> {
                    override fun onResponse(call: Call<DLoginUser>, response: Response<DLoginUser>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MainActivityGantiPassword, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivityGantiPassword, MainActivityLogin::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@MainActivityGantiPassword, "Gagal memperbarui password", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DLoginUser>, t: Throwable) {
                        Toast.makeText(this@MainActivityGantiPassword, "Terjadi kesalahan: " + t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Kata sandi tidak sesuai", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
