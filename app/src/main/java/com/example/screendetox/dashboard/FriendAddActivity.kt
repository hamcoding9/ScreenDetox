package com.example.screendetox.dashboard

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var adapter : RequestAdapter
    private lateinit var recyclerView : RecyclerView
    private lateinit var userList : ArrayList<User>
    private lateinit var tempList : ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = binding.requestRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadUserList()
        Log.i("userlist", userList.size.toString())
        tempList.addAll(userList)
        adapter = RequestAdapter(tempList)
        recyclerView.adapter = adapter

        binding.inputFriendname.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                TODO("")
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                tempList.clear()
                val searchText = p0!!
                if (searchText.isNotEmpty()) {
                    userList.forEach {
                        if (it.userName!!.contains(searchText)){
                            tempList.add(it)
                        }
                    }
                    recyclerView.adapter!!.notifyDataSetChanged()
                } else {
                    tempList.clear()
                    tempList.addAll(userList)
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
                return false
            }
        })

    }

    private fun loadUserList() {
        userList = arrayListOf<User>()
        tempList = arrayListOf<User>()
        userDB = Firebase.database.reference.child("Users")
        userDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    //userList.clear()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        userList.add(user!!)
                    }
                }
                Log.i("userList size", userList.size.toString())
                tempList.addAll(userList)
                adapter = RequestAdapter(tempList)
                recyclerView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getCurrentUserID(): String {
        if (SaveService.auth.currentUser == null){
        }
        return SaveService.auth.currentUser?.uid.orEmpty()
    }
}

/*    private fun requestUser(query: String) {
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
    }*/



/*        binding.followBtn.setOnClickListener {
            Log.i("friendAdd", "친구요청 클릭")
            Toast.makeText(this@FriendAddActivity, "친구 요청 클릭", Toast.LENGTH_SHORT).show()

            val query = binding.inputFriendname.text.toString()
            Log.i("friendAdd", "친구요청 입력이름: ${query} ")
            requestUser(query)
        }*/

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