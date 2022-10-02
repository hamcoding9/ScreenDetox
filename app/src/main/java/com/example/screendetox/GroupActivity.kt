/*
GroupActivity: 다른 사람들의 사용 시간을 볼 수 있음.
2022.10.02 : 현재까지 가입한 모든 사용자들의 사용 시간 볼 수 있음.
 */
package com.example.screendetox

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GroupActivity: AppCompatActivity() {
    // 계정 정보
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    // 다른 사람의 사용 시간을 알아야 하므로 userDB 필요
    private lateinit var userDB: DatabaseReference
    // ListView 띄워야 하므로 ListView 불러오기
    private lateinit var usersList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        userDB = Firebase.database.reference.child("Users")
        val currentUserDB = userDB.child(getCurrentUserID())

        // UserGroup 보여주기
//        ShowUserGroup()
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