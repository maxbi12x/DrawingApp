package com.example.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.security.identity.ResultData
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.defaults.colorpicker.ColorPickerPopup
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var imgBut: ImageButton? = null
    var customProgressDialog: Dialog? = null




    private val openGalleryActivity: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val iv_background = findViewById<ImageView>(R.id.iv_background)
                iv_background.setImageURI(result.data?.data)
            }
        }


    private val storageResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this@MainActivity,
                            "Access Granted For Storage",
                            Toast.LENGTH_LONG
                        ).show()
                        val pickIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryActivity.launch(pickIntent)
                    }
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this@MainActivity,
                            "Access Denied For Storage",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawingView)
        drawingView?.setSizeforBrush(10f)

        val ll: LinearLayout = findViewById(R.id.color_list)
        imgBut = ll[1] as ImageButton
        imgBut!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_active))
        drawingView!!.setColor(imgBut!!.tag.toString())
        val ib_brush = findViewById<ImageButton>(R.id.ib_brush)
        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
        val galleyButton = findViewById<ImageButton>(R.id.ib_gallery)
        galleyButton.setOnClickListener {
            requestStoragePermission()
        }
        val ibSave = findViewById<ImageView>(R.id.save)
        ibSave.setOnClickListener {
            if (isReadStorage()) {
                lifecycleScope.launch {
                    showProgressBar()
                    val flDrawingView: FrameLayout = findViewById(R.id.framelayout)
                    saveBitmapFile(getBitmapFromView(flDrawingView))
                }
            }
        }

    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRationaleDialog(
                "DRAWING APP",
                "Storage cannot be accessed as storage access has been denied"
            )
        } else {
            storageResultLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }


    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size : ")
        val smallbtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallbtn.setOnClickListener {
            drawingView?.setSizeforBrush(10f)
            brushDialog.dismiss()
        }
        val medbtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        medbtn.setOnClickListener {
            drawingView?.setSizeforBrush(20f)
            brushDialog.dismiss()
        }
        val largebtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largebtn.setOnClickListener {
            drawingView?.setSizeforBrush(30f)
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)
        return returnedBitmap

    }

    fun selectedPaint(view: View) {
        if (imgBut != view) {
            imgBut!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet))
            imgBut = view as ImageButton
            imgBut!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_active))
            drawingView!!.setColor(imgBut!!.tag.toString())
        }

    }

    fun randomColor(v: View) {
        imgBut!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet))
        imgBut = v as ImageButton
        imgBut!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_active))

        class over() : ColorPickerPopup.ColorPickerObserver() {
            override fun onColorPicked(colr: Int) {
                imgBut!!.tag = colr.toString()
                imgBut!!.setBackgroundColor(colr)
                drawingView!!.setcolor2(colr)
            }
        }
        ColorPickerPopup.Builder(this).initialColor(Color.RED).enableAlpha(true).okTitle("Choose")
            .cancelTitle("Cancel").showIndicator(true).showValue(true).build().show(null, over())


    }

    fun undo(x: View) {
        drawingView!!.undous()
    }

    private fun isReadStorage(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun showRationaleDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Positive") { dial, which ->
                Toast.makeText(this, "pressed Positive", Toast.LENGTH_LONG).show()
                dial.dismiss()
            }
            .setNegativeButton("Negative") { dial, which ->
                Toast.makeText(this, "pressed Negative", Toast.LENGTH_LONG).show()
                dial.dismiss()
            }.setNeutralButton("Neutral") { dial, which ->
                Toast.makeText(this, "pressed Neutral", Toast.LENGTH_LONG).show()
                dial.dismiss()
            }
        builder.create().show()
    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (null != mBitmap) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap?.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(
                        externalCacheDir?.absoluteFile.toString() +
                                File.separator + "DrawingApp" + System.currentTimeMillis() / 1000 + ".png"
                    )
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                    runOnUiThread {
                        endProgressBar()
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File Saved Successfully : $result",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            shareFile(result)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "OOPS!! Something Went Wrong, File Cannot be Saved",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }

        }
        return result
    }

    private fun showProgressBar() {
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog?.setContentView(R.layout.custom_progress)
        customProgressDialog!!.show()
    }

    private fun endProgressBar() {
        if (customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

    private fun shareFile(result: String) {
        MediaScannerConnection.scanFile(this, arrayOf(result), null) { path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent, "Share"))
        }
    }

}