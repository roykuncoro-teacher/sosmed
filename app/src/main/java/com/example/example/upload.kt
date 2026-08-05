package com.example.example

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class upload : AppCompatActivity() {

    private var selectedMediaUri: Uri? = null
    private var isPhotoSelected: Boolean = true

    // Firebase Instances
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // Launcher Pemilih File Gambar / Video
    private val mediaPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedMediaUri = it
            val ivPreview = findViewById<ImageView>(R.id.ivPreview)
            ivPreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.upload)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.upload)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi UI
        val btnClose = findViewById<ImageView>(R.id.btnClose)
        val tvShare = findViewById<TextView>(R.id.tvShare)
        val ivPreview = findViewById<ImageView>(R.id.ivPreview)
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)

        // Tombol Kembali
        btnClose.setOnClickListener { finish() }

        // Switch Toggle Foto / Video
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isPhotoSelected = checkedId == R.id.btnPhoto
                selectedMediaUri = null
                ivPreview.setImageURI(null) // Reset preview saat tipe berubah
            }
        }

        // Klik Area Preview untuk Pilih Media
        ivPreview.setOnClickListener {
            openMediaPicker()
        }

        // Tombol Bagikan
        tvShare.setOnClickListener {
            val caption = findViewById<EditText>(R.id.etCaption).text.toString().trim()
            if (selectedMediaUri == null) {
                Toast.makeText(this, "Silakan pilih foto atau video terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvShare.isEnabled = false
            Toast.makeText(this, "Mengunggah...", Toast.LENGTH_SHORT).show()
            startUploadProcess(caption, tvShare)
        }
    }

    private fun openMediaPicker() {
        val mimeType = if (isPhotoSelected) "image/*" else "video/*"
        mediaPickerLauncher.launch(mimeType)
    }

    private fun startUploadProcess(caption: String, tvShare: TextView) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Sesi berakhir. Silakan login kembali.", Toast.LENGTH_SHORT).show()
            tvShare.isEnabled = true
            return
        }

        // 1. Ambil Data User (Username & Avatar) dari Firestore
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { userDoc ->
                val username = userDoc.getString("username") ?: "Anonim"
                val userAvatarUrl = userDoc.getString("avatarUrl") ?: ""

                // 2. Unggah Media ke Firebase Storage
                uploadMediaToStorage(currentUserId, username, userAvatarUrl, caption, tvShare)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat profil: ${e.message}", Toast.LENGTH_SHORT).show()
                tvShare.isEnabled = true
            }
    }

    private fun uploadMediaToStorage(
        uid: String,
        username: String,
        userAvatarUrl: String,
        caption: String,
        tvShare: TextView
    ) {
        val fileExtension = if (isPhotoSelected) "jpg" else "mp4"
        val folder = if (isPhotoSelected) "post_photos" else "post_videos"
        val fileName = "${UUID.randomUUID()}.$fileExtension"
        val storageRef = storage.reference.child("$folder/$fileName")

        selectedMediaUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { mediaDownloadUri ->
                        // 3. Simpan Metadata Post ke Cloud Firestore
                        savePostToFirestore(
                            uid,
                            username,
                            userAvatarUrl,
                            mediaDownloadUri.toString(),
                            caption,
                            tvShare
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mengunggah media: ${e.message}", Toast.LENGTH_SHORT).show()
                    tvShare.isEnabled = true
                }
        }
    }

    private fun savePostToFirestore(
        uid: String,
        username: String,
        userAvatarUrl: String,
        mediaUrl: String,
        caption: String,
        tvShare: TextView
    ) {
        val postId = firestore.collection("posts").document().id

        val postMap = hashMapOf(
            "postId" to postId,
            "userId" to uid,
            "username" to username,
            "userAvatarUrl" to userAvatarUrl,
            "imageUrl" to mediaUrl, // Jika video, disimpan dalam atribut URL yang sama
            "caption" to caption,
            "mediaType" to if (isPhotoSelected) "image" else "video",
            "likesCount" to 0,
            "commentsCount" to 0,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("posts").document(postId)
            .set(postMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Postingan berhasil dibagikan!", Toast.LENGTH_SHORT).show()
                finish() // Tutup activity upload
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan postingan: ${e.message}", Toast.LENGTH_SHORT).show()
                tvShare.isEnabled = true
            }
    }
}