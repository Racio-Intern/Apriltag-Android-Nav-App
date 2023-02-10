package com.example.apriltagapp.view.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.apriltagapp.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    companion object {
        const val DEFAULT_DESTINATION_ID = -1
    }

    private var binding: FragmentSearchBinding? = null
    private val viewModel: SearchViewModel by viewModels()
    private var destinationId: Int = DEFAULT_DESTINATION_ID


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)

        val spotPairList = arrayListOf<Pair<String, Int>>()
        val spotNameList = arrayListOf<String>()
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, spotNameList)

        viewModel.spots.observe(viewLifecycleOwner) { spots->
            spotNameList.clear()
            spotPairList.clear()
            for(spot in spots) {
                spotNameList.add(spot.key)
                spotPairList.add(Pair(spot.key, spot.value))
            }
            arrayAdapter.notifyDataSetChanged()
        }


        val destSpinner = binding?.spnDest?.apply {
            adapter = arrayAdapter
            this.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    destinationId = spotPairList[position].second
                    println("목적지 : ${spotPairList[position].first}")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
        }

        binding?.btnStartNavi?.setOnClickListener {
            val action = SearchFragmentDirections.actionSearchFragmentToCameraFragment(destinationId)
            findNavController().navigate(action)
        }



        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}