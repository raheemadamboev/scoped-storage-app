package xyz.teamgravity.scopedstorage.helper.adapter.internalphoto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xyz.teamgravity.scopedstorage.databinding.CardPhotoBinding
import xyz.teamgravity.scopedstorage.model.InternalPhotoModel

class InternalPhotoAdapter(
    private val listener: InternalPhotoListener
) : ListAdapter<InternalPhotoModel, InternalPhotoAdapter.InternalPhotoViewHolder>(InternalPhotoDiff()) {

    inner class InternalPhotoViewHolder(private val binding: CardPhotoBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageI.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onInternalPhotoLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(model: InternalPhotoModel) {
            binding.apply {
                imageI.setImageBitmap(model.bitmap)

                val aspectRatio = model.bitmap.width.toFloat() / model.bitmap.height.toFloat()
                ConstraintSet().apply {
                    clone(root)
                    setDimensionRatio(imageI.id, aspectRatio.toString())
                    applyTo(root)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InternalPhotoViewHolder =
        InternalPhotoViewHolder(CardPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: InternalPhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}