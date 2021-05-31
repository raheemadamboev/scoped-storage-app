package xyz.teamgravity.scopedstorage.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.teamgravity.scopedstorage.databinding.ActivityMainBinding
import xyz.teamgravity.scopedstorage.helper.adapter.internalphoto.InternalPhotoAdapter
import xyz.teamgravity.scopedstorage.helper.adapter.internalphoto.InternalPhotoListener
import xyz.teamgravity.scopedstorage.model.InternalPhotoModel
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), InternalPhotoListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var takePhotoLauncher: ActivityResultLauncher<Void>
    private lateinit var internalPhotoAdapter: InternalPhotoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lateInIt()
        internalPhotoRecyclerView()
        button()
    }

    private fun lateInIt() {
        // camera photo
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                if (binding.privateSwitch.isChecked) {
                    if (savePhotoToInternalStorage(UUID.randomUUID().toString(), bitmap)) {
                        getInternalPhotos()
                        Toast.makeText(this, "Photos saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun internalPhotoRecyclerView() {
        internalPhotoAdapter = InternalPhotoAdapter(this)

        binding.apply {
            privatePhotoRecyclerView.layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
            privatePhotoRecyclerView.adapter = internalPhotoAdapter
        }

        getInternalPhotos()
    }

    private fun button() {
        onCamera()
    }

    private fun onCamera() {
        binding.photoB.setOnClickListener {
            takePhotoLauncher.launch(null)
        }
    }

    private fun getInternalPhotos() {
        lifecycleScope.launchWhenStarted {
            internalPhotoAdapter.submitList(loadPhotosFromInternalStorage())
        }
    }

    private fun savePhotoToInternalStorage(name: String, bitmap: Bitmap): Boolean {
        return try {
            openFileOutput("$name.jpg", MODE_PRIVATE).use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw IOException("Couldn't save bitmap")
                }
                true
            }
        } catch (e: IOException) {
            println("debug: ${e.message}")
            false
        }
    }

    private suspend fun loadPhotosFromInternalStorage(): List<InternalPhotoModel> {
        return withContext(Dispatchers.IO) {
            val files = filesDir.listFiles()
            files?.filter {
                it.canRead() && it.isFile && it.name.endsWith(".jpg")
            }?.map {
                val bytes = it.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalPhotoModel(
                    name = it.name,
                    bitmap = bitmap
                )
            } ?: listOf()
        }
    }

    private fun deleteFromInternalStorage(name: String): Boolean {
        return try {
            deleteFile(name)
        } catch (e: Exception) {
            println("debug: ${e.message}")
            false
        }
    }

    // internal photo long click
    override fun onInternalPhotoLongClick(photo: InternalPhotoModel) {
        if (deleteFromInternalStorage(photo.name)) {
            getInternalPhotos()
            Toast.makeText(this, "${photo.name} deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to delete ${photo.name}", Toast.LENGTH_SHORT).show()
        }
    }
}