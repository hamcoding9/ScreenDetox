package com.example.screendetox.data

// Adapter List 에 담는 단위
// User 한 명이 가지고 있는 정보: userName(식별 이름), usageTotaltime(total 사용 시간)
data class User (
    var userId: String ?= null,
    var userName: String ?= null,
    //var goalTime: Long ?= null,
    var totalTime: Long ?= null,
    var mostUsedApp: String ?= null,
)