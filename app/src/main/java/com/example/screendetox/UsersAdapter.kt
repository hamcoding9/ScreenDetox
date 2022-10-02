package com.example.screendetox

// UserAdapter Class
// Data를 받아서 View로 뿌리는 클래스(?)
/*

class UsersAdapter(context: Context?, UserInfoArrayList: ArrayList<User?>?) :
    ArrayAdapter<User?>(context!!, 0, UserInfoArrayList!!)
{
        // 현재 position에 있는 view를 가져옴.
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            // Get the data item for this position
            var convertView = convertView
            // 현재 position에서 User class 를 가져옴
            val userInfo = getItem(position)

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.activity_main, parent, false)
            }

            // layout에서 View 가져오기
            // 사용자 프로필 이미지
            val user_img = convertView.findViewById<ImageView>(R.id.profile_image)
            // 사용자 이름
            val user_name = convertView!!.findViewById<TextView>(R.id.user_name)
            // 사용자 Total 시간
            val usage_duration_tv = convertView.findViewById<TextView>(R.id.usage_duration)

            // Data랑 View 연결
            user_img.setImageDrawable(userInfo.UserIcon)
            user_name.text = userInfo!!.UserName
            usage_duration_tv.text = userInfo.usageDuration

            // Return the completed view to render on screen
            return convertView
        }
}
*/
