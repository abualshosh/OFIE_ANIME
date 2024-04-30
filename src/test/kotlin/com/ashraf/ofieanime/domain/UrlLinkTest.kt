package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class UrlLinkTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(UrlLink::class)
        val urlLink1 = UrlLink()
        urlLink1.id = 1L
        val urlLink2 = UrlLink()
        urlLink2.id = urlLink1.id
        assertThat(urlLink1).isEqualTo(urlLink2)
        urlLink2.id = 2L
        assertThat(urlLink1).isNotEqualTo(urlLink2)
        urlLink1.id = null
        assertThat(urlLink1).isNotEqualTo(urlLink2)
    }
}
