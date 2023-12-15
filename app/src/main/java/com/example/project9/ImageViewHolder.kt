package com.example.project9

import  android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project9.PhotosViewModel
import com.example.project9.databinding.ItemImageBinding

class ImageViewHolder(private val binding: ItemImageBinding,
                      private val navController: NavController) :
    RecyclerView.ViewHolder(binding.root) {



    fun bind(image: PhotosViewModel.Image) {
        binding.apply {
            Glide.with(this.iV)
                .load(image.url)
                .into(this.iV)
        }




    }
}