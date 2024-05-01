package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Character
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Character entity.
 */
@Suppress("unused")
@Repository
interface CharacterRepository : JpaRepository<Character, Long>
