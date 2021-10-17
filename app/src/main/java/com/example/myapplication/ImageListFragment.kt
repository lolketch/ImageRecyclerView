package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.model.ApiResponse
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import android.graphics.drawable.Drawable
import android.widget.Button
import android.widget.TextView

import androidx.annotation.NonNull
import androidx.annotation.Nullable

import com.bumptech.glide.request.target.CustomTarget
import kotlinx.coroutines.*


class ImageListFragment:Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //вынести в instance
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerPersons)
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
                val linearLayoutManager:LinearLayoutManager = LinearLayoutManager(context)
                val data: List<ApiResponse>? = response.body()
                val imageAdapter = data?.let { ImageAdapter(it,context!!) }
                recyclerView.adapter = imageAdapter
                recyclerView.layoutManager = linearLayoutManager
            }

            override fun onFailure(call: Call<List<ApiResponse>>, t: Throwable) {
                println("ERROR " + t.message)
            }
        })
    }
}

class ImageAdapter(private val results: List<ApiResponse>, val context:Context):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val HEADER_ITEM = 0
    private val NORMAL_ITEM = 1
    lateinit var mRecyclerView:RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView;
        recyclerView.layoutManager
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER_ITEM else NORMAL_ITEM
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view:View
        return if (viewType == HEADER_ITEM) {
            view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_header_list, viewGroup, false)
            HeaderViewHolder(view)
        }else{
            view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_list, viewGroup, false)
            DefaultViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == HEADER_ITEM) {
            val viewHolder = holder as HeaderViewHolder
            Glide
                .with(context)
                .load(results[position].picture)
                .into(viewHolder.photo)
            viewHolder.btn_next.setOnClickListener {
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
                        .into(viewHolder.photo)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return results.size
    }

    inner class DefaultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.photo)
    }
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.header_photo)
        val btn_next: Button = itemView.findViewById(R.id.btn_nextItem)
    }

}
