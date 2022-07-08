package com.example.patient_logger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jjoe64.graphview.series.LineGraphSeries

import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint


class chartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(com.example.patient_logger.R.layout.fragment_chart, container, false)

        val graph = view?.findViewById(com.example.patient_logger.R.id.graph) as GraphView
        val series: LineGraphSeries<DataPoint> = LineGraphSeries(
            arrayOf(
                DataPoint(1.0, 2.1),
                DataPoint(1.5,2.8),
                DataPoint(2.0,2.8)





            )
        )
        graph.addSeries(series)
        graph.setTitle("Posture Vs. Time Graph")
        graph.titleTextSize = 25F

        // Inflate the layout for this fragment
        return view
    }

}