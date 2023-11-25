package com.example.deteksisayur

import Data.APILogin
import Data.DLoginUser
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivityLogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_login)

        val usernameEditText = findViewById<EditText>(R.id.editTextUsername)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.loginUser)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginData = DLoginUser(username, password)

            APILogin.RetrofitClient.instance.userLogin(loginData)
                .enqueue(object : Callback<DLoginUser> {
                    override fun onResponse(call: Call<DLoginUser>, response: Response<DLoginUser>) {
                        if (response.isSuccessful) {
                            val intent = Intent(this@MainActivityLogin, MainActivityAnalisis::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@MainActivityLogin, "Login Gagal. Periksa kembali username dan password Anda.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DLoginUser>, t: Throwable) {
                        Toast.makeText(this@MainActivityLogin, "Terjadi kesalahan. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        val loginAnonimus = findViewById<TextView>(R.id.loginAnonimus)
        loginAnonimus.setOnClickListener {
            val intent = Intent(this, MainActivityAnalisis::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        val daftarAkunTextView = findViewById<TextView>(R.id.daftarAkun)
        daftarAkunTextView.setOnClickListener {
            val intent = Intent(this, MainActivityDaftarAkun::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        val loginAdminTextView = findViewById<TextView>(R.id.loginAdmin)
        loginAdminTextView.setOnClickListener {
            val intent = Intent(this, MainActivityLoginAdmin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        val lupaPasswordTextView = findViewById<TextView>(R.id.lupaPassword)
        lupaPasswordTextView.setOnClickListener {
            val intent = Intent(this, MainActivityLupaPassword::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }
}
