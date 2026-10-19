package com.chukchukhaksa.mobile.presentation.portallink.component

import kotlin.test.Test
import kotlin.test.assertEquals

class KeepAllTest {

  @Test
  fun `word joiner is inserted only between adjacent hangul syllables`() {
    assertEquals("학⁠교 정⁠보⁠를", "학교 정보를".keepAll())
  }

  @Test
  fun `spaces punctuation latin and line breaks keep their break opportunities`() {
    assertEquals("(ex. 신⁠입⁠생,\n편⁠입⁠생)", "(ex. 신입생,\n편입생)".keepAll())
    assertEquals("3분 이⁠상", "3분 이상".keepAll())
  }

  @Test
  fun `removing word joiners restores the original text`() {
    val text = "계정이 잠겼습니다. 포털사이트로 돌아가서 학번/사번 찾기 및 비밀번호 재발급을 진행해주세요"
    assertEquals(text, text.keepAll().replace("⁠", ""))
  }
}
