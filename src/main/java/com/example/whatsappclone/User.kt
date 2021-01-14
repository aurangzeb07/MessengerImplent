package com.example.whatsappclone

import com.google.firebase.firestore.FieldValue

data class User(
    val name: String,
    val imageUrl: String,
    val thumbImage: String,
    val uid: String,
    val deviceToken: String,
    val status: String,
    val onlineStatus: Boolean
) {



    // Always while making a data class for Firebase first made an Empty Constructor otherwise it will not work.
    constructor() : this("" , "" , "" ,"" , "" , "" , false)          // Empty Constructor

    constructor(name: String , imageUrl: String , thumbImage: String , uid: String) : this(
        name,
        imageUrl,
        thumbImage,
        uid,
        "",
        " Hey there I am using WhatsApp",
        false

    )
//    constructor() : this("", "", "", "", "Hey There, I am using whatsapp", "", false)
//
//    constructor(name: String, imageUrl: String, thumbImage: String, uid: String) :
//            this(name, imageUrl, thumbImage, "", uid = uid, status = "Hey There, I am using whatsapp", online = false)

}