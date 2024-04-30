package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class CharacterTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Character::class)
        val character1 = Character()
        character1.id = 1L
        val character2 = Character()
        character2.id = character1.id
        assertThat(character1).isEqualTo(character2)
        character2.id = 2L
        assertThat(character1).isNotEqualTo(character2)
        character1.id = null
        assertThat(character1).isNotEqualTo(character2)
    }
}
