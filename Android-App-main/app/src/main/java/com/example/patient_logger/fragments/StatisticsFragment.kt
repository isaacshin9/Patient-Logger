package com.example.patient_logger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentTransaction
import com.example.patient_logger.R
import kotlinx.android.synthetic.main.fragment_statistics.view.*

/**
 * A simple [Fragment] subclass.
 * Use the [StatisticsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class StatisticsFragment : Fragment() {




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)



        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        insertNestedFragment()

        val button = view?.findViewById<Button>(R.id.button3)
        button.setOnClickListener() {

            val chartFormFragment: Fragment = chartFragment()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.stats_page_chart, chartFormFragment)

            transaction.commit()
        }

        val button4 = view?.findViewById<Button>(R.id.button4)
        button4.setOnClickListener() {

            val barGraphFragment: Fragment = BarGraphFragment()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.stats_page_chart, barGraphFragment)

            transaction.commit()
        }

        val button5 = view?.findViewById<Button>(R.id.button5)
        button5.setOnClickListener() {

            val comboGraphFragment: Fragment = ComboGraphFragment()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.stats_page_chart, comboGraphFragment)

            transaction.commit()
        }
    }

    val chartFragment: Fragment = chartFragment()


    private fun insertNestedFragment() {
        val headingFragment: Fragment = ChildFragment()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        //transaction.replace(R.id.stats_page_chart, chartFormFragment)
        transaction.replace(R.id.stats_page_chart, chartFragment)
        transaction.replace(R.id.stats_page_heading, headingFragment)
        transaction.commit()
    }



}