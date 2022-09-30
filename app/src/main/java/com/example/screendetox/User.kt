package com.example.screendetox

import android.graphics.drawable.Drawable

// Adapter List 에 담는 단위
// User 한 명이 가지고 있는 정보: UserIcon(프로필), UserName(이름), usageDuration(total 사용 시간)
class User (
    var UserIcon: Drawable,
    var UserName: String,
    var usageDuration: String
    )