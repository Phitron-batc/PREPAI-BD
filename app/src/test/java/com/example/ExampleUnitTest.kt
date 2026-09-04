package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPhase2UserSession() {
    val session = com.example.data.model.UserSession(
      accessToken = "test_token_123",
      refreshToken = "test_refresh",
      userId = "u123",
      email = "student@prepaibd.com",
      role = com.example.data.model.UserRole.STUDENT
    )
    assertEquals("test_token_123", session.accessToken)
    assertEquals(com.example.data.model.UserRole.STUDENT, session.role)
    assertFalse(session.role == com.example.data.model.UserRole.ADMIN)
  }

  @Test
  fun testSubscriptionTiers() {
    val tiers = com.example.data.model.SubscriptionTier.values()
    assertTrue(tiers.contains(com.example.data.model.SubscriptionTier.PREMIUM))
    assertTrue(tiers.contains(com.example.data.model.SubscriptionTier.FREE))
  }
}
