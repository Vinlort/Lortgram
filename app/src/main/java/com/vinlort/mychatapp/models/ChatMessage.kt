package com.vinlort.mychatapp.models

data class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: Long,
    val keyAES: String,
    val iv: String
) {
    constructor() : this("", "", "", "", -1, "","")
}