package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.RangeParser
import com.example.service.StorageService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun read_string_from_context() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Offline PDF Studio", appName)
  }

  @Test
  fun test_range_parser_multi_range() {
    val result = RangeParser.parse("1-3, 5, 8-10", 10)
    // 1-3 -> 0, 1, 2
    // 5 -> 4
    // 8-10 -> 7, 8, 9
    assertEquals(listOf(0, 1, 2, 4, 7, 8, 9), result)
  }

  @Test
  fun test_range_parser_presets() {
    val all = RangeParser.parse("all", 5)
    assertEquals(listOf(0, 1, 2, 3, 4), all)

    val odd = RangeParser.parse("odd", 5)
    assertEquals(listOf(0, 2, 4), odd)

    val even = RangeParser.parse("even", 5)
    assertEquals(listOf(1, 3), even)
  }

  @Test
  fun test_storage_service_format_bytes() {
    assertEquals("0 B", StorageService.formatBytes(0))
    assertEquals("500 B", StorageService.formatBytes(500))
    assertTrue(StorageService.formatBytes(2048).contains("KB"))
    assertTrue(StorageService.formatBytes(5 * 1024 * 1024).contains("MB"))
  }
}
