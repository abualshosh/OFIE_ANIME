package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Tag entity.
 */
@Suppress("unused")
@Repository
interface TagRepository : JpaRepository<Tag, Long>
