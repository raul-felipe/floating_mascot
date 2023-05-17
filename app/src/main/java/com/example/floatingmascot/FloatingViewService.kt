package com.example.floatingmascot

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import kotlin.math.abs

class FloatingViewService : Service() {

    private var gifMap : GifMap = GifMap()
    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field = (this.getSystemService(WINDOW_SERVICE) as WindowManager)
            return field
        }

    lateinit var floatView: View
    private lateinit var gif: pl.droidsonroids.gif.GifImageView
    private lateinit var gifOverlay: pl.droidsonroids.gif.GifImageView
    private lateinit var layoutParams: WindowManager.LayoutParams

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var firstX: Int = 0
    private var firstY: Int = 0

    var isShowing = false
    private var touchConsumedByMove = false

    private val onTouchListener = View.OnTouchListener { view, event ->
        val totalDeltaX = lastX - firstX
        val totalDeltaY = lastY - firstY

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                firstX = lastX
                firstY = lastY
            }
            MotionEvent.ACTION_UP -> {
                view.performClick()
                if(abs(totalDeltaX) < 5 && abs(totalDeltaY) < 5){
                    var intent = this.packageManager.getLaunchIntentForPackage(this.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ContextCompat.startActivity(this, intent!!, null)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX.toInt() - lastX
                val deltaY = event.rawY.toInt() - lastY
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                if (abs(totalDeltaX) >= 5 || abs(totalDeltaY) >= 5) {
                    if (event.pointerCount == 1) {
                        layoutParams.x += deltaX
                        layoutParams.y += deltaY
                        touchConsumedByMove = true
                        windowManager?.apply {
                            updateViewLayout(floatView, layoutParams)
                        }
                    } else {
                        touchConsumedByMove = false
                    }
                } else {
                    touchConsumedByMove = false
                }
            }
            else -> {
            }
        }
        touchConsumedByMove
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")

    }

    override fun onCreate() {
        super.onCreate()
        floatView = LayoutInflater.from(this).inflate(R.layout.floating_window, null)
        floatView.setOnTouchListener(onTouchListener)

        layoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

            @Suppress("DEPRECATION")
            type = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else -> WindowManager.LayoutParams.TYPE_TOAST
            }

            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            //token =

        }

        show()

    }


    fun show() {
        //dismiss()
        gif = floatView.findViewById(R.id.floating_gif)
        gifOverlay = floatView.findViewById(R.id.overlay_floating_gif)
        gif.setImageResource(gifMap.START)
        gif.tag = gifMap.START
        isShowing = true
        windowManager?.addView(floatView, layoutParams)
    }

    fun dismiss() {
        if (isShowing) {
            windowManager?.removeView(floatView)
            isShowing = false
        }
    }

    fun updateGif(drawable: Int){
        if(!isSameBackgroud(drawable)) {
            //gif.setBackgroundResource(drawable)
            gif.setImageResource(drawable)
            gif.tag = drawable
        }
    }
    fun updateOverlayGif(drawable: Int){
        if(!isSameBackgroud(drawable)) {
            //gif.setBackgroundResource(drawable)
            gifOverlay.setImageResource(drawable)
            //gif.tag = drawable
        }
    }

    fun getGif():Int{
        return gif.tag as Int;
    }

    fun isSameBackgroud(drawable: Int):Boolean{
        return gif.tag.equals(drawable)
        //return gif.background.constantState!!.equals(activity.resources.getDrawable(drawable,null).constantState)

    }


}