package com.example.project9

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project9.databinding.FragmentPhotosBinding
import com.example.project9.databinding.ItemImageBinding
import kotlin.math.sqrt


class PhotosFragment : Fragment() {
    private val TAG = "Photos Fragment"
    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!
    private var lastShakeTime: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        val view = binding.root
        val viewModel : PhotosViewModel by activityViewModels()
        val storageRef = viewModel.imagesRef
        binding.lifecycleOwner = viewLifecycleOwner

        if(!viewModel.isUserLoggedIn()){
            val action = PhotosFragmentDirections
                .actionPhotosFragmentToSignInFragment()
            this.findNavController().navigate(action)
            viewModel.onNavigatedToSignIn()
        }

        storageRef?.listAll()?.addOnSuccessListener {
            val images = it.items.map{
                PhotosViewModel.Image(it.name, it.downloadUrl)
            }

            val adapter = PhotoAdapter(images)
            binding.recyclerView.adapter = adapter
            viewModel.images.observe(viewLifecycleOwner) { images ->
                adapter.submitList(images)
            }

        }



        val sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (isShakeDetected(event)) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > 1000){
                        lastShakeTime = now
                        val navController = NavHostFragment.findNavController(this@PhotosFragment)
                        navController.navigate(R.id.action_photosFragment_to_cameraFragment)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Accuracy: $accuracy")
            }
        }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        return view
    }

    private fun isShakeDetected(event: SensorEvent): Boolean {

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val ACCELERATION_THRESHOLD = 5
        val minAcceleration = 10
        val acceleration = sqrt((x*x + y*y + z*z).toDouble())
        // Return true if acceleration exceeds threshold
        return acceleration > ACCELERATION_THRESHOLD && acceleration > minAcceleration
    }

    class PhotoAdapter(private val images: List<PhotosViewModel.Image>) : ListAdapter<PhotosViewModel.Image,PhotoAdapter.ViewHolder>(object : DiffUtil.ItemCallback<PhotosViewModel.Image>(){

        override fun areItemsTheSame(oldItem: PhotosViewModel.Image, newItem: PhotosViewModel.Image) =
            oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: PhotosViewModel.Image, newItem: PhotosViewModel.Image) =
            oldItem == newItem
    }) {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(image: PhotosViewModel.Image) {
                val imageView = itemView.findViewById<ImageView>(R.id.iV)
                // Load image into ImageView using Glide
                Glide.with(imageView)
                    .load(image.url)
                    .into(imageView)
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            // Inflate view
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)

            // Return ViewHolder
            return ViewHolder(view)

        }



        override fun getItemCount() = images.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val image = images[position]
            holder.bind(image)
        }

    }

}