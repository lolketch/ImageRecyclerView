package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.model.ApiResponse
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.example.myapplication.databinding.FragmentImageListBinding
import com.example.myapplication.databinding.ItemHeaderListBinding
import com.example.myapplication.databinding.ItemListBinding
import kotlinx.coroutines.*

class ImageListFragment:Fragment() {

    private  lateinit var binding: FragmentImageListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentImageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //вынести в instance
        val recyclerView = binding.recyclerPersons
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://picsum.photos/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val requestInterface = retrofit.create(InterfaceAPI::class.java)
        val call: Call<List<ApiResponse>> = requestInterface.getImageJson()
        call.enqueue(object : Callback<List<ApiResponse>> {
            override fun onResponse(
                call: Call<List<ApiResponse>>,
                response: Response<List<ApiResponse>>
            ) {
                val data: List<ApiResponse>? = response.body()
                val imageAdapter = data?.let { ImageAdapter(it) }
                recyclerView.adapter = imageAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)
            }

            override fun onFailure(call: Call<List<ApiResponse>>, t: Throwable) {
                println("ERROR " + t.message)
            }
        })
    }
}

class ImageAdapter(private val results: List<ApiResponse>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val HEADER_ITEM = 0
    private val NORMAL_ITEM = 1
    lateinit var mRecyclerView:RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER_ITEM else NORMAL_ITEM
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER_ITEM) {
            HeaderViewHolder(ItemHeaderListBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false))
        }else{
            DefaultViewHolder(ItemListBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == HEADER_ITEM) {
            val viewHolder = holder as HeaderViewHolder
            Glide
                .with(viewHolder.itemView)
                .load(results[position].picture)
                .into(viewHolder.binding.headerPhoto)
            viewHolder.binding.btnNextItem.setOnClickListener {
                val linearLayoutManager = mRecyclerView.layoutManager as LinearLayoutManager
                linearLayoutManager.scrollToPositionWithOffset(1,0)
            }
        } else {
            val viewHolder = holder as DefaultViewHolder
            val job = Job()
            val uiScope = CoroutineScope(Dispatchers.Main + job)
            uiScope.launch(Dispatchers.IO){
                withContext(Dispatchers.Main){
                    Glide
                        .with(viewHolder.itemView)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(50)))
                        .placeholder(R.drawable.ic_plug)
                        .load(results[position].picture)
                        .into(viewHolder.binding.photo)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return results.size
    }

    inner class DefaultViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)
    inner class HeaderViewHolder(val binding: ItemHeaderListBinding) : RecyclerView.ViewHolder(binding.root)

}
