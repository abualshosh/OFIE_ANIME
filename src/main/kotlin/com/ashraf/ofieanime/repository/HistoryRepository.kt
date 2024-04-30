package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.History
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the History entity.
 */
@Suppress("unused")
@Repository
interface HistoryRepository : JpaRepository<History, Long> {
}
