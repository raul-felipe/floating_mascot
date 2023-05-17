package com.example.floatingmascot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.math.abs



class FloatingService : Service() {

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

    var isFloatingserviceRunning = false



    lateinit var intent :Intent

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
                    Log.d("CREATED", "${this.toString()}")
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

    private val binder = FloatingBinder()

    inner class FloatingBinder : Binder() {
        fun getService(): FloatingService{
            return this@FloatingService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!isFloatingserviceRunning) stopSelf()
        generateForegroundNotification()
        isFloatingserviceRunning = true
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        dismiss()
        stopSelf()
        isShowing = false
        isFloatingserviceRunning=false
    }

    override fun onCreate() {
        super.onCreate()
        isFloatingserviceRunning = true
        floatView = LayoutInflater.from(this).inflate(R.layout.floating_window, null)
        floatView.setOnTouchListener(onTouchListener)

        intent = this.packageManager.getLaunchIntentForPackage(this.packageName)!!
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


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
        }

        show()

    }

    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 1

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                val notificationChannel =
                    NotificationChannel("service_channel", "Service Notifications",
                        NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")
            builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" service is running").toString())
                .setTicker(StringBuilder(resources.getString(R.string.app_name)).append("service is running").toString())
                .setContentText("Touch to open") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }
            builder.color = resources.getColor(androidx.appcompat.R.color.material_blue_grey_800)
            notification = builder.build()
            startForeground(mNotificationId, notification)
        }

    }

    fun show() {
        dismiss()
        gif = floatView.findViewById(R.id.floating_gif)
        gifOverlay = floatView.findViewById(R.id.overlay_floating_gif)
        gif.setImageResource(gifMap.START)
        gif.tag = gifMap.START
        isShowing = true
        windowManager?.addView(floatView, layoutParams)
        //generateForegroundNotification()
    }

    fun dismiss() {
        if (isShowing) {
            windowManager?.removeView(floatView)
            isShowing = false
            isFloatingserviceRunning =false
            stopSelf()
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



//package com.example.floatingmascot
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationChannelGroup
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.Service
//import android.content.Context
//import android.content.Context.WINDOW_SERVICE
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.PixelFormat
//import android.os.Build
//import android.os.IBinder
//import android.view.*
//import androidx.annotation.RequiresApi
//import androidx.core.app.NotificationCompat
//import androidx.core.content.ContextCompat.startActivity
//import androidx.core.content.res.TypedArrayUtils.getText
//import kotlin.math.abs
//
//
//class FloatingService constructor(private val context: Context): Service(){
//
//
//    private var gifMap : GifMap = GifMap()
//    private var windowManager: WindowManager? = null
//        get() {
//            if (field == null) field = (context.getSystemService(WINDOW_SERVICE) as WindowManager)
//            return field
//        }
//
//    private var floatView: View = LayoutInflater.from(context).inflate(R.layout.floating_window, null)
//    private lateinit var gif: pl.droidsonroids.gif.GifImageView
//    private lateinit var gifOverlay: pl.droidsonroids.gif.GifImageView
//    private lateinit var layoutParams: WindowManager.LayoutParams
//
//    private var lastX: Int = 0
//    private var lastY: Int = 0
//    private var firstX: Int = 0
//    private var firstY: Int = 0
//
//    var isShowing = false
//    private var touchConsumedByMove = false
//
//    private val onTouchListener = View.OnTouchListener { view, event ->
//        val totalDeltaX = lastX - firstX
//        val totalDeltaY = lastY - firstY
//
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                lastX = event.rawX.toInt()
//                lastY = event.rawY.toInt()
//                firstX = lastX
//                firstY = lastY
//            }
//            MotionEvent.ACTION_UP -> {
//                view.performClick()
//                if(abs(totalDeltaX) < 5 && abs(totalDeltaY) < 5){
//                    var intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
//                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(context,intent!!,null)
//                }
//            }
//            MotionEvent.ACTION_MOVE -> {
//                val deltaX = event.rawX.toInt() - lastX
//                val deltaY = event.rawY.toInt() - lastY
//                lastX = event.rawX.toInt()
//                lastY = event.rawY.toInt()
//                if (abs(totalDeltaX) >= 5 || abs(totalDeltaY) >= 5) {
//                    if (event.pointerCount == 1) {
//                        layoutParams.x += deltaX
//                        layoutParams.y += deltaY
//                        touchConsumedByMove = true
//                        windowManager?.apply {
//                            updateViewLayout(floatView, layoutParams)
//                        }
//                    } else {
//                        touchConsumedByMove = false
//                    }
//                } else {
//                    touchConsumedByMove = false
//                }
//            }
//            else -> {
//            }
//        }
//        touchConsumedByMove
//    }
//
//    init {
////        with(floatView) {
////            textView.text = "I'm a float view!"
////        }
//
//        floatView.setOnTouchListener(onTouchListener)
//
//        layoutParams = WindowManager.LayoutParams().apply {
//            format = PixelFormat.TRANSLUCENT
//            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//
//            @Suppress("DEPRECATION")
//            type = when {
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
//                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//                else -> WindowManager.LayoutParams.TYPE_TOAST
//            }
//
//            gravity = Gravity.CENTER
//            width = WindowManager.LayoutParams.WRAP_CONTENT
//            height = WindowManager.LayoutParams.WRAP_CONTENT
//            //token =
//
//        }
//    }
//
//    private fun startMyForegroudService(){
//        var intent = Intent(this, MainActivity::class.java)
//
//        val pendingIntent: PendingIntent =
//            intent.let { notificationIntent ->
//                PendingIntent.getActivity(this, 0, notificationIntent,
//                    PendingIntent.FLAG_MUTABLE)
//            }
//
//        val notification: Notification = Notification.Builder(this, context.packageName)
//            .setContentTitle("Your mascot is wake up")
//            //.setContentText(getText(R.string.notification_message))
//            //.setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentIntent(pendingIntent)
//            //.setTicker(getText(R.string.ticker_text))
//            .build()
//
//        // Notification ID cannot be 0.
//        startForeground(1,notification)
//    }
//
//
//    fun show() {
//        dismiss()
//        startMyForegroudService()
//        gif = floatView.findViewById(R.id.floating_gif)
//        gifOverlay = floatView.findViewById(R.id.overlay_floating_gif)
//        //gif.setBackgroundResource(gifMap.START)
//        gif.setImageResource(gifMap.START)
//        gif.tag = gifMap.START
//        isShowing = true
//        windowManager?.addView(floatView, layoutParams)
//
//    }
//
//    fun dismiss() {
//        if (isShowing) {
//            stopForeground(STOP_FOREGROUND_REMOVE)
//            windowManager?.removeView(floatView)
//            isShowing = false
//        }
//    }
//
//    fun updateGif(drawable: Int){
//        if(!isSameBackgroud(drawable)) {
//            //gif.setBackgroundResource(drawable)
//            gif.setImageResource(drawable)
//            gif.tag = drawable
//        }
//    }
//
//    fun updateOverlayGif(drawable: Int){
//        if(!isSameBackgroud(drawable)) {
//            //gif.setBackgroundResource(drawable)
//            gifOverlay.setImageResource(drawable)
//            //gif.tag = drawable
//        }
//    }
//
//    fun getGif():Int{
//        return gif.tag as Int;
//    }
//
//    fun isSameBackgroud(drawable: Int):Boolean{
//        return gif.tag.equals(drawable)
//        //return gif.background.constantState!!.equals(activity.resources.getDrawable(drawable,null).constantState)
//
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        TODO("Not yet implemented")
//    }
//
//}