package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Studio
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Studio entity.
 */
@Suppress("unused")
@Repository
interface StudioRepository : JpaRepository<Studio, Long>
