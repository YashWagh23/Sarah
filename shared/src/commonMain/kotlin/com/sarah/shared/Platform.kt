package com.sarah.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
