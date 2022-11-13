package com.example.screendetox

import android.Manifest
import android.app.AppOpsManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.screendetox.dashboard.RankingActivity
import com.example.screendetox.service.SaveService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    // Permission 관련 View 불러오기
    var enableBtn: Button? = null
    var permissionTv: TextView? = null

    // 로그인 정보
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableBtn = findViewById(R.id.enable_btn) // permission enable 버튼
        permissionTv = findViewById(R.id.permission_tv) // permission text
        scheduleJob()
    }

    private fun scheduleJob() {
        val componentName = ComponentName(this, SaveService::class.java)
        val info = JobInfo.Builder(1, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            .setPersisted(true)
            .setPeriodic(DateUtils.MINUTE_IN_MILLIS * 5) // 5분마다 반복적
            .build()
        val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = jobScheduler.schedule(info)

        val isJobScheduledSuccess = resultCode == JobScheduler.RESULT_SUCCESS
        Log.d("MainActivity", "Job Scheduled ${if (isJobScheduledSuccess) "SUCCESS" else "FAILED"}")
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
                startActivity(Intent(this, RankingActivity::class.java))
            }
        }
        else {
            showHideNoPermission()
            enableBtn!!.setOnClickListener {view: View? -> startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))}
        }
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
}