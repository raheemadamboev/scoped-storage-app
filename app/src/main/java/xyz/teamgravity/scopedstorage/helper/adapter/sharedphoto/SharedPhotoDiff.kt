package xyz.teamgravity.scopedstorage.helper.adapter.sharedphoto

import androidx.recyclerview.widget.DiffUtil
import xyz.teamgravity.scopedstorage.model.SharedPhotoModel

class SharedPhotoDiff: DiffUtil.ItemCallback<SharedPhotoModel>() {

    override fun areItemsTheSame(oldItem: SharedPhotoModel, newItem: SharedPhotoModel) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: SharedPhotoModel, newItem: SharedPhotoModel) =
        oldItem == newItem
}