package com.example.screendetox.dashboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.User
import com.example.screendetox.databinding.ItemUserBinding
import java.util.concurrent.TimeUnit

class UserAdapter : ListAdapter<User, UserAdapter.ViewHolder>(diffUtil){
    inner class ViewHolder(var binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: User){
            binding.nameTv.text = item.userName
            binding.durationTv.text = getDurationBreakdown(item.totalTime)

            //totalTime이 goalTime(현재는 default: 3Hours) 넘었으면 색 바꾸기
            if (item.totalTime!! > 3 * 3600000){
                binding.durationTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
            }

            itemView.setOnClickListener {
                Intent(itemView.context, FriendDetailActivity::class.java).apply {
                    putExtra("userID", item.userName)
                    putExtra("userTotalTime", getDurationBreakdown(item.totalTime))
                    putExtra("userMostUsedApp", item.mostUsedApp)
                }.run { itemView.context.startActivity(this) }
            }
        }
    }

    private fun getDurationBreakdown(millis:Long?): String{
        var millis = millis!!
        require(millis >= 0) { " Duration must be greater than zero! "}
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        return "$hours 시간 $minutes 분"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<User>(){
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
}