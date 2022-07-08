package com.example.patient_logger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.example.patient_logger.CommDataSensorManagement
import com.example.patient_logger.R
import kotlinx.android.synthetic.main.fragment_sensor_management.*


class sensorManagementFragment : Fragment() {

    private lateinit var communicator: CommDataSensorManagement

    var displayMessage: Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sensor_management, container, false)

        communicator = activity as CommDataSensorManagement


        val fragmentText = view?.findViewById<Switch>(R.id.switch2)


        fragmentText?.setOnClickListener {
            if (switch2.isChecked) {
                communicator.passDataCom(true)
                displayMessage = true
            } else {
                communicator.passDataCom(false)
                displayMessage = false
            }
        }


        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }




}