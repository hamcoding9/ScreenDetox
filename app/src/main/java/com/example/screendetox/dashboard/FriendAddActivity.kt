package com.example.screendetox.dashboard

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.screendetox.R
import com.example.screendetox.SaveService
import com.example.screendetox.data.User
import com.example.screendetox.databinding.ActivityFriendAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FriendAddActivity : AppCompatActivity() {
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference
    private lateinit var binding: ActivityFriendAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_add)
        binding = ActivityFriendAddBinding.inflate(layoutInflater)

        binding.friendRequestBtn.setOnClickListener {
            Log.i("friendAdd", "친구요청 클릭")
            Toast.makeText(this@FriendAddActivity, "친구 요청 클릭", Toast.LENGTH_SHORT).show()

            val query = binding.inputFriendname.text.toString()
            Log.i("friendAdd", "친구요청 입력이름: ${query} ")
            requestUser(query)
        }

/*        binding.friendSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색 버튼 누를 때 호출
                if (!query.isNullOrEmpty()) {
                    Log.i ("friendAdd", "검색 버튼 클릭")
                    requestUser(query)
                } else {
                    Toast.makeText(this@FriendAddActivity, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                TODO("Not yet implemented")
            }
        })*/
    }

    private fun requestUser(query: String) {
        val userDB = Firebase.database.reference.child("Users")
        val userMap = loadUserList()
        if (query in userMap.values) {
            val friendId = userMap.filterValues { it == query }.keys.first()
            Log.i("friendAdd", "입력한 친구 닉네임: ${query}")
            Log.i("friendAdd", "입력한 친구 UID: ${friendId}")
            userDB.child(friendId!!)
                .child("requestedBy")
                .child("pending")
                .child(getCurrentUserID())
                .setValue(true)
            Toast.makeText(this@FriendAddActivity, "친구 요청을 완료했습니다", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(this@FriendAddActivity, "검색어를 확인해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserList(): HashMap<String?, String?> {
        val userNameMap = hashMapOf<String?, String?>()
        val userDB = Firebase.database.reference.child("Users")
        userDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userNameMap.clear()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        userNameMap[user!!.userId] = user.userName
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        return userNameMap
    }

    private fun getCurrentUserID(): String {
        if (SaveService.auth.currentUser == null){
        }
        return SaveService.auth.currentUser?.uid.orEmpty()
    }
}