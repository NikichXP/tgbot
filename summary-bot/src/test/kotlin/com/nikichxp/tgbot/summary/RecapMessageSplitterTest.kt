package com.nikichxp.tgbot.summary

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RecapMessageSplitterTest {

    @Test
    fun shortTextStaysInOneMessage() {
        assertThat(RecapMessageSplitter.split("a\n\nb", maxLength = 10)).containsExactly("a\n\nb")
    }

    @Test
    fun packsParagraphsIntoFewestMessages() {
        val parts = RecapMessageSplitter.split("aaa\n\nbbb\n\nccc\n\nddd", maxLength = 10)

        assertThat(parts).containsExactly("aaa\n\nbbb", "ccc\n\nddd")
    }

    @Test
    fun splitsParagraphWithoutBlankLinesAtFarthestPeriod() {
        val parts = RecapMessageSplitter.split("Aa. Bb. Cc. Dd.", maxLength = 10)

        assertThat(parts).containsExactly("Aa. Bb.", "Cc. Dd.")
    }

    @Test
    fun hardSplitsParagraphWithoutBlankLinesAndPeriods() {
        val parts = RecapMessageSplitter.split("x".repeat(25), maxLength = 10)

        assertThat(parts).allSatisfy { assertThat(it).hasSizeLessThan(10) }
        assertThat(parts.joinToString("")).isEqualTo("x".repeat(25))
    }
}
