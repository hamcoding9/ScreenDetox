/*
GroupActivity: 다른 사람들의 사용 시간을 볼 수 있음.
2022.10.02 : 현재까지 가입한 모든 사용자들의 사용 시간 볼 수 있음.
 */
package com.example.screendetox.dashboard


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screendetox.R
import com.example.screendetox.data.User
import com.example.screendetox.databinding.ActivityBoardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class BoardActivity: AppCompatActivity() {
    private lateinit var usersRecyclerView : RecyclerView
    // 계정 정보
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    // 다른 사람의 사용 시간을 알아야 하므로 userDB 필요
    private lateinit var userDB: DatabaseReference
    // 유저 객체를 어댑터 쪽으로 담을 어레이 리스트
    private lateinit var userList : ArrayList<User>
    // 뷰 바인딩
    private lateinit var binding: ActivityBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Rank()) // 기본 fragment: rank 화면
        // 네비게이션 바
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.ranking -> replaceFragment(Rank())
                R.id.stats -> replaceFragment(Stats())

                else -> {
                }
            }
            true
        }

        // 예전 그룹 액티비티 코드
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        // show users
        userList = arrayListOf<User>()
        getUserData()
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

    private fun getUserData() {
        // 파이어베이스 연동
        userDB = Firebase.database.reference.child("Users")
        // DB에 있는 값을 UserList에 담는다
        userDB.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val user = userSnapshot.getValue(User::class.java)
                        userList.add(user!!)
                    }
                    usersRecyclerView.adapter = UserAdapter(userList)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
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