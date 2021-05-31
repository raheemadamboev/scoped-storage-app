package xyz.teamgravity.scopedstorage.helper.adapter.internalphoto

import xyz.teamgravity.scopedstorage.model.InternalPhotoModel

interface InternalPhotoListener {

    fun onInternalPhotoLongClick(photo: InternalPhotoModel)
}