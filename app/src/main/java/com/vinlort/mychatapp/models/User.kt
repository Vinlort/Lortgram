package com.vinlort.mychatapp.models

class User(val uid: String, val username: String, val profileImageUrl: String){
    constructor(): this("","","")
}