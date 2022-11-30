package com.example.screendetox.dashboard

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
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
        adapter = RequestAdapter(tempList, followUser)
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
                    userList.clear()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        userList.add(user!!)
                    }
                }
                Log.i("userList size", userList.size.toString())
                tempList.addAll(userList)
                adapter = RequestAdapter(tempList, followUser)
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

    // follow 버튼에 달 click listener 람다 함수
    val followUser: (User) -> Unit = { user ->
        if (user.userId != getCurrentUserID()) {
            Log.i("friend add", "${user.userName!!}에게 친구 추가")
            userDB = Firebase.database.reference.child("Users")
            userDB.child(getCurrentUserID())
                .child("following")
                .child(user.userId!!)
                .setValue(true)
            Toast.makeText(this@FriendAddActivity, "친구 추가를 완료했습니다", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@FriendAddActivity, "자기 자신은 친구 추가 할 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
}