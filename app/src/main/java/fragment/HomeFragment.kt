package com.example.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout feed/dashboard Anda di sini
        return inflater.inflate(R.layout.item_post, container, false) // Ganti dengan layout feed beranda Anda
    }
}