package com.ashraf.ofieanime.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.ashraf.ofieanime.web.rest.equalsVerifier

import java.util.UUID

class YearlySeasonTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(YearlySeason::class)
        val yearlySeason1 = YearlySeason()
        yearlySeason1.id = 1L
        val yearlySeason2 = YearlySeason()
        yearlySeason2.id = yearlySeason1.id
        assertThat(yearlySeason1).isEqualTo(yearlySeason2)
        yearlySeason2.id = 2L
        assertThat(yearlySeason1).isNotEqualTo(yearlySeason2)
        yearlySeason1.id = null
        assertThat(yearlySeason1).isNotEqualTo(yearlySeason2)
    }
}
