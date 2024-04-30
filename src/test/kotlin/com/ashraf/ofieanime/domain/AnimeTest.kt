package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class AnimeTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Anime::class)
        val anime1 = Anime()
        anime1.id = 1L
        val anime2 = Anime()
        anime2.id = anime1.id
        assertThat(anime1).isEqualTo(anime2)
        anime2.id = 2L
        assertThat(anime1).isNotEqualTo(anime2)
        anime1.id = null
        assertThat(anime1).isNotEqualTo(anime2)
    }
}
