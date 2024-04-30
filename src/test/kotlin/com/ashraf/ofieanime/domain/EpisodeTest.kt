package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class EpisodeTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Episode::class)
        val episode1 = Episode()
        episode1.id = 1L
        val episode2 = Episode()
        episode2.id = episode1.id
        assertThat(episode1).isEqualTo(episode2)
        episode2.id = 2L
        assertThat(episode1).isNotEqualTo(episode2)
        episode1.id = null
        assertThat(episode1).isNotEqualTo(episode2)
    }
}
