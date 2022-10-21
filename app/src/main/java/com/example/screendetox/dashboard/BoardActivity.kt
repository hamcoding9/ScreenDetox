package com.example.screendetox.dashboard


import android.app.usage.UsageStats
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.screendetox.R
import com.example.screendetox.databinding.ActivityBoardBinding
import com.google.firebase.auth.FirebaseAuth

class BoardActivity: AppCompatActivity() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    // 뷰 바인딩
    private lateinit var binding: ActivityBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Rank()) // 기본 fragment: rank 화면
        // 네비게이션 탭바 클릭 및 프래그먼트 이동
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.ranking -> replaceFragment(Rank())
                R.id.stats -> replaceFragment(Stats())
                else -> {
                }
            }
            true
        }
        // Rank fragment에서 사용할 appUsageMap 받기
        val mySortedMap: HashMap<String, UsageStats> = intent.getSerializableExtra("appUsageMap") as HashMap<String, UsageStats>
        //if(!mySortedMap.isNullOrEmpty()){Log.i("hashmap","mysortedmap'size is %d".format(mySortedMap.size))}

        // Rank fragment로 전달
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        // Stats Fragment인 경우 데이터 전달
        if(fragment==Stats()){
            val mySortedMap: HashMap<String, UsageStats> = intent.getSerializableExtra("appUsageMap") as HashMap<String, UsageStats>
            var stats = Stats()
            var bundle = Bundle()
            bundle.putSerializable("mySortedMap", mySortedMap)
            stats.arguments = bundle
            fragmentTransaction.replace(R.id.frame_layout, stats)
            fragmentTransaction.commit()
        }
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

/*
        // 사용자 닉네임 설정하기
        // 설정이 완료 되었으면 사용자 목록 불러오기를 실행한다.
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot){
                    if (snapshot.child("Name").value == null){
                        showNameInputPopup()
                        return
                    }
                    getUserGroup()
                }
                override fun onCancelled(error: DatabaseError) {}
        })
    */

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
    // 사용자 닉네임 설정
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
    }*/
/*
    // 사용자 닉네임 DB에 저장
    private fun saveUserName(name: String) {

        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user["userID"] = userId
        user["name"] = name
        currentUserDB.updateChildren(user)

        getUserGroup()
    }
*/

}