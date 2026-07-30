package com.example.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        // Handling Window Insets untuk Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Inisialisasi Komponen dari XML (Pastikan ID di XML sesuai)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // 2. Event Handling Tombol Login
        btnLogin?.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Jalankan Validasi Input
            if (validateInput(email, password)) {
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                // Pindah ke MainActivity (Ganti MainActivity dengan nama Activity Utama Anda)
                val intent = Intent(this, dashboard::class.java)
                startActivity(intent)
                finish() // Menutup halaman login agar tidak bisa dikembalikan dengan tombol back
            }
        }

        // 3. Event Handling Tombol Google Login
        btnGoogle?.setOnClickListener {
            Toast.makeText(this, "Menghubungkan ke Google...", Toast.LENGTH_SHORT).show()
            // Tempatkan logika Firebase Auth / Google Sign-In Anda di sini
        }

        // 4. Event Handling Teks Registrasi
        tvRegister?.setOnClickListener {
            // Pindah ke Halaman Daftar (Ganti RegisterActivity dengan nama Activity Daftar Anda)
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }
    }

    /**
     * Fungsi untuk memvalidasi kelayakan input pengguna
     */
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}