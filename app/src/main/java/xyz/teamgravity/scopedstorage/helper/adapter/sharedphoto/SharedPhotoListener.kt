package xyz.teamgravity.scopedstorage.helper.adapter.sharedphoto

import xyz.teamgravity.scopedstorage.model.SharedPhotoModel

interface SharedPhotoListener {

    fun onPhotoLongClick(photo: SharedPhotoModel)
}