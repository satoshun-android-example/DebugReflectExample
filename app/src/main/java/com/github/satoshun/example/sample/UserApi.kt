package com.github.satoshun.example.sample

import retrofit2.http.GET

interface UserApi {
  @GET("user")
  fun getUser(): retrofit2.Call<User>
}
