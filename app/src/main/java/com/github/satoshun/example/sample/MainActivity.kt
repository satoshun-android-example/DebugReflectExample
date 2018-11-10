package com.github.satoshun.example.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.github.satoshun.example.sample.databinding.MainActBinding
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import retrofit2.http.GET
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(),
    CoroutineScope {

  private val job = Job()
  override val coroutineContext: CoroutineContext get() = job + Dispatchers.Main

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = DataBindingUtil.setContentView<MainActBinding>(this, R.layout.main_act)

    val component = DaggerAppComponent.create()

//    val retrofit = Retrofit.Builder()
//        .baseUrl("https://api.example.com")
//        .addConverterFactory(MoshiConverterFactory.create())
//        .build()
//    val userApi = retrofit.create(UserApi::class.java)

    val moshi = createMoshiBuilder()
    val user = moshi
        .adapter(User::class.java)
        .fromJson(
            """{
          |"firstName": "sato",
          |"lastName": "shun"
          |}""".trimMargin()
        )!!
    binding.firstName.text = user.firstName
    binding.lastName.text = user.lastName

    // access default value?
    try {
      println(user.other.toString())
    } catch (e: Exception) {
      println(e)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    job.cancel()
  }
}

interface UserApi {
  @GET("user")
  fun getUser(): retrofit2.Call<User>
}

@JsonClass(generateAdapter = true)
data class User(
  val firstName: String,
  val lastName: String,
  val other: String = "hoge"
)
