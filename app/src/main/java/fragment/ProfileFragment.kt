package com.example.example

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val userPostImages = mutableListOf<String>()
    private lateinit var gridAdapter: ProfileGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup UI Components
        setupRecyclerView(view)
        setupTabLayout(view)

        // 2. Fetch Data dari Firebase
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            loadUserProfile(view, currentUserId)
            loadUserPosts(view, currentUserId)
        } else {
            redirectToLogin()
        }

        // 3. Listener Klik
        view.findViewById<ImageView>(R.id.btnMenu).setOnClickListener {
            showMenuOptions()
        }

        view.findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Edit Profil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView(view: View) {
        val rvProfileGrid = view.findViewById<RecyclerView>(R.id.rvProfileGrid)
        gridAdapter = ProfileGridAdapter(userPostImages)
        rvProfileGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        rvProfileGrid.adapter = gridAdapter
    }

    private fun setupTabLayout(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_menu))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_user))
    }

    private fun loadUserProfile(view: View, uid: String) {
        firestore.collection("users").document(uid)
            .addSnapshotListener { document, error ->
                if (error != null || document == null || !document.exists()) return@addSnapshotListener

                val username = document.getString("username") ?: "username"
                val name = document.getString("name") ?: "Nama Pengguna"
                val bio = document.getString("bio") ?: "Bio Profil..."
                val avatarUrl = document.getString("avatarUrl") ?: ""

                view.findViewById<TextView>(R.id.tvUsernameTop).text = username
                view.findViewById<TextView>(R.id.tvFullName).text = name
                view.findViewById<TextView>(R.id.tvBio).text = bio

                val ivProfile = view.findViewById<ShapeableImageView>(R.id.ivProfile)
                if (avatarUrl.isNotEmpty() && isAdded) {
                    Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.img_placeholder)
                        .into(ivProfile)
                }
            }
    }

    private fun loadUserPosts(view: View, uid: String) {
        firestore.collection("posts")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                snapshot?.let {
                    userPostImages.clear()
                    for (doc in it.documents) {
                        val imageUrl = doc.getString("imageUrl")
                        if (!imageUrl.isNullOrEmpty()) {
                            userPostImages.add(imageUrl)
                        }
                    }

                    view.findViewById<TextView>(R.id.tvPostCount).text = userPostImages.size.toString()
                    gridAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun showMenuOptions() {
        val options = arrayOf("Logout", "Batal")
        AlertDialog.Builder(requireContext())
            .setTitle("Opsi Akun")
            .setItems(options) { dialog, which ->
                if (which == 0) {
                    auth.signOut()
                    redirectToLogin()
                } else {
                    dialog.dismiss()
                }
            }
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(requireActivity(), login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}