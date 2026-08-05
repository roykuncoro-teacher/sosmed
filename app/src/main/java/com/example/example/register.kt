package com.example.example

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class register : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    // Inisialisasi Instance Firebase
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            val ivAvatar = findViewById<ShapeableImageView>(R.id.ivAvatar)
            ivAvatar.setImageURI(it)
            ivAvatar.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val ivAvatar = findViewById<ShapeableImageView>(R.id.ivAvatar)
        val etName = findViewById<EditText>(R.id.etName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmailReg = findViewById<EditText>(R.id.etEmailReg)
        val etPassReg = findViewById<EditText>(R.id.etPassReg)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)

        btnBack.setOnClickListener { finish() }

        ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmailReg.text.toString().trim()
            val password = etPassReg.text.toString().trim()

            if (validateForm(name, username, email, password)) {
                btnRegister.isEnabled = false
                performRegister(name, username, email, password)
            }
        }
    }

    private fun performRegister(name: String, username: String, email: String, password: String) {
        // 1. Pendaftaran Pengguna di Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                // Jika ada foto profil yang dipilih, unggah ke Storage terlebih dahulu
                if (selectedImageUri != null) {
                    uploadAvatarAndSaveUser(uid, name, username, email)
                } else {
                    saveUserToFirestore(uid, name, username, email, "")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Registrasi Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                findViewById<MaterialButton>(R.id.btnRegister).isEnabled = true
            }
    }

    private fun uploadAvatarAndSaveUser(uid: String, name: String, username: String, email: String) {
        val storageRef = storage.reference.child("avatars/$uid.jpg")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveUserToFirestore(uid, name, username, email, downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal unggah gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                    saveUserToFirestore(uid, name, username, email, "")
                }
        }
    }

    private fun saveUserToFirestore(uid: String, name: String, username: String, email: String, avatarUrl: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "name" to name,
            "username" to username,
            "email" to email,
            "avatarUrl" to avatarUrl
        )

        // 2. Simpan Dokumen Profil Pengguna di Koleksi "users"
        firestore.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                findViewById<MaterialButton>(R.id.btnRegister).isEnabled = true
            }
    }

    private fun validateForm(name: String, username: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (username.isEmpty() || username.contains(" ")) {
            Toast.makeText(this, "Username tidak boleh kosong atau mengandung spasi", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}