package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class StudioTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Studio::class)
        val studio1 = Studio()
        studio1.id = 1L
        val studio2 = Studio()
        studio2.id = studio1.id
        assertThat(studio1).isEqualTo(studio2)
        studio2.id = 2L
        assertThat(studio1).isNotEqualTo(studio2)
        studio1.id = null
        assertThat(studio1).isNotEqualTo(studio2)
    }
}
