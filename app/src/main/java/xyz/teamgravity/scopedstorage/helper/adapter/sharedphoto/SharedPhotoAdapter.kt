package xyz.teamgravity.scopedstorage.helper.adapter.sharedphoto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xyz.teamgravity.scopedstorage.databinding.CardPhotoBinding
import xyz.teamgravity.scopedstorage.model.SharedPhotoModel

class SharedPhotoAdapter(
    private val listener: SharedPhotoListener
) : ListAdapter<SharedPhotoModel, SharedPhotoAdapter.SharedPhotoViewHolder>(SharedPhotoDiff()) {

    inner class SharedPhotoViewHolder(private val binding: CardPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                imageI.setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onPhotoLongClick(getItem(position))
                    }
                    true
                }
            }
        }

        fun bind(model: SharedPhotoModel) {
            binding.apply {
                imageI.setImageURI(model.contentUri)

                val aspectRatio = model.width.toFloat() / model.height.toFloat()
                ConstraintSet().apply {
                    clone(root)
                    setDimensionRatio(imageI.id, aspectRatio.toString())
                    applyTo(root)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedPhotoViewHolder =
        SharedPhotoViewHolder(CardPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SharedPhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}