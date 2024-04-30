package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class SeasonTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Season::class)
        val season1 = Season()
        season1.id = 1L
        val season2 = Season()
        season2.id = season1.id
        assertThat(season1).isEqualTo(season2)
        season2.id = 2L
        assertThat(season1).isNotEqualTo(season2)
        season1.id = null
        assertThat(season1).isNotEqualTo(season2)
    }
}
