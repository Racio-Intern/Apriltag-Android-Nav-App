package com.example.apriltagapp.view.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apriltagapp.databinding.FragmentSearchBinding
import com.example.apriltagapp.listener.SearchResultClickListener

class SearchFragment : Fragment(), SearchResultClickListener {

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

        binding?.viewResult?.layoutManager = LinearLayoutManager(context)
        binding?.viewResult?.adapter = SearchResultAdapter(spotPairList, this)

        val queryTextListener = object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.apply {
                    spotNameList.clear()
                    for(name in viewModel.onSearchTextChanged(newText)) {
                        spotNameList.add(name)
                    }
                    binding?.viewResult?.adapter?.notifyDataSetChanged()
                }
                return false
            }

        }

        binding?.viewSearch?.setOnQueryTextListener(queryTextListener)



        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onSearchResultClicked(spotId: Int) {
        val action = SearchFragmentDirections.actionSearchFragmentToCameraFragment(spotId)
        findNavController().navigate(action)
    }

}