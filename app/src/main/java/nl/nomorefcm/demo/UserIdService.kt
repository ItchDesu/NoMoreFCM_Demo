package nl.nomorefcm.demo

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import java.util.*

class UserIdService : Service() {

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()

    val userId = generateUserID(applicationContext)

    val intent = Intent()
    intent.action = "nl.nomorefcm.USER_ID"
    intent.putExtra("userId", userId)
    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
    applicationContext.sendBroadcast(intent)

  }

  companion object {
    private const val TAG = "NotificationService"
    private const val NOTIFICATION_ID = 1
  }

  private fun generateUserID(context: Context): String {
    val sharedPreferences = context.getSharedPreferences("UserIdPreferences", Context.MODE_PRIVATE)
    var userId = sharedPreferences.getString("UserID", null)

    if (userId == null) {
      // Generar un nuevo UserID único
      userId = UUID.randomUUID().toString()

      // Guardar el UserID en la caché
      val editor: SharedPreferences.Editor = sharedPreferences.edit()
      editor.putString("UserID", userId)
      editor.apply()
    }

    return userId
  }
}