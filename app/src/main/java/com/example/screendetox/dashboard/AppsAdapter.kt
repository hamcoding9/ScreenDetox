package com.example.screendetox.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.App

class AppsAdapter(private val appsList: ArrayList<App>) :
    RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_app,
            parent, false
        )
        return AppViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val currentItem = appsList[position]
        holder.app_name.text = currentItem.appName
        holder.app_icon_img.setImageDrawable(currentItem.appIcon)
        holder.app_usage_duration.text = currentItem.usageDuration
        holder.app_progressBar.progress = currentItem.usagePercentage
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val app_name: TextView = itemView.findViewById(R.id.app_name_tv)
        val app_usage_duration: TextView = itemView.findViewById(R.id.usage_duration_tv)
        val app_icon_img: ImageView = itemView.findViewById(R.id.icon_img)
        val app_progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }
}