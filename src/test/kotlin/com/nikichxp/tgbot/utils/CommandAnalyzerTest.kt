package com.nikichxp.tgbot.utils

import com.nikichxp.tgbot.core.util.ChatCommandParser
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommandAnalyzerTest {

    @Test
    fun `test parsing tool`() = runBlocking {
        var test: String? = null
        val result = ChatCommandParser.analyze("test 123 demo") {
            path("test") {
                asArg("id") {
                    path("demo") {
                        test = vars["id"]
                    }
                }
            }
        }
        assertTrue(result)
        assertEquals(test, "123")
    }

    @Test
    fun `test parsing tool in wrong case`() = runBlocking {
        var test: String? = null
        val result = ChatCommandParser.analyze("test 123 demo") {
            path("test") {
                asArg("id") {
                    path("other") {
                        test = vars["id"]
                    }
                }
            }
        }
        assertFalse(result)
        assertNull(test)
    }

    @Test
    fun `test independent segments are both matched regardless of noise and order`() = runBlocking {
        val map = mutableMapOf<String, String>()

        suspend fun parse(tokens: List<String>) = ChatCommandParser.analyze(tokens) {
            path("foo") {
                asArg("foo") {
                    vars["foo"]?.let { map["foo"] = it }
                }
            }
            path("bar") {
                asArg("bar") {
                    vars["bar"]?.let { map["bar"] = it }
                }
            }
        }

        val result = parse("something foo 123 kek 456 bar 789".split(" "))
        assertTrue(result)
        assertEquals("123", map["foo"])
        assertEquals("789", map["bar"])

        map.clear()
        val resultReversed = parse("bar 789 foo 123".split(" "))
        assertTrue(resultReversed)
        assertEquals("123", map["foo"])
        assertEquals("789", map["bar"])
    }

    @Test
    fun `test only one of independent segments present still succeeds`() = runBlocking {
        val map = mutableMapOf<String, String>()

        val result = ChatCommandParser.analyze("foo 123".split(" ")) {
            path("foo") {
                asArg("foo") {
                    vars["foo"]?.let { map["foo"] = it }
                }
            }
            path("bar") {
                asArg("bar") {
                    vars["bar"]?.let { map["bar"] = it }
                }
            }
        }

        assertTrue(result)
        assertEquals("123", map["foo"])
        assertNull(map["bar"])
    }
}