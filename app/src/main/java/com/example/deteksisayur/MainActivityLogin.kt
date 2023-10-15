package com.example.deteksisayur

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivityLogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_login)

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
