package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aistudio.policedirectory.zxklm.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ALL Police Contact", appName)
    try {
      val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
      println("FirebaseAuth instance: $auth")
    } catch (t: Throwable) {
      println("FirebaseAuth failed with: ${t.javaClass.name}: ${t.message}")
      t.printStackTrace()
    }
  }

  @Test
  fun `launch MainActivity`() {
    org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
  }
}
