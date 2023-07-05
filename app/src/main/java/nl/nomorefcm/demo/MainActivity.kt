package nl.nomorefcm.demo

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.vanniktech.rxpermission.Permission
import com.vanniktech.rxpermission.RealRxPermission
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var MyReceiver: NotificationReceiver? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sendNotification = findViewById<Button>(R.id.sendNotification)

        RealRxPermission.getInstance(this)
            .requestEach(
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.FOREGROUND_SERVICE,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            .reduce<Boolean>(
                true
            ) { c: Boolean, p: Permission -> c && p.state() == Permission.State.GRANTED }
            .subscribe { granted: Boolean ->
                if (granted) {
                    MyReceiver = NotificationReceiver()
                    val intentFilter = IntentFilter("nl.nomorefcm.NOTIFICATION_SERVICE")
                    if (intentFilter != null) {
                        registerReceiver(MyReceiver, intentFilter)
                    }
                    startService(Intent(this, UserIdService::class.java))
                    sendNotification.setOnClickListener {
                        enviarPeticionPOST()
                    }
                    sendNotification.text = "Send Test Notification"
                } else {
                    sendNotification.text = "Enable Notifications"
                }
            }
    }

    private fun enviarPeticionPOST() {
        val url = "https://ws.nomorefcm.nl/notifications"
        val client = OkHttpClient()

        sharedPreferences = applicationContext.getSharedPreferences("UserIdPreferences", Context.MODE_PRIVATE)
        var userId = sharedPreferences.getString("UserID", null)

        val requestBody = FormBody.Builder()
            .add("recipientId", userId.toString())
            .add("title", "Welcome to NoMoreFCM")
            .add("content", "The notifications is working correctly")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MyReceiver != null) unregisterReceiver(MyReceiver)
    }
}