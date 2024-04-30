package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.YearlySeason
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the YearlySeason entity.
 */
@Suppress("unused")
@Repository
interface YearlySeasonRepository : JpaRepository<YearlySeason, Long> {
}
