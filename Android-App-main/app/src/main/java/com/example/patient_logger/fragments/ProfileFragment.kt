package com.example.patient_logger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction


import com.example.patient_logger.CommDataSensorManagement

import com.example.patient_logger.R

class ProfileFragment : Fragment(), CommDataSensorManagement{





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       return inflater.inflate(R.layout.fragment_profile, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        insertNestedFragment()

    }
    val sensorManagementFragment: Fragment = sensorManagementFragment()
    val sensorManagementFragment1: Fragment = sensorManagementFragment()
    val sensorManagementFragment2: Fragment = sensorManagementFragment()

    private fun insertNestedFragment() {
        val headingFragment: Fragment = ChildFragment()
        val formFragment: Fragment = PatientFormFragment()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.sensor_management_container, sensorManagementFragment)
        transaction.replace(R.id.profile_page_heading, headingFragment)
        transaction.replace(R.id.profile_patient_form, formFragment)
        transaction.replace(R.id.sensor_management_container1, sensorManagementFragment1)
        transaction.replace(R.id.sensor_management_container2, sensorManagementFragment2)


        transaction.commit()
    }
    override fun passDataCom(sensor1: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("message", sensor1)

        val transaction = this.childFragmentManager.beginTransaction()
        sensorManagementFragment.arguments = bundle

        transaction.commit()

    }
}

