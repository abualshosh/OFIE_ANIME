package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class ProfileTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Profile::class)
        val profile1 = Profile()
        profile1.id = 1L
        val profile2 = Profile()
        profile2.id = profile1.id
        assertThat(profile1).isEqualTo(profile2)
        profile2.id = 2L
        assertThat(profile1).isNotEqualTo(profile2)
        profile1.id = null
        assertThat(profile1).isNotEqualTo(profile2)
    }
}
