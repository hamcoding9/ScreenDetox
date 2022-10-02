package com.example.screendetox

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

class MainActivity : AppCompatActivity() {

    // Permission 관련 View 불러오기
    var enableBtn: Button? = null
    var permissionTv: TextView? = null

    // 로그인 정보
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableBtn = findViewById(R.id.enable_btn) // permission enable 버튼
        permissionTv = findViewById(R.id.permission_tv) // permission text
    }

    // 1. 어플을 처음 실행시켰을 때, permission 되어 있지 않으면 user permission setting 화면으로 넘어감
    // 2. 어플을 처음 실행시켰을 때, login 되어 있지 않으면 login 화면으로 넘어감
    override fun onStart()
    {
        super.onStart()
        // permission
        if(grantStatus){ // permission 허용되어 있으면
            showHideWithPermission() // Permission 화면 숨기기
            // login 실패: 로그인 / 회원가입
            // login 성공: group activity 불러오기
            if (auth.currentUser == null){
                startActivity(Intent(this, LoginActivity::class.java))
            }
            else {
                loadStatistics() // 사용자의 사용 시간을 불러오는 함수
                startActivity(Intent(this, GroupActivity::class.java))
            }
        }

    }

    // 지난 24시간 동안의 유저의 사용 시간을 불러오는 함수 (List에 담기)
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
            // 내 사용기록을 불러 왔으니 DB에 저장
            saveAppsUsage(mySortedMap)
        }
    }

    // 전체 시간(int) DB에 저장하기
    private fun saveAppsUsage(mySortedMap: Map<String, UsageStats>){
        val usageStatsList: List<UsageStats> = ArrayList(mySortedMap.values) // UsageStats만 모아서 list

        // 전체 시간 구하기
        val totalTime = usageStatsList.stream().map {obj: UsageStats -> obj.totalTimeInForeground }
            .mapToLong {obj: Long -> obj}.sum()
        // 전체 시간 @@H @@M @@S 형태로 바꾸기
        val usageTotaltime = getDurationBreakdown(totalTime)
        // 전체 시간(string) DB에 저장하기
        var userDB = Firebase.database.reference.child("Users")
        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["totalTime"] = usageTotaltime
        currentUserDB.updateChildren(user)
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

/*
* check if PACKAGE_USAGE_STATS permission is allowed for this application
* @return true if permission granted
* */
    private val grantStatus: Boolean
        private get() {
            val appOps = applicationContext.getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), applicationContext.packageName
            )
            return if (mode == AppOpsManager.MODE_DEFAULT) {
                applicationContext.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
            } else{
                mode == AppOpsManager.MODE_ALLOWED
            }
        }

    /**
     * helper method used to show/hide items in the view when PACKAGE_USAGE_STATS permission is not allowed
     */
    // Permission 화면 보여주기
    fun showHideNoPermission() {
        enableBtn!!.visibility = View.VISIBLE
        permissionTv!!.visibility = View.VISIBLE
    }

    /**
     * helper method used to show/hide items in the view when  PACKAGE_USAGE_STATS permission allowed
     */
    // Permission 화면 숨기기
    fun showHideWithPermission() {
        enableBtn!!.visibility = View.GONE
        permissionTv!!.visibility = View.GONE
    }

    /**
     * helper method to get string in format hh:mm:ss from miliseconds
     * @param millis (application time in foreground)
     * @return string in format hh:mm:ss from miliseconds
     */
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