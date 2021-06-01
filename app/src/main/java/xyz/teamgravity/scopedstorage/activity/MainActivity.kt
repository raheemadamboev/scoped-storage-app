package xyz.teamgravity.scopedstorage.activity

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.teamgravity.scopedstorage.databinding.ActivityMainBinding
import xyz.teamgravity.scopedstorage.helper.adapter.internalphoto.InternalPhotoAdapter
import xyz.teamgravity.scopedstorage.helper.adapter.internalphoto.InternalPhotoListener
import xyz.teamgravity.scopedstorage.helper.adapter.sharedphoto.SharedPhotoAdapter
import xyz.teamgravity.scopedstorage.helper.adapter.sharedphoto.SharedPhotoListener
import xyz.teamgravity.scopedstorage.model.InternalPhotoModel
import xyz.teamgravity.scopedstorage.model.SharedPhotoModel
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), InternalPhotoListener, SharedPhotoListener {
    companion object {
        private val SDK_28_ABOVE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        private val PERMISSIONS_LEGACY = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private val PERMISSIONS_SCOPED = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        private val PERMISSIONS = if (SDK_28_ABOVE) PERMISSIONS_SCOPED else PERMISSIONS_LEGACY
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var takePhotoLauncher: ActivityResultLauncher<Void>
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var internalPhotoAdapter: InternalPhotoAdapter
    private lateinit var sharedPhotoAdapter: SharedPhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lateInIt()
        checkPermissions()
        internalPhotoRecyclerView()
        sharedPhotoRecyclerView()
        button()
    }

    private fun lateInIt() {
        // camera photo
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                val successful = if (binding.privateSwitch.isChecked) savePhotoToInternalStorage(UUID.randomUUID().toString(), bitmap)
                // TODO what if storage permission did not grant in below 29 API
                else savePhotoToExternalStorage(UUID.randomUUID().toString(), bitmap)

                if (successful) {
                    getInternalPhotos()
                    Toast.makeText(this, "Photos saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // request permissions
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

        }
    }

    private fun checkPermissions() {
        if (!hasPermissions()) {
            requestPermissions()
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

    private fun sharedPhotoRecyclerView() {
        sharedPhotoAdapter = SharedPhotoAdapter(this)

        binding.apply {
            sharedPhotoRecyclerView.layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
            sharedPhotoRecyclerView.adapter = sharedPhotoAdapter
        }


    }

    private fun button() {
        onCamera()
    }

    private fun hasPermissions() = PERMISSIONS.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        permissionsLauncher.launch(PERMISSIONS)
    }

    private fun getInternalPhotos() {
        lifecycleScope.launchWhenStarted {
            internalPhotoAdapter.submitList(loadPhotosFromInternalStorage())
        }
    }

    private fun getExternalPhotos() {

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

    private fun savePhotoToExternalStorage(name: String, bitmap: Bitmap): Boolean {
        // dir
        val imageCollection = if (SDK_28_ABOVE) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // metadata
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }

        // save file
        return try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")

            true
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

    // camera button
    private fun onCamera() {
        binding.photoB.setOnClickListener {
            takePhotoLauncher.launch(null)
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

    // shared photo long click
    override fun onPhotoLongClick(photo: SharedPhotoModel) {

    }
}