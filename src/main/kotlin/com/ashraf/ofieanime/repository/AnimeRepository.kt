package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Anime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Anime entity.
 */
@Suppress("unused")
@Repository
interface AnimeRepository : JpaRepository<Anime, Long>
