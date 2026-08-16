package com.sarah.app.domain.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeEpochMs(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
