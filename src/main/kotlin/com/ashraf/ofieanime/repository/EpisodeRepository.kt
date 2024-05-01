package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Episode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Episode entity.
 */
@Suppress("unused")
@Repository
interface EpisodeRepository : JpaRepository<Episode, Long>
