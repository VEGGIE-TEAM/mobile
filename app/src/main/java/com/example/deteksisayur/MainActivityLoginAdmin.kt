package com.example.deteksisayur

import Api.APILogin
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

class MainActivityLoginAdmin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_login_admin)

        val usernameEditText = findViewById<EditText>(R.id.editTextUsernameAdmin)
        val passwordEditText = findViewById<EditText>(R.id.editTextPasswordAdmin)
        val loginButton = findViewById<Button>(R.id.loginAdmin)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginData = DLoginUser(username, password)

            APILogin.RetrofitClient.instance.adminLogin(loginData)
                .enqueue(object : Callback<DLoginUser> {
                    override fun onResponse(call: Call<DLoginUser>, response: Response<DLoginUser>) {
                        if (response.isSuccessful) {
                            val intent = Intent(this@MainActivityLoginAdmin, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@MainActivityLoginAdmin, "Login Gagal. Periksa kembali username dan password Anda.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DLoginUser>, t: Throwable) {
                        Toast.makeText(this@MainActivityLoginAdmin, "Terjadi kesalahan. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        val userTextView = findViewById<TextView>(R.id.loginUser)
        userTextView.setOnClickListener {
            val intent = Intent(this, MainActivityLogin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
}
