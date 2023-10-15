package com.example.deteksisayur

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivityLupaPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_lupa_password)

        val Username = findViewById<EditText>(R.id.editTextUsername)
        val buttonLanjut = findViewById<Button>(R.id.lanjut)

        buttonLanjut.setOnClickListener {
            val user = Username.text.toString()

            if (user.isNotEmpty()) {
                val intent = Intent(this, MainActivityGantiPassword::class.java)
                intent.putExtra("USERNAME", user)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Username harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
