package crestron.eventboard.com.crestronvideos

import android.app.DownloadManager.EXTRA_DOWNLOAD_ID
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast


class CrestronBroadcastReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    val refId = intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1) ?: -1
    Toast.makeText(context, "Download complete for $refId", Toast.LENGTH_LONG).show()
    Log.w("Hello Kitty", "Download complete for $refId")
  }
}
