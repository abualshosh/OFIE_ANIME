package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Season
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Season entity.
 */
@Suppress("unused")
@Repository
interface SeasonRepository : JpaRepository<Season, Long> {
}
