package com.example.deteksisayur

import Api.APIDatabase
import Api.APIMachineLearning
import Data.DDatabase
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.hardware.camera2.*
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var capReq: CaptureRequest.Builder
    lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraDevice: CameraDevice
    lateinit var imageReader: ImageReader
    private lateinit var hasilDetect: TextView
    lateinit var imageView: ImageView
    private var isTextureViewVisible = true
    private var cameraOrientation: Int = 0
    private var isImageLoaded = false
    private lateinit var apiMachineLearning: APIMachineLearning
    private val REQUEST_PICK_IMAGE = 1

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            try {
                val inputStream = selectedImageUri?.let { contentResolver.openInputStream(it) }
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                imageView.visibility = View.VISIBLE
                textureView.visibility = View.GONE
                isImageLoaded = true
                apiMachineLearning.sendImageToServer(bitmap)
            } catch (e: FileNotFoundException) {
                Toast.makeText(this@MainActivity, "Terjadi kesalahan saat memilih gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners() {
        val spinnerNamaPasar: Spinner = findViewById(R.id.spinnerNamaPasar)
        val spinnerNamaSayur: Spinner = findViewById(R.id.spinnerNamaSayur)

        val namaPasarItems = arrayOf("Pasar Sayur A", "Pasar Sayur B", "Pasar Sayur C")
        val namaSayurItems = arrayOf("Timun", "Wortel", "Kubis", "Brokoli", "Selada")

        val pasarAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaPasarItems)
        val sayurAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaSayurItems)

        pasarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sayurAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerNamaPasar.adapter = pasarAdapter
        spinnerNamaSayur.adapter = sayurAdapter
    }

    private fun createPartFromString(string: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), string)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPermission()
        setupSpinners()
        textureView = findViewById(R.id.textureView)
        imageView = findViewById(R.id.ImageView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        val analisisData = findViewById<ImageButton>(R.id.analisisData)
        analisisData.setOnClickListener {
            val intent = Intent(this, MainActivityAnalisisAdmin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera(surface)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        imageReader = ImageReader.newInstance(3546, 4608, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader?.acquireLatestImage()
            try {
                if (image != null) {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    runOnUiThread {
                        imageView.setImageBitmap(bitmap)
                        imageView.visibility = View.VISIBLE
                        textureView.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                image?.close()
            }
        }, handler)

        findViewById<Button>(R.id.ambilKamera).apply {
            setOnClickListener {
                if (isTextureViewVisible) {
                    try {
                        cameraDevice.createCaptureSession(
                            listOf(imageReader.surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    try {
                                        val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                                        captureRequest.addTarget(imageReader.surface)

                                        captureRequest.set(
                                            CaptureRequest.JPEG_ORIENTATION,
                                            cameraOrientation
                                        )

                                        session.capture(captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {
                                            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                                                runOnUiThread {
                                                    val capturedBitmap = textureView.bitmap
                                                    if (capturedBitmap != null) {
                                                        apiMachineLearning.sendImageToServer(capturedBitmap)
                                                    } else {
                                                        Toast.makeText(this@MainActivity, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show()
                                                    }
                                                    isImageLoaded = true
                                                }
                                            }

                                            override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
                                                runOnUiThread {
                                                    Toast.makeText(this@MainActivity, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }, handler)
                                    } catch (e: CameraAccessException) {
                                        e.printStackTrace()
                                        runOnUiThread {
                                            Toast.makeText(this@MainActivity, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {}
                            },
                            handler
                        )
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Tidak dapat mengambil gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val galleryButton = findViewById<Button>(R.id.ambilGaleri)
        galleryButton.setOnClickListener {
            openGallery()
        }

        val imageButton = findViewById<ImageButton>(R.id.simpanLokal)
        imageButton.setOnClickListener {
            try {
                lateinit var currentPhotoPath: String
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

                val image = File.createTempFile(
                    "img_${timeStamp}",
                    ".jpg",
                    storageDir
                ).apply {
                    currentPhotoPath = absolutePath
                }

                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                val stream = FileOutputStream(image)

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.close()

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(image.absolutePath, options)
                Toast.makeText(this@MainActivity, "Gambar berhasil disimpan", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
            }
        }

        hasilDetect = findViewById(R.id.rhasilDetect)
        apiMachineLearning = APIMachineLearning(this) { hasilDeteksi ->
            runOnUiThread {
                val customResult = if (hasilDeteksi == "0") {
                    "Sayur Busuk"
                } else if (hasilDeteksi == "1") {
                    "Sayur Segar"
                } else {
                    "Hasil deteksi tidak valid"
                }
                hasilDetect.text = customResult
            }
        }

        val kirimButton = findViewById<ImageButton>(R.id.kirimServer)
        kirimButton.setOnClickListener {

            val spinnerNamaSayur: Spinner = findViewById(R.id.spinnerNamaSayur)
            val spinnerNamaPasar: Spinner = findViewById(R.id.spinnerNamaPasar)

            val selectedSayur = spinnerNamaSayur.selectedItem.toString()
            val selectedPasar = spinnerNamaPasar.selectedItem.toString()

            if (isImageLoaded) {
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap

                val newWidth = 360
                val newHeight = 630

                val compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

                val byteArrayOutputStream = ByteArrayOutputStream()
                compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

                val requestFile = RequestBody.create(MediaType.parse("image/*"), byteArray)
                val gambarPart = MultipartBody.Part.createFormData("gambar_sayur", "image.png", requestFile)

                val deteksiData = HashMap<String, RequestBody>()
                deteksiData["nama_sayur"] = createPartFromString(selectedSayur)
                deteksiData["nama_pasar"] = createPartFromString(selectedPasar)
                deteksiData["hasil_deteksi"] = createPartFromString(hasilDetect.text.toString())

                val api = APIDatabase.APIClient.api
                api.postKirimServer(deteksiData, gambarPart).enqueue(object : Callback<DDatabase> {
                    override fun onResponse(call: Call<DDatabase>, response: Response<DDatabase>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Data berhasil dikirim ke server", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Gagal mengirim data ke server", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DDatabase>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Gagal mengirim data ke server", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@MainActivity, "Gambar belum dimuat. Mohon muat gambar terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }


        val resetButton = findViewById<ImageButton>(R.id.reload)
        resetButton.setOnClickListener {
            cameraDevice.close()
            runOnUiThread {
                imageView.visibility = View.GONE
                textureView.visibility = View.VISIBLE
                isImageLoaded = false
                hasilDetect.text = "Hasil Deteksi"
            }
            openCamera(textureView.surfaceTexture!!)
        }

        imageView.setOnClickListener {
            isImageLoaded = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(surfaceTexture: SurfaceTexture) {
        cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                val characteristics = cameraManager.getCameraCharacteristics(camera.id)

                cameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                val surface = Surface(surfaceTexture)
                capReq.addTarget(surface)

                cameraDevice.createCaptureSession(
                    listOf(surface, imageReader.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            session.setRepeatingRequest(capReq.build(), null, null)
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}

                    },
                    handler
                )
            }

            override fun onDisconnected(camera: CameraDevice) {}

            override fun onError(camera: CameraDevice, error: Int) {}

        }, handler)
    }

    private fun getPermission() {
        val permissionList = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.CAMERA)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                getPermission()
            }
        }
    }
}
