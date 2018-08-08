package crestron.eventboard.com.crestronvideos

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.playerView
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

  private var downloadId = -1L
  private lateinit var downloadManager: DownloadManager
  private val networkService = NetworkService()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val url = "https://s3-us-west-2.amazonaws.com/assets-theme/videos/eventboard/Atlassian/80s.mp4"
//    val url = "https://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_10mb.mp4"
    networkService.getVideo(url)
        .observeOn(Schedulers.io())
        .map { responseBody: ResponseBody -> getVideoUri(responseBody.byteStream()) }
        .doOnError { t: Throwable -> Log.e("MainActivity", "Something done broke", t) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe{ uri -> startVideo(uri) }
//    statusButton.setOnClickListener {
//      if (cursor.moveToFirst()) {
//        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
//        val status = cursor.getInt(columnIndex)
//        val statusText = when(status) {
//          DownloadManager.STATUS_FAILED -> "Failed"
//          DownloadManager.STATUS_PAUSED -> "Paused"
//          DownloadManager.STATUS_PENDING -> "Pending"
//          DownloadManager.STATUS_RUNNING -> "Running"
//          DownloadManager.STATUS_SUCCESSFUL -> "Successful"
//          else -> "Unknown"
//        }
//        Toast.makeText(this@MainActivity, "Download status: $statusText", LENGTH_LONG).show()
//      }
//    }
  }

  private fun startVideo(videoUri: Uri?) {
    videoUri?.let {
      playerView.setVideoURI(videoUri)
      playerView.start()
    }
  }

  private fun getVideoUri(inputStream: InputStream): Uri? {
    try {
      val path = "${application.getExternalFilesDir(null)}${File.separator}eb_videos${File.separator}"
      val dir = File(path)
      if (!dir.exists()) dir.mkdirs()
      val file = File(dir, "video_file.mp4")
      if (!file.exists()) file.createNewFile()
      val outputStream = FileOutputStream(file)
      val buffer = ByteArray(4 * 1024)
      var read = inputStream.read(buffer)
      while (read != -1) {
        outputStream.write(buffer, 0, read)
        read = inputStream.read(buffer)
      }

      outputStream.flush()
      outputStream.close()

      return Uri.parse("${application.getExternalFilesDir(null)}${File.separator}eb_videos${File.separator}video_file.mp4")
    } catch (e: IOException) {
      Log.e("MainActivity", "Couldn't render video", e)
      return null
    }
  }

  private fun getDownloadStatus(downloadId: Long): Cursor {
    val videoQuery = DownloadManager.Query()
    videoQuery.setFilterById(downloadId)
    return downloadManager.query(videoQuery)
  }

  private fun startDownload(url: String): Long {
    val videoUri = Uri.parse(url)
    val request = DownloadManager.Request(videoUri)
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
    request.setTitle("Movie download")
    request.setDescription("Downloading a movie")
    request.setVisibleInDownloadsUi(false)
    request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "movie.mp4")
    val intent = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    registerReceiver(CrestronBroadcastReceiver(), intent)
    val id = downloadManager.enqueue(request)
    Toast.makeText(this, "Download id is $id", Toast.LENGTH_LONG).show()
    return id
  }

}
