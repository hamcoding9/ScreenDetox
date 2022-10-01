package com.example.screendetox

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.stream.Collectors

class MainActivity : AppCompatActivity() {

    // main layout view 불러오기
    var usersList: ListView? = null
    var enableBtn: Button? = null
    var permissionTv: TextView? = null

    // 로그인 정보
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usersList = findViewById(R.id.users_list) // 유저 리스트
        enableBtn = findViewById(R.id.enable_btn) // permission enable 버튼
        permissionTv = findViewById(R.id.permission_tv) // permission text
        loadStatistics() // 사용자들의 사용 시간을 불러오는 함수
    }

    // 어플을 처음 실행시켰을 때, permission 되어 있지 않으면 user permission setting 화면으로 넘어감
    // 어플을 처음 실행시켰을 때, login 되어 있지 않으면 login 화면으로 넘어감
    override fun onStart()
    {
        super.onStart()
        // login
        if (auth.currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // permission
        if(grantStatus){ // permission 허용되어 있으면
            showUsersList() // 유저 리스트 띄우기
        }
        else{
            showPermission() // permission 띄우기
            enableBtn!!.setOnClickListener {view: View? -> startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))}
        }
    }

    // 지난 24시간 동안의 유저들의 사용 시간을 불러오는 함수 (List에 담기)
    fun loadStatistics()
    {
        val usm = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

        // queryUsageStats(intervalType, beginTime, endTime) : MutableList<UsageStats!>!
        // 지난 24시간 동안의 현재 사용자의 어플 사용 기록을 appList에 저장한다
        var appList = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 2600 * 24,
            System.currentTimeMillis())

        // 지난 24시간 동안 사용한 어플리케이션(사용 시간 > 0인 것만 filter)만 불러오기
        // totalTimeInForeground: Get the total time this package spent in the foreground, measured in milliseconds.
        appList = appList.stream().filter{ app: UsageStats -> app.totalTimeInForeground > 0}
            .collect(Collectors.toList())
        
        // Group the usageStats by application and sort them by total time in foreground
        if (appList.size > 0){
            val mySortedMap: MutableMap<String, UsageStats> = TreeMap()
            for (usageStats in appList) {
                mySortedMap[usageStats.packageName] = usageStats
            }
            showAppsUsage(mySortedMap)
        }
    }
    // 유저들의 사용 시간을 화면에 보이게 함
    // View에 Data 전달

    fun showAppUsage(mySortedMap: Map<String, UsageStats>){
        val UsersList = ArrayList<User?>()
        val usageStatsList: List<UsageStats> = ArrayList(mySortedMap.values) // UsageStats만 모아서 list

        // 전체 시간 구하기
        val totalTime = usageStatsList.stream().map {obj: UsageStats -> obj.totalTimeInForeground }
            .mapToLong {obj: Long -> obj}.sum()

        // UsersList 채우기
    }
}

