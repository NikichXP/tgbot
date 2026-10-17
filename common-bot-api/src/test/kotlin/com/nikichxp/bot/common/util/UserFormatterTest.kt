package com.nikichxp.bot.common.util

import com.nikichxp.bot.common.entity.model.UserModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserFormatterTest {

    private val withUsername = UserModel(id = 1, username = "alice", fullName = "Alice Smith")
    private val withoutUsername = UserModel(id = 2, username = null, fullName = "Bob")

    @Test
    fun displayNamePrefersUsernameWithoutAt() {
        assertThat(UserFormatter.displayName(withUsername)).isEqualTo("alice")
        assertThat(UserFormatter.displayName(withoutUsername)).isEqualTo("Bob")
    }

    @Test
    fun mentionAddsAtOnlyToUsername() {
        assertThat(UserFormatter.mention(withUsername)).isEqualTo("@alice")
        assertThat(UserFormatter.mention(withoutUsername)).isEqualTo("Bob")
    }

    @Test
    fun diagnosticListsPrefixedFieldsAndSkipsMissingUsername() {
        assertThat(UserFormatter.diagnostic(withUsername, "from"))
            .isEqualTo("from.id:1 | from.username:alice | from.fullName:Alice Smith")
        assertThat(UserFormatter.diagnostic(withoutUsername, "replyTo"))
            .isEqualTo("replyTo.id:2 | replyTo.fullName:Bob")
    }
}
