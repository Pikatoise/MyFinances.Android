package com.example.myfinances.lists

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.myfinances.R
import com.example.myfinances.api.models.operationType.OperationTypeResponse

class TypeAdapter(
    context: Context,
    data: List<OperationTypeResponse>,
    val clickCallback: (position: Int) -> Unit) :
    ArrayAdapter<OperationTypeResponse?>(context, R.layout.type_item, data) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var currentView = view
        val item = getItem(position)!!

        if (currentView == null) {
            currentView = LayoutInflater.from(context).inflate(R.layout.type_item, parent, false)
        }

        val itemImage = currentView!!.findViewById<ImageView>(R.id.iv_item_type_icon)
        val itemButton = currentView.findViewById<TextView>(R.id.button_item_type)

        val iconPath = "https://api.myfinances.tw1.ru/images/${item.iconSrc}"
        itemImage.loadSvg(iconPath)
        itemButton.setOnClickListener {
            clickCallback(position)
        }

        return currentView
    }

    fun ImageView.loadSvg(url: String) {
        val imageLoader = ImageLoader.Builder(this.context)
            .componentRegistry { add(SvgDecoder(this@loadSvg.context)) }
            .build()

        val request = ImageRequest.Builder(this.context)
            .crossfade(true)
            .crossfade(500)
            .data(url)
            .target(this)
            .build()

        imageLoader.enqueue(request)
    }
}