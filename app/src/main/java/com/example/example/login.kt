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
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {

    // 1. Inisialisasi Instance Firebase Auth
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

        // Inisialisasi Komponen UI
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Event Handling Tombol Login
        btnLogin?.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Jalankan Validasi Input
            if (validateInput(email, password)) {
                // Nonaktifkan tombol sementara untuk mencegah multiple click saat proses verifikasi
                btnLogin.isEnabled = false

                // Proses Verifikasi Pengguna ke Firebase Auth
                performLogin(email, password, btnLogin)
            }
        }

        // Event Handling Tombol Google Login
        btnGoogle?.setOnClickListener {
            Toast.makeText(this, "Fitur Google Sign-In dapat ditambahkan di sini", Toast.LENGTH_SHORT).show()
        }

        // Event Handling Teks Registrasi
        tvRegister?.setOnClickListener {
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }
    }

    /**
     * Memeriksa apakah pengguna sudah pernah login sebelumnya (Auto-Login)
     */
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            // Jika sudah ada sesi aktif, langsung arahkan ke Dashboard
            startActivity(Intent(this, dashboard::class.java))
            finish()
        }
    }

    /**
     * Fungsi untuk memverifikasi email & password ke Firebase Authentication
     */
    private fun performLogin(email: String, password: String, btnLogin: Button) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                // Pindah ke Halaman Dashboard
                val intent = Intent(this, dashboard::class.java)
                startActivity(intent)
                finish() // Menutup halaman login
            }
            .addOnFailureListener { exception ->
                // Kembalikan tombol agar bisa diklik kembali
                btnLogin.isEnabled = true

                Toast.makeText(
                    this,
                    "Login Gagal: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Fungsi untuk memvalidasi kelayakan input lokal pengguna
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