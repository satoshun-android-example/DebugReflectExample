package com.github.satoshun.example.sample

import dagger.Component
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Component
interface AppComponent {
  fun handler(): UserHanlderWrapper
}

@Singleton
class UserHanlder @Inject constructor() {
  private var c: Int = 0

  fun count(): String {
    c += 1
    return c.toString()
  }
}

class UserHanlderWrapper @Inject constructor(
  private val handler: UserHanlder
) {
  fun showName() {
    println(handler.count())
  }
}
