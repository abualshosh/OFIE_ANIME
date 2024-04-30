package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class HistoryTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(History::class)
        val history1 = History()
        history1.id = 1L
        val history2 = History()
        history2.id = history1.id
        assertThat(history1).isEqualTo(history2)
        history2.id = 2L
        assertThat(history1).isNotEqualTo(history2)
        history1.id = null
        assertThat(history1).isNotEqualTo(history2)
    }
}
