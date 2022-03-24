package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var drawpath: CustomPath? = null
    private var bitmap: Bitmap? = null
    private var paint: Paint? = null
    private var canvasPaint: Paint? = null
    private var brushSize: Float = 0f
    private var color = Color.RED
    private var canvas: Canvas? = null
    private val allPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        paint = Paint()
        paint!!.color = color
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeJoin = Paint.Join.ROUND
        paint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
        drawpath = CustomPath(color, brushSize)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap!!, 0f, 0f, canvasPaint)
        for (path in allPaths) {
            paint!!.strokeWidth = path.thickness
            paint!!.color = path.color
            canvas.drawPath(path, paint!!)
        }
        if (!drawpath!!.isEmpty) {
            paint!!.strokeWidth = drawpath!!.thickness
            paint!!.color = drawpath!!.color
            canvas.drawPath(drawpath!!, paint!!)

        }
        Log.i("GGS","FSFSF")
        Log.e("GGS","FSFSF")
        Log.w("GGS","FSFSF")
        Log.v("GGS","FSFSF")
        Log.d("GGS","FSFSF")
    }
    fun undous()
    {

        if(allPaths.size>0)
        allPaths.removeLast()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawpath!!.color = color
                drawpath!!.thickness = brushSize
                drawpath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        drawpath!!.moveTo(touchX, touchY)
                    }
                }

            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        drawpath!!.lineTo(touchX, touchY)
                    }
                }

            }
            MotionEvent.ACTION_UP -> {
                allPaths.add(drawpath!!)
                drawpath = CustomPath(color, brushSize)
            }
            else -> return false


        }
        invalidate()
        return true
    }

    fun setSizeforBrush(newSize: Float) {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        paint!!.strokeWidth = brushSize
    }

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        paint!!.color = color
    }

    fun setcolor2(colo: Int) {
        paint!!.color = colo
        color = colo
    }


    internal class CustomPath(var color: Int, var thickness: Float) : Path() {

    }
}