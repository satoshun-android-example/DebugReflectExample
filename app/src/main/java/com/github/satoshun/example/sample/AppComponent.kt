package com.github.satoshun.example.sample

import dagger.Component
import javax.inject.Inject

@Component
interface AppComponent {
  fun hanlder(): UserHanlder
}

class UserHanlder @Inject constructor()
