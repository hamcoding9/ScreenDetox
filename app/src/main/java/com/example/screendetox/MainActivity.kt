package com.example.screendetox

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // main layout view 불러오기
    var usersList: ListView? = null
    var enableBtn: Button? = null
    var permissionTv: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usersList = findViewById(R.id.users_list) // 유저 리스트
        enableBtn = findViewById(R.id.enable_btn) // permission enable 버튼
        permissionTv = findViewById(R.id.permission_tv) // permission text
        loadStatistics() // 사용자들의 사용 시간을 불러오는 함수
    }

    // 어플을 처음 실행시켰을 때, permission 되어 있지 않으면 user permission setting 화면으로 넘어감
    override fun onStart()
    {
        super.onStart()
        if(grantStatus){ // permission 허용되어 있으면
            showUsersList() // 유저 리스트 띄우기
        }
        else{
            showPermission() // permission 띄우기
            enableBtn!!.setOnClickListener {view: View? -> startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))}
        }
    }

    // 지난 24시간 동안의 유저들의 사용 시간을 보여줌.
    fun loadStatistics()
    {

    }
}

