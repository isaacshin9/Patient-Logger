package com.example.patient_logger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult

import com.example.patient_logger.R
import kotlinx.android.synthetic.main.fragment_pain.view.*


class PainFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pain, container, false)

        return view

    }
        var pointRecorded = 0
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            val slide = view.findViewById<SeekBar>(R.id.seekBarDistance)

            slide.seekBarDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (slide.seekBarDistance != null) {
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    //here we can write some code to do something whenever the user touche the seekbar
                    if (slide.seekBarDistance != null) {
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // show some message after user stopped scrolling the seekbar
                    if (slide.seekBarDistance != null) {
                        pointRecorded = seekBar.progress
                        println("user set pain to $pointRecorded")
                        setFragmentResult("painLevel", bundleOf("painData" to pointRecorded))
                    }
                }
            })
        }


}


