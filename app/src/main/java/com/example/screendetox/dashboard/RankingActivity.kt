package com.example.screendetox.dashboard

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.User
import com.example.screendetox.databinding.ActivityRankingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

// 랭킹 대시보드 Activity
class RankingActivity : AppCompatActivity() {
    // 로그인 정보
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    // 다른 사람의 사용 시간을 알아야 하므로 userDB 필요
    private lateinit var userDB: DatabaseReference
    // 뷰 바인딩
    private lateinit var binding: ActivityRankingBinding

    // recycler view
    private lateinit var adapter : UserAdapter
    private lateinit var recyclerView : RecyclerView
    // user 정보를 담아서 adapter에 전달할 userList
    private lateinit var userList : ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 네비게이션 탭바 터치에 따른 액티비티 이동
        //Use NavigationBarView.setOnItemReselectedListener(NavigationBarView.OnItemReselectedListener) instead.
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.ranking -> {
                }
                R.id.stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
                    overridePendingTransition(0,0)
                }
                else -> {
                }
            }
            true
        }
        recyclerView = binding.usersRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart(){
        super.onStart()
        loadStatistics()
        loadUsers()
    }

    private fun loadStatistics() {
        val usm = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        var appList = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 2600 * 24,
            System.currentTimeMillis())
        // 지난 24시간 동안 사용한 어플리케이션(사용 시간 > 0인 것만 filter)만 불러오기
        // totalTimeInForeground: Get the total time this package spent in the foreground, measured in milliseconds.
        appList = appList.stream().filter{ app: UsageStats -> app.totalTimeInForeground > 0}
            .collect(Collectors.toList())
        // 내 사용 기록 불러 왔으니 나의 사용 시간을 DB에 업데이트
        saveAppUsage(appList)
    }

    private fun saveAppUsage(appList: MutableList<UsageStats>) {
        // 전체 사용 시간 구하기
        val totalTime = appList.stream().map {obj:UsageStats -> obj.totalTimeInForeground }.mapToLong{obj: Long -> obj}.sum()
        // 전체 사용 시간을 @시간@분@초 형태로 바꾸기
        val usageTotaltime = getDurationBreakdown(totalTime)
        // 전체 사용 시간(string) DB에 저장하기
        var userDB = Firebase.database.reference.child("Users")
        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        // DB에 업데이트
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["totalTime"] = usageTotaltime
        currentUserDB.updateChildren(user)
    }

    private fun loadUsers() {
        userList = arrayListOf<User>()
        // 파이어베이스 연동
        userDB = Firebase.database.reference.child("Users")
        // DB에 있는 값을 UserList에 담는다
        userDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val user = userSnapshot.getValue(User::class.java)
                        userList.add(user!!)
                    }
                    adapter = UserAdapter(userList)
                    recyclerView.adapter = adapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // User ID 가져오는 함수
    // login 되어 있지 않으면 다시 login 화면으로, login 되어 있으면 id return
    private fun getCurrentUserID(): String {
        if (auth.currentUser == null){
            Toast.makeText(this, "로그인이 되어있지않습니다.", Toast.LENGTH_SHORT).show()
            // 다시 로그인 화면으로 돌아옴
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }

    private fun getDurationBreakdown(millis:Long): String{
        var millis = millis
        require(millis >= 0) { " Duration must be greater than zero! "}
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        return "$hours 시간 $minutes 분 $seconds 초"
    }
}