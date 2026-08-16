package com.sarah.shared

import kotlin.test.Test
import kotlin.test.assertTrue

class SarahSharedGreetingTest {

    @Test
    fun testGreetContainsSarah() {
        val greeting = SarahSharedGreeting().greet()
        assertTrue(greeting.contains("Sarah KMP"), "Greeting should mention Sarah KMP")
    }
}
