package com.ashraf.ofieanime.repository

import com.ashraf.ofieanime.domain.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Comment entity.
 */
@Suppress("unused")
@Repository
interface CommentRepository : JpaRepository<Comment, Long>
