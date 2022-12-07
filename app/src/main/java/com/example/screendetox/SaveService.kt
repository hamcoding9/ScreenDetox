package com.example.screendetox

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.Configuration
import com.example.screendetox.dashboard.RankingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.stream.Collectors

class SaveService : JobService() {
    private lateinit var followList : ArrayList<String>
    companion object {
        private const val TAG = "SaveService"
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        val nameMap = mapOf(
            "1oHkW3eLkSOJUlruISg6PKi2vBn1" to "코니 태블릿",
            "sFyK7vZ4u1PHeGmrsrw48n4nFOs2" to "브라운",
            "t4SjbXWQr7fuxGaxUkXdgBxjvw02" to "코니",
        )
    }

    init {
        Configuration.Builder().setJobSchedulerJobIdRange(0, 1000).build()
    }

    private var jobCanceled: Boolean = false

    private fun doBackgroundWork(p0: JobParameters?) {
        Thread(Runnable {
            kotlin.run {
                saveUserDuration()
                loadFollowList()
                show()
                Log.d(TAG, "Job finish")
                jobFinished(p0, false)
            }
        }).start()
    }

    private fun showOverUser() {
        val userDB = Firebase.database.reference.child("Users")
        for (userId in followList) {
            val userOverDB = userDB.child(userId).child("isOver")
            userOverDB.addChildEventListener(object : ChildEventListener {
                override fun onChildRemoved(p0: DataSnapshot) {}
                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    val isOver = p0.getValue(Boolean::class.java)
                    Log.i(TAG, "isOver: $isOver")
                    if (isOver!!) {
                        showNotification(userId)
                        Log.i("SaveService", "$userId 5시간 넘음")
                    }
                }
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {}
                override fun onCancelled(p0: DatabaseError) {}
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            })
        }
    }

    private fun show() {
        val durationDB = Firebase.database.reference.child("Duration")
        durationDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.i("saveService", "snapshot exists")
                    for (durationSnapshot in snapshot.children) {
                        val userId = durationSnapshot.key
                        Log.i("saveService", userId.toString())
                        if (followList.contains(userId!!)) {
                            val isOver = durationSnapshot.getValue(Boolean::class.java)
                            Log.i("saveService", isOver.toString())
                            if(isOver!!) {
                                showNotification(nameMap[userId]!!)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        Log.d(TAG, "Job Started")
        doBackgroundWork(p0)
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Log.d(TAG, "Job canceled before completion")
        jobCanceled = true
        return true
    }

    private fun saveUserDuration() {
        val usm = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        //val midnight: Long = (System.currentTimeMillis() / 86400000) * 86400000 - (9 * 3600000)
        var appList = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 3600 * 24,
            System.currentTimeMillis()
        )
        appList = appList.stream().filter { app: UsageStats -> app.totalTimeInForeground > 0 }
            .collect(Collectors.toList())
        /* 내 사용 기록 불러 왔으니 나의 사용 기록을 DB에 업데이트 */
        val usageTotaltime = appList.stream().map { obj: UsageStats -> obj.totalTimeInForeground }
            .mapToLong { obj: Long -> obj }.sum()
        // DB와 연결
        val userDB = Firebase.database.reference.child("Users")
        val userId = auth.currentUser?.uid.orEmpty()
        val currentUserDB = userDB.child(userId)
        // DB에 UserInfo 저장하기
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["totalTime"] = usageTotaltime
        currentUserDB.updateChildren(user)

        // Duration DB에 저장
        val durationDB = Firebase.database.reference.child("Duration")
        val duration = mutableMapOf<String, Any>()
        val targetDuration = 5 * 3600000
        val isOver = (usageTotaltime > targetDuration)
        duration[userId] = isOver
        durationDB.updateChildren(duration)
    }

    private fun loadFollowList() {
        followList = arrayListOf<String>()
        var userDB: DatabaseReference = Firebase.database.reference.child("Users")
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

    // notification
    private fun showNotification(name: String) {
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val activityIntent = Intent(this, RankingActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this,
            1,
            activityIntent,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val notification = NotificationCompat.Builder(this, "screendetox")
            .setContentTitle("Screen Detox")
            .setContentText("${name}님의 스마트폰 사용 시간이 5시간을 넘었습니다.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun getCurrentUserID(): String {
        if (auth.currentUser == null){
            Toast.makeText(this, "로그인이 되어있지않습니다.", Toast.LENGTH_SHORT).show()
            // 다시 로그인 화면으로 돌아옴
        }
        return auth.currentUser?.uid.orEmpty()
    }
}