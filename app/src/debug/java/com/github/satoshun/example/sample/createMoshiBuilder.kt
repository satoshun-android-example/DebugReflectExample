package com.github.satoshun.example.sample

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

fun createMoshiBuilder() = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
