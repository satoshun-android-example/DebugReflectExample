package com.github.satoshun.example.sample

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.github.satoshun.example.sample.databinding.MainActBinding
import com.squareup.moshi.JsonClass

class MainActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = DataBindingUtil.setContentView<MainActBinding>(this, R.layout.main_act)

    val component = DaggerAppComponent.create()
    component.handler().showName()
    component.handler().showName()
    component.handler().showName()

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
}

@JsonClass(generateAdapter = true)
data class User(
  val firstName: String,
  val lastName: String,
  val other: String = "hoge"
)
