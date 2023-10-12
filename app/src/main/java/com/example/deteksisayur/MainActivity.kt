package com.example.deteksisayur

import Database
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.deteksisayur.databinding.ActivityMainBinding
import com.example.deteksisayur.ml.Modelbaru
import org.tensorflow.lite.support.image.TensorImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Base64
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var hasildetect: TextView
    private val GALLERY_REQUEST_CODE = 123
    private val TAG: String = "CHECK_RESPONSE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        imageView = binding.ImageView
        button = binding.Ambil
        hasildetect = binding.rhasilDetect
        val buttonGaleri = binding.ambilGaleri
        val buttonSimpanLokal = binding.simpanLokal
        val buttonKirimServer = binding.kirimServer

        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                takePicturePreview.launch(null)
            } else {
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }

        buttonGaleri.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            } else {
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        buttonSimpanLokal.setOnClickListener {
            saveImageAndResultToLocalStorage()
        }

        val requestInternetPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    Toast.makeText(this, "Berhasil Terhubung", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Akses INTERNET ditolak!! Coba lagi", Toast.LENGTH_SHORT).show()
                }
            }

        buttonKirimServer.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.INTERNET
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val hasilDeteksi = hasildetect.text.toString()
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                val base64Image = bitmapToBase64(bitmap)

                val dataToSend = Data(base64Image, hasilDeteksi)

                val databaseService = Database()

                databaseService.sendDataToDatabase(dataToSend) { success ->
                    if (success) {
                        Toast.makeText(this@MainActivity, "Data berhasil dikirim ke Database", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Gagal mengirim data ke Database", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                requestInternetPermission.launch(android.Manifest.permission.INTERNET)
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                takePicturePreview.launch(null)
            } else {
                Toast.makeText(this, "Akses Ditolak !! Coba Lagi ", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePicturePreview =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                outputGenerator(bitmap)
            }
        }

    private val onresult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i("TAG", "Hasil: ${result.data} ${result.resultCode}")
            onResultReceived(GALLERY_REQUEST_CODE, result)
        }

    private fun onResultReceived(requestCode: Int, result: ActivityResult) {
        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        Log.i("TAG", "Hasil Diterima: $uri")
                        val bitmap =
                            BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        imageView.setImageBitmap(bitmap)
                        outputGenerator(bitmap)
                    }
                } else {
                    Log.e("TAG", "error memilih gambar")
                }
            }
        }
    }

    private fun outputGenerator(bitmap: Bitmap) {
        val veggieModelbaru = Modelbaru.newInstance(this)

        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfimage = TensorImage.fromBitmap(newBitmap)

        val outputs = veggieModelbaru.process(tfimage)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score }
            }
        val highProbabilityOutput = outputs[0]

        hasildetect.text = highProbabilityOutput.label
        Log.i("TAG", "outputGenerator: $highProbabilityOutput")
    }

    private fun saveImageAndResultToLocalStorage() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val hasilDeteksi = hasildetect.text.toString()
            saveResultToLocalStorage(hasilDeteksi)

            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val fileName = "img_${getCurrentDateTime()}.jpg"
            val savedImagePath = saveImageToInternalStorage(bitmap, fileName)

            if (savedImagePath != null) {
                Toast.makeText(this, "Foto disimpan di $savedImagePath", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestWritePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap, fileName: String): String? {
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val filePath = File(directory, fileName)

        val width: Int
        val height: Int
        if (bitmap.width > bitmap.height) {
            width = 4608
            height = 3456
        } else {
            width = 3456
            height = 4608
        }

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        try {
            val stream: OutputStream = FileOutputStream(filePath)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return filePath.absolutePath
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val currentDateAndTime: String = sdf.format(Date())
        return currentDateAndTime
    }

    private val requestWritePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val hasilDeteksi = hasildetect.text.toString()
                saveResultToLocalStorage(hasilDeteksi)
            } else {
                Toast.makeText(this, "Akses Ditolak !! Coba Lagi ", Toast.LENGTH_SHORT).show()
            }
        }

    private fun saveResultToLocalStorage(result: String) {
        val fileName = "hasil_deteksi_${getCurrentDateTime()}.txt"
        val directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val filePath = File(directory, fileName)
        try {
            filePath.writeText(result)
            Toast.makeText(this, "Hasil deteksi disimpan di ${filePath.absolutePath}", Toast.LENGTH_SHORT)
                .show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan hasil deteksi", Toast.LENGTH_SHORT).show()
        }
    }
}
