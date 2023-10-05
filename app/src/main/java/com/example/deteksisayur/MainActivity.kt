package com.example.deteksisayur

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var cameraOrientation = 0
    lateinit var capReq: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var imageReader: ImageReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        get_permission()

        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)
        textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }

        }
        imageReader = ImageReader.newInstance(1080,1920, ImageFormat.JPEG, 50)
        imageReader.setOnImageAvailableListener(object: ImageReader.OnImageAvailableListener{
            override fun onImageAvailable(reader: ImageReader?) {
                val image = reader?.acquireLatestImage()
                val buffer = image!!.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "img_$timeStamp.jpg")
                val opStream = FileOutputStream(file)

                opStream.write(bytes)

                opStream.close()
                image.close()
                Toast.makeText(this@MainActivity, "Gambar Telah Disimpan", Toast.LENGTH_SHORT).show()
            }
        }, handler)

        findViewById<Button>(R.id.simpanGambar).apply {
            setOnClickListener {
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                capReq.addTarget(imageReader.surface)
                cameraCaptureSession.capture(capReq.build(), null, null)

                capReq.set(CaptureRequest.JPEG_ORIENTATION, cameraOrientation)
                cameraCaptureSession.capture(capReq.build(), null, null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object: CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera

                val characteristics = cameraManager.getCameraCharacteristics(camera.id)
                cameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                val surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {

                    }
                }, handler)
            }

            override fun onDisconnected(camera: CameraDevice) {

            }

            override fun onError(camera: CameraDevice, error: Int) {

            }
        }, handler)
    }

    fun get_permission(){
        val permissionLst = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionLst.add(android.Manifest.permission.CAMERA)
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionLst.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionLst.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionLst.size > 0) {
            requestPermissions(permissionLst.toTypedArray(),101)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if(it != PackageManager.PERMISSION_GRANTED){
                get_permission()
            }
        }
    }
}