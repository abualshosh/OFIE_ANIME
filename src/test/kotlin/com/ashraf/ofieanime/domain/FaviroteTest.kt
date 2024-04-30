package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class FaviroteTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Favirote::class)
        val favirote1 = Favirote()
        favirote1.id = 1L
        val favirote2 = Favirote()
        favirote2.id = favirote1.id
        assertThat(favirote1).isEqualTo(favirote2)
        favirote2.id = 2L
        assertThat(favirote1).isNotEqualTo(favirote2)
        favirote1.id = null
        assertThat(favirote1).isNotEqualTo(favirote2)
    }
}
