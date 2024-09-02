package com.nikichxp.tgbot

import com.nikichxp.tgbot.util.ChatCommandParser
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
}