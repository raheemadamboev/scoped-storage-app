package xyz.teamgravity.scopedstorage.model

import android.net.Uri

data class SharedPhotoModel(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val contentUri: Uri
)
