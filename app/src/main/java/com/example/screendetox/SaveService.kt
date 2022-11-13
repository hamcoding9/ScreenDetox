package com.example.screendetox

import android.app.job.JobParameters
import android.app.job.JobService
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.stream.Collectors

class SaveService: JobService() {

    companion object {
        private const val TAG = "SaveService"
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
    }

    private var jobCanceled : Boolean = false

    private fun doBackgroundWork(p0: JobParameters?) {
        Thread(Runnable {
            kotlin.run {
                saveUserDuration()
                Log.d(TAG, "Job finish")
                jobFinished(p0, false)
            }
        }).start()
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
        val midnight : Long = (System.currentTimeMillis() / 86400000) * 86400000 - (9 * 3600000)
        var appList = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            midnight,
            System.currentTimeMillis())
        appList = appList.stream().filter{ app: UsageStats -> app.totalTimeInForeground > 0}
            .collect(Collectors.toList())
        // 내 사용 기록 불러 왔으니 나의 사용 기록을 DB에 업데이트
        val usageTotaltime = appList.stream().map {obj:UsageStats -> obj.totalTimeInForeground }.mapToLong{obj: Long -> obj}.sum()
        // DB와 연결
        var userDB = Firebase.database.reference.child("Users")
        val userId = auth.currentUser?.uid.orEmpty()
        val currentUserDB = userDB.child(userId)
        // DB에 UserInfo 저장하기
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        user["totalTime"] = usageTotaltime
        currentUserDB.updateChildren(user)
    }
}