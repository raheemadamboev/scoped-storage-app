package xyz.teamgravity.scopedstorage.helper.adapter.internalphoto

import androidx.recyclerview.widget.DiffUtil
import xyz.teamgravity.scopedstorage.model.InternalPhotoModel

class InternalPhotoDiff: DiffUtil.ItemCallback<InternalPhotoModel>() {

    override fun areItemsTheSame(oldItem: InternalPhotoModel, newItem: InternalPhotoModel) =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: InternalPhotoModel, newItem: InternalPhotoModel) =
        oldItem.name == newItem.name && oldItem.bitmap.sameAs(newItem.bitmap)
}