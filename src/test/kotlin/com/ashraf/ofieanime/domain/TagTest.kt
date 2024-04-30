package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class TagTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Tag::class)
        val tag1 = Tag()
        tag1.id = 1L
        val tag2 = Tag()
        tag2.id = tag1.id
        assertThat(tag1).isEqualTo(tag2)
        tag2.id = 2L
        assertThat(tag1).isNotEqualTo(tag2)
        tag1.id = null
        assertThat(tag1).isNotEqualTo(tag2)
    }
}
