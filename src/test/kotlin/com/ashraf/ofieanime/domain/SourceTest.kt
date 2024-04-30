package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class SourceTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Source::class)
        val source1 = Source()
        source1.id = 1L
        val source2 = Source()
        source2.id = source1.id
        assertThat(source1).isEqualTo(source2)
        source2.id = 2L
        assertThat(source1).isNotEqualTo(source2)
        source1.id = null
        assertThat(source1).isNotEqualTo(source2)
    }
}
