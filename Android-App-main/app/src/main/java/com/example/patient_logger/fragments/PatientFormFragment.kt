package com.example.patient_logger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.patient_logger.R


class PatientFormFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val firstname = view.findViewById<EditText>(R.id.editFirstName)
        val button = view.findViewById<Button>(R.id.loginButton)

        button.setOnClickListener {
            println("LOGIN PRESSED")
            var result = firstname.text.toString()

            println("name is $result being sent to main right now")

            setFragmentResult("patientName", bundleOf("name" to result))

        }

    }



}