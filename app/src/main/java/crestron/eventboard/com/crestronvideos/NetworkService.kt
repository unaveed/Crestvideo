package crestron.eventboard.com.crestronvideos

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.net.URL


class NetworkService {
  private val client = OkHttpClient.Builder().build()
  private lateinit var retrofit: Retrofit

  fun getVideo(url: String): Observable<ResponseBody> {
    val api = getRetrofitClient(url).create(Api::class.java)
    return api.getVideo(getPath(url))
  }

  private fun getRetrofitClient(url: String): Retrofit {
    retrofit = Retrofit.Builder().client(client).baseUrl(getBaseUrl(url)).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
    return retrofit
  }

  private fun getBaseUrl(urlStr: String): String {
    val url = URL(urlStr)
    return "${url.protocol}://${url.host}"
  }

  private fun getPath(urlStr: String): String {
    val url = URL(urlStr)
    return url.path
  }
}
