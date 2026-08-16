package com.sarah.shared

class SarahSharedGreeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello from Sarah KMP on ${platform.name}!"
    }
}
