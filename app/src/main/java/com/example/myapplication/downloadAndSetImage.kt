package com.example.myapplication

import android.widget.ImageView
import com.squareup.picasso.Picasso


fun ImageView.downloadAndSetImage(url: String) {
    /* Функция раширения ImageView, скачивает и устанавливает картинку*/
    Picasso.get()
        .load(url)
        .into(this)
}
