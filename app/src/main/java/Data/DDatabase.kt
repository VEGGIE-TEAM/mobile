package Data

import java.io.File

data class DDatabase(
    val nama_sayur: String,
    val nama_pasar: String,
    val gambar_sayur: File,
    val tanggal_input: String,
    val hasil_deteksi: String
)