package com.example.deteksisayur

import Data.APIDatabase
import Data.APIMachineLearning
import Data.DDatabase
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
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
import android.os.*
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
    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraDevice: CameraDevice
    lateinit var imageReader: ImageReader
    private lateinit var hasilDetect: TextView
    lateinit var imageView: ImageView
    private var isTextureViewVisible = true
    private var cameraOrientation: Int = 0
    var isImageLoaded = false
    private lateinit var apiMachineLearning: APIMachineLearning

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Method.processActivityResult(this, requestCode, resultCode, data, imageView, textureView, isImageLoaded)
        isImageLoaded = true
    }

    private fun createPartFromString(string: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), string)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPermission()
        val spinnerNamaPasar: Spinner = findViewById(R.id.spinnerNamaPasar)
        val spinnerNamaSayur: Spinner = findViewById(R.id.spinnerNamaSayur)
        Method.setupSpinners(this, spinnerNamaPasar, spinnerNamaSayur)
        textureView = findViewById(R.id.textureView)
        imageView = findViewById(R.id.ImageView)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        val analisisData = findViewById<ImageButton>(R.id.analisisData)
        analisisData.setOnClickListener {
            val intent = Intent(this, MainActivityAnalisisAdmin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) { openCamera(surface) }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean { return false }

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
                Method.captureImageFromCamera(this@MainActivity, isTextureViewVisible, cameraDevice, imageReader, textureView, cameraOrientation, isImageLoaded, handler)
            }
        }

        val galleryButton = findViewById<Button>(R.id.ambilGaleri)
        galleryButton.setOnClickListener {
            Method.openGallery(this)
        }

        val imageButton = findViewById<ImageButton>(R.id.simpanLokal)
        imageButton.setOnClickListener {
            val context: Context = applicationContext
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageByteArray = stream.toByteArray()
            val detectionResult: String = hasilDetect.text.toString()

            Method.saveToSQLITE(context, imageByteArray, detectionResult)
        }

        hasilDetect = findViewById(R.id.rhasilDetect)
        apiMachineLearning = APIMachineLearning(this) { hasilDeteksi ->
            runOnUiThread {
                val customResult = if (hasilDeteksi == "0") {
                    "Foto sayur tidak ditemukan"
                } else if (hasilDeteksi == "1") {
                    "Sayur Busuk"
                } else {
                    "Sayur Segar"
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

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            if (isImageLoaded) {
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap

                val newWidth = 3456
                val newHeight = 4608

                val compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

                val byteArrayOutputStream = ByteArrayOutputStream()
                compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

                val requestFile = RequestBody.create(MediaType.parse("image/*"), byteArray)
                val gambarPart = MultipartBody.Part.createFormData("gambar_sayur", "image_${timeStamp}.png", requestFile)

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

        val cropButton = findViewById<ImageButton>(R.id.cropGambar)
        cropButton.setOnClickListener {
            Method.setCropValues(190, 130, 1070, 1500)
            val croppedBitmap = Method.cropImage(imageView)

            if (croppedBitmap != null) {
                imageView.setImageBitmap(croppedBitmap)
                Toast.makeText(this, "Gambar berhasil di potong", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Tidak ada gambar yang bisa di potong", Toast.LENGTH_SHORT).show()
            }
        }

        val machinelearn = findViewById<ImageButton>(R.id.machineLearning)
        machinelearn.setOnClickListener {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            apiMachineLearning.sendImageToServer(bitmap)
        }

        val resetButton = findViewById<ImageButton>(R.id.reload)
        resetButton.setOnClickListener {
            Method.resetCamera(cameraDevice, imageView, textureView, isImageLoaded, hasilDetect, ::openCamera, this@MainActivity::runOnUiThread)
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.CAMERA)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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
