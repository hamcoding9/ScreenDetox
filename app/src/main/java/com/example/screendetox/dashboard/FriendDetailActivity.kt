package com.example.screendetox.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.screendetox.databinding.ActivityFriendDetailBinding

class FriendDetailActivity : AppCompatActivity() {

    // 뷰 바인딩
    private lateinit var binding: ActivityFriendDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("userID") as String
        val userTotalTime = intent.getStringExtra("userTotalTime") as String
        val userMostUsedApp = intent.getStringExtra("userMostUsedApp") as String

        binding.nameTv.text = userId
        binding.durationTv.text = userTotalTime
        binding.categoryTv.text = userMostUsedApp
    }
}