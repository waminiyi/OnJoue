package com.sotopatrick.onjoue

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform