package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Favirote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Favirote entity.
 */
@Suppress("unused")
@Repository
interface FaviroteRepository : JpaRepository<Favirote, Long> {
}
