package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.UrlLink
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the UrlLink entity.
 */
@Suppress("unused")
@Repository
interface UrlLinkRepository : JpaRepository<UrlLink, Long>
