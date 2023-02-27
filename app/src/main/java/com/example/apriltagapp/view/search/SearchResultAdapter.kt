package com.example.apriltagapp.view.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apriltagapp.databinding.SearchResultBinding
import com.example.apriltagapp.listener.SearchResultClickListener

class SearchResultAdapter(private val searchResult: MutableList<Pair<String, Int>>, val searchResultClickListener: SearchResultClickListener): RecyclerView.Adapter<SearchResultAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = SearchResultBinding.inflate(LayoutInflater.from(parent.context))
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(searchResult[position])
    }

    override fun getItemCount(): Int {
        return searchResult.size
    }

    inner class Holder(val binding: SearchResultBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(spot: Pair<String, Int>) {
            binding.txtName.text = spot.first // name
            binding.txtName.setOnClickListener {
                searchResultClickListener.onSearchResultClicked(spot.second) // id
            }
        }
    }
}