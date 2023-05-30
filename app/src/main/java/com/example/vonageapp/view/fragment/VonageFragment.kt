package com.example.vonageapp.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vonageapp.R
import com.example.vonageapp.databinding.FragmentVonageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VonageFragment : Fragment() {
    private var binding: FragmentVonageBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVonageBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding!!.root
    }

}