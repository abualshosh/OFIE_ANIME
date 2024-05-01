package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Source
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Source entity.
 */
@Suppress("unused")
@Repository
interface SourceRepository : JpaRepository<Source, Long>
