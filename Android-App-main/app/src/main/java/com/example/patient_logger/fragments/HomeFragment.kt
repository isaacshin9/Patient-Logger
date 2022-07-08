package com.example.patient_logger.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.patient_logger.R


class HomeFragment : Fragment() {

    var displayMessage: Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    var pain = 0
    var symptoms = ""
    var patientName: String? = "none"
    var patientId: Int? = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        insertNestedFragment()

        displayMessage = arguments?.getBoolean("message")

        val painbutton = view.findViewById<Button>(R.id.PainButton)
        val patientinput = view.findViewById<EditText>(R.id.patientInput)


        if (displayMessage == true) {
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.hide(sensorFragment)

            transaction.commit()
        } else {
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.show(sensorFragment)

            transaction.commit()
        }
        childFragmentManager.setFragmentResultListener("painLevel", this) { requestKey, bundle ->
            pain = bundle.getInt("painData")
            println("patient pain now set to $pain")
        }

        painbutton.setOnClickListener {

            println("patient writes " + patientinput.text)
            println("patient reports pain level of: $pain")
            println("patient reports symptoms: $symptoms")

            /*
            This OnClickListener needs to be the button that takes the sensor data and saves it. As of now, it takes all of the patient input, and prints it out to a textbox
            But this can be changed to push values, pain, symptoms, Name, Id, as well as patient input to the backend
             */
        }
    }


    val sensorFragment: Fragment = SensorFragment()


    private fun insertNestedFragment() {
        val childFragment: Fragment = ChildFragment()
        val painFragment: Fragment = PainFragment()
        val sensorFragment1: Fragment = SensorFragment()
        val sensorFragment2: Fragment = SensorFragment()
        val scrollerFragment: Fragment = SymptomScrollerFragment()

        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()

        transaction.replace(R.id.child_fragment_container, childFragment)
        transaction.replace(R.id.sensor_fragment_container, sensorFragment)
        transaction.replace(R.id.sensor_fragment_container1, sensorFragment1)
        transaction.replace(R.id.sensor_fragment_container2, sensorFragment2)

        transaction.replace(R.id.pain_fragment_container, painFragment)
        transaction.replace(R.id.horizontal_scroll, scrollerFragment)

        transaction.commit()
    }

}