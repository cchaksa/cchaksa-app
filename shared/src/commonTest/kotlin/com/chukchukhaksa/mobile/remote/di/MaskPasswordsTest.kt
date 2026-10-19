package com.chukchukhaksa.mobile.remote.di

import kotlin.test.Test
import kotlin.test.assertEquals

class MaskPasswordsTest {

  @Test
  fun `password value is masked and other fields are kept`() {
    assertEquals(
      """{"portal_type":"suwon","username":"20231234","password":"***"}""",
      """{"portal_type":"suwon","username":"20231234","password":"p@ss \"word\""}""".maskPasswords(),
    )
  }

  @Test
  fun `body without password is unchanged`() {
    val body = """{"refreshToken":"abc"}"""
    assertEquals(body, body.maskPasswords())
  }
}
