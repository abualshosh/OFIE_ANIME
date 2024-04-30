package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Profile entity.
 */
@Suppress("unused")
@Repository
interface ProfileRepository : JpaRepository<Profile, Long> {
}
