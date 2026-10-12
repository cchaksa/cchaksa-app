package com.chukchukhaksa.mobile.common.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class Sha256Test {

    @Test
    fun emptyString() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            sha256Hex(""),
        )
    }

    @Test
    fun shortString() {
        // NIST example: SHA-256("abc")
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            sha256Hex("abc"),
        )
    }

    @Test
    fun testString() {
        assertEquals(
            "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
            sha256Hex("test"),
        )
    }

    @Test
    fun nistTwoBlockMessage() {
        // NIST example: 448-bit message (exactly triggers two-block padding)
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            sha256Hex("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"),
        )
    }

    @Test
    fun helloWorld() {
        assertEquals(
            "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
            sha256Hex("hello world"),
        )
    }

    @Test
    fun simpleString() {
        // 간단한 문자열 해싱 테스트
        assertEquals(
            "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae",
            sha256Hex("foo"),
        )
    }

    @Test
    fun paddingBoundary55Bytes() {
        // 55 bytes: padding 후 정확히 64 bytes (한 블록)
        val input = "a".repeat(55)
        assertEquals(
            "9f4390f8d30c2dd92ec9f095b65e2b9ae9b0a925a5258e241c9f1e910f734318",
            sha256Hex(input),
        )
    }

    @Test
    fun paddingBoundary56Bytes() {
        // 56 bytes: padding 시 두 블록 필요
        val input = "a".repeat(56)
        assertEquals(
            "b35439a4ac6f0948b6d6f9e3c6af0f5f590ce20f1bde7090ef7970686ec6738a",
            sha256Hex(input),
        )
    }

    @Test
    fun longString() {
        // 1000 bytes
        val input = "a".repeat(1000)
        assertEquals(
            "41edece42d63e8d9bf515a9ba6932e1c20cbc9f5a5d134645adb5db1b9737ea3",
            sha256Hex(input),
        )
    }
}
