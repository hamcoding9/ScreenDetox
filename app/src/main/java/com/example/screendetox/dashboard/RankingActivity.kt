package com.example.screendetox.dashboard

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.User
import com.example.screendetox.data.appNameMap
import com.example.screendetox.databinding.ActivityRankingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
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
    private lateinit var followList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 네비게이션 탭바 터치에 따른 액티비티 이동
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

        // 친구 추가 화면으로 이동
        binding.friendAddBtn.setOnClickListener {
            startActivity(Intent(this, FriendAddActivity::class.java))
        }

        // 초기 설정 : 닉네임
        userDB = Firebase.database.reference.child("Users")
        val currentUserDB = userDB.child(getCurrentUserID())

        // 닉네임 설정
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("userName").value == null) {
                    showNameInputPopup()
                    return
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        loadTodayDate()
        loadStatistics()
        loadFollowList()
        loadUsers()
    }

    private fun loadFollowList() {
        followList = arrayListOf<String>()
        userDB = Firebase.database.reference.child("Users")
        val followingDB = userDB.child(getCurrentUserID()).child("following")
        followingDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    //followList.clear()
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key
                        followList.add(userId!!)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadTodayDate() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        binding.rankingDateTv.text = formatted
    }

    private fun loadStatistics() {
        val usm = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val midnight : Long = (System.currentTimeMillis() / 86400000) * 86400000 - (9 * 3600000)
        Log.i("timeStamp", "${midnight}")
        Log.i("timeStamp", "${System.currentTimeMillis()}")
        // 오늘 0시를 기준으로 현재까지의 사용 통계 받아오기
        var appList = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            midnight,
            System.currentTimeMillis())
        // 지난 24시간 동안 사용한 어플리케이션(사용 시간 > 0인 것만 filter)만 불러오기
        // totalTimeInForeground: Get the total time this package spent in the foreground, measured in milliseconds.
        appList = appList.stream().filter{ app: UsageStats -> app.totalTimeInForeground > 0}
            .collect(Collectors.toList())
        // 내 사용 기록 불러 왔으니 나의 사용 기록을 DB에 업데이트
        saveAppUsage(appList)
    }

    private fun getTimeSpent(): HashMap<String, Int?> {
        val usageStatsManager = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        // today 기준 12am 불러오기
        val beginTime: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 14)
        }
        var currentEvent: UsageEvents.Event
        val allEvents: MutableList<UsageEvents.Event> = ArrayList()
        val appUsageMap: HashMap<String, Int?> = HashMap()
        // 12am 부터 현재 시간까지 모든 usageEvents 불러오기
        val usageEvents = usageStatsManager.queryEvents(beginTime.timeInMillis, System.currentTimeMillis())
        while (usageEvents.hasNextEvent()) {
            currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
//            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED)
            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                allEvents.add(currentEvent)
                val key = currentEvent.packageName
                if (appUsageMap[key] == null) appUsageMap[key] = 0
            }
        }
        for (i in 0 until allEvents.size - 1) {
            val e0 = allEvents[i]
            val e1 = allEvents[i + 1]
//            if (e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED && e1.eventType == UsageEvents.Event.ACTIVITY_PAUSED && e0.className == e1.className)
            if (e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED && e0.className == e1.className) {
                var diff = (e1.timeStamp - e0.timeStamp).toInt()
                diff /= 1000
                var prev = appUsageMap[e0.packageName]
                if (prev == null) prev = 0
                appUsageMap[e0.packageName] = prev + diff
            }
        }
        val lastEvent = allEvents[allEvents.size - 1]
        if (lastEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
            var diff = System.currentTimeMillis().toInt() - lastEvent.timeStamp.toInt()
            diff /= 1000
            var prev = appUsageMap[lastEvent.packageName]
            if (prev == null) prev = 0
            appUsageMap[lastEvent.packageName] = prev + diff
        }
        return appUsageMap
    }

    private fun saveAppUsage(appList: MutableList<UsageStats>) {
        val usageTotaltime = getTotalTime(appList)
        val mostUsedApp = getMostUsedApp(appList)
        // DB와 연결
        var userDB = Firebase.database.reference.child("Users")
        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
       // DB에 UserInfo 저장하기
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["totalTime"] = usageTotaltime
        user["mostUsedApp"] = mostUsedApp
        currentUserDB.updateChildren(user)
    }

    private fun getMostUsedApp(appList: MutableList<UsageStats>): String {
        var appList = appList.sortedBy { it.totalTimeInForeground }
        val mostAppStats = appList.last()
        val packageName = mostAppStats.packageName
        var appName = packageName.split(".").last()
        if (appNameMap.contains(packageName)) {
            appName = appNameMap[packageName]!!
        }
        return appName
    }

    private fun getTotalTime(appList: MutableList<UsageStats>): Long {
        // 전체 사용 시간 구하기
        val totalTime = appList.stream().map {obj:UsageStats -> obj.totalTimeInForeground }.mapToLong{obj: Long -> obj}.sum()
        // 전체 사용 시간을 @시간@분@초 형태로 바꾸기
        return totalTime
    }

    private fun loadUsers() {
        userList = arrayListOf<User>()
        // 파이어베이스 연동
        userDB = Firebase.database.reference.child("Users")
        // DB에 있는 값을 UserList에 담는다
        userDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    userList.clear()
                    for (userSnapshot in snapshot.children){
                        val user = userSnapshot.getValue(User::class.java)
                        if (followList.contains(user!!.userId)) {
                            userList.add(user)
                        }
                        if (user!!.userId == getCurrentUserID()) {
                            userList.add(user)
                        }
                    }
                    adapter = UserAdapter()
                    recyclerView.adapter = adapter
                    adapter.submitList(userList)
                    // user간 구분선 추가
                    //val decoration = DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)
                    //recyclerView.addItemDecoration(decoration)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun isAppInfoAvailable(usageStats: UsageStats): Boolean {
        return try{
            applicationContext.packageManager.getApplicationInfo(usageStats.packageName, 0)
            true
        }
        catch (e: PackageManager.NameNotFoundException){
            false
        }
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
        return "$hours 시간 $minutes 분"
    }

    private fun showNameInputPopup() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.write_name))
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                if (editText.text.isEmpty()) {
                    showNameInputPopup()
                } else {
                    saveUserName(editText.text.toString())
                }
            }
            .setCancelable(false)
            .show()
    }

/*    private fun showGoalInputPopup() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.write_goal))
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                if (editText.text.isEmpty()) {
                    showNameInputPopup()
                } else {
                    saveGoalTime(editText.text.toString())
                }
            }
            .setCancelable(false)
            .show()
    }*/

    private fun saveUserName(name: String) {
        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["userName"] = name
        currentUserDB.updateChildren(user)
    }

    private fun saveGoalTime(goal: String) {
        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["goalTime"] = goal.toLong()
        currentUserDB.updateChildren(user)
    }
}