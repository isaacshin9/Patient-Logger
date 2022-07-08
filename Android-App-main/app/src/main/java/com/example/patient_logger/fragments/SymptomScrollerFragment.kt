package com.example.patient_logger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.patient_logger.R
import com.example.patient_logger.databinding.FragmentSymptomScrollerBinding

lateinit var binding: FragmentSymptomScrollerBinding

class SymptomScrollerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSymptomScrollerBinding.inflate(layoutInflater)
        binding.img1.setOnClickListener {
            println("Image 1 had been clicked")
            Toast.makeText(activity, "Image 1 has been clicked!", Toast.LENGTH_SHORT).show()
            binding.imageViewArray.setImageResource(R.drawable.headache)
        }
        binding.img2.setOnClickListener {
            println("Image 2 had been clicked")
            Toast.makeText(activity, "Image 2 has been clicked!", Toast.LENGTH_SHORT).show()
            binding.imageViewArray.setImageResource(R.drawable.muscle_pain)
        }
        binding.img3.setOnClickListener {
            println("Image 3 had been clicked")
            Toast.makeText(activity, "Image 3 has been clicked!", Toast.LENGTH_SHORT).show()
            binding.imageViewArray.setImageResource(R.drawable.chest_pain_or_pressure)
        }
        binding.img4.setOnClickListener {
            println("Image 4 had been clicked")
            Toast.makeText(activity, "Image 4 has been clicked!", Toast.LENGTH_SHORT).show()
            binding.imageViewArray.setImageResource(R.drawable.fever)
        }
        binding.img5.setOnClickListener {
            println("Image 5 had been clicked")
            Toast.makeText(activity, "Image 5 has been clicked!", Toast.LENGTH_SHORT).show()
            binding.imageViewArray.setImageResource(R.drawable.dizzy)
        }
        binding.img6.setOnClickListener {
            println("Image 6 had been clicked")
            Toast.makeText(activity, "Image 6 has been clicked!", Toast.LENGTH_SHORT).show()
            binding.imageViewArray.setImageResource(R.drawable.pain_in_joints)
        }
        binding.img7.setOnClickListener {
            println("Image 7 had been clicked")
            Toast.makeText(activity, "Image 7 has been clicked!", Toast.LENGTH_SHORT).show()
            binding.imageViewArray.setImageResource(R.drawable.fatigue)
        }


        // Inflate the layout for this fragment
        return binding.root
    }

}