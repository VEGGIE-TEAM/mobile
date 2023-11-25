package com.example.deteksisayur

import android.content.Intent
import android.provider.MediaStore
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.hardware.camera2.*
import android.media.ImageReader
import android.net.Uri
import android.os.Handler
import android.view.TextureView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.FileNotFoundException

const val REQUEST_PICK_IMAGE = 1
var isImageLoaded = false
private var cropX: Int = 150
private var cropY: Int = 130
private var cropWidth: Int = 1000
private var cropHeight: Int = 1500

class Method {
    companion object {
        fun openGallery(activity: Activity) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activity.startActivityForResult(intent, REQUEST_PICK_IMAGE)
        }

        fun setCropValues(x: Int, y: Int, width: Int, height: Int) {
            cropX = x
            cropY = y
            cropWidth = width
            cropHeight = height
        }

        fun cropImage(imageView: ImageView): Bitmap? {
            val drawable = imageView.drawable
            if (drawable == null || drawable !is BitmapDrawable) {
                return null
            }

            val bitmap = drawable.bitmap
            val imageWidth = bitmap.width
            val imageHeight = bitmap.height

            if (cropX < 0 || cropY < 0 || cropX + cropWidth > imageWidth || cropY + cropHeight > imageHeight) {
                return null
            }

            return Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
        }

        fun resetCamera(
            cameraDevice: CameraDevice,
            imageView: ImageView,
            textureView: TextureView,
            isImageLoaded: Boolean,
            hasilDetect: TextView,
            openCamera: (SurfaceTexture) -> Unit,
            runOnUiThread: (Runnable) -> Unit
        ) {
            cameraDevice.close()
            runOnUiThread(Runnable {
                imageView.visibility = View.GONE
                textureView.visibility = View.VISIBLE
                var isImageLoaded = false
                hasilDetect.text = "Hasil Deteksi"
            })
            openCamera(textureView.surfaceTexture!!)
        }


        fun setupSpinners(activity: Activity, spinnerNamaPasar: Spinner, spinnerNamaSayur: Spinner) {
            val namaPasarItems = arrayOf("Pasar Soponyono", "Pasar Wadung Asri", "Pasar Rungkut Kidul")
            val namaSayurItems = arrayOf("Timun", "Wortel", "Kubis", "Brokoli", "Selada")

            val pasarAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, namaPasarItems)
            val sayurAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, namaSayurItems)

            pasarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sayurAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerNamaPasar.adapter = pasarAdapter
            spinnerNamaSayur.adapter = sayurAdapter
        }

        fun processActivityResult(
            activity: AppCompatActivity,
            requestCode: Int,
            resultCode: Int,
            data: Intent?,
            imageView: ImageView,
            textureView: View,
            isImageLoaded: Boolean,
        ) {
            if (requestCode == REQUEST_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK) {
                val selectedImageUri: Uri? = data?.data
                try {
                    val inputStream = selectedImageUri?.let { activity.contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                    textureView.visibility = View.GONE
                    Toast.makeText(activity, "Gambar Berhasil Dimuat", Toast.LENGTH_SHORT).show()
                } catch (e: FileNotFoundException) {
                    Toast.makeText(activity, "Terjadi kesalahan saat memilih gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun saveToSQLITE(context: Context, imageByteArray: ByteArray, detectionResult: String) {
            try {
                val dbHelper = SQLITE(context)
                val db = dbHelper.writableDatabase

                val values = ContentValues().apply {
                    put(SQLITE.COL_IMAGE, imageByteArray)
                    put(SQLITE.COL_DETECTION_RESULT, detectionResult)
                }

                val newRowId = db?.insert(SQLITE.TABLE_NAME, null, values)

                if (newRowId != -1L) {
                    Toast.makeText(context, "Data berhasil disimpan di database lokal", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal menyimpan ke database lokal", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        fun captureImageFromCamera(
            mainActivity: MainActivity,
            isTextureViewVisible: Boolean,
            cameraDevice: CameraDevice,
            imageReader: ImageReader,
            textureView: TextureView,
            cameraOrientation: Int,
            isImageLoaded: Boolean,
            handler: Handler
        ) {
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
                                            mainActivity.runOnUiThread {
                                                val capturedBitmap = textureView.bitmap
                                                if (capturedBitmap != null) {
                                                    Toast.makeText(mainActivity, "Gambar berhasil Diambil", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(mainActivity, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show()
                                                }
                                                mainActivity.isImageLoaded = true
                                            }
                                        }

                                        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
                                            mainActivity.runOnUiThread {
                                                Toast.makeText(mainActivity, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }, handler)
                                } catch (e: CameraAccessException) {
                                    e.printStackTrace()
                                    mainActivity.runOnUiThread {
                                        Toast.makeText(mainActivity, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {}
                        },
                        handler
                    )
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                    Toast.makeText(mainActivity, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(mainActivity, "Tidak dapat mengambil gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
