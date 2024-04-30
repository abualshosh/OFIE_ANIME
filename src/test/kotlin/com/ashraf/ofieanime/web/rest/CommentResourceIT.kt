package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Comment
import com.ashraf.ofieanime.repository.CommentRepository
import com.ashraf.ofieanime.repository.search.CommentSearchRepository
import kotlin.test.assertNotNull
import org.assertj.core.util.IterableUtil
import org.apache.commons.collections4.IterableUtils
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Base64Utils
import org.springframework.validation.Validator
import javax.persistence.EntityManager
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Stream

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import org.awaitility.Awaitility.await


/**
 * Integration tests for the [CommentResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CommentResourceIT {
    @Autowired
    private lateinit var commentRepository: CommentRepository
    @Autowired
    private lateinit var commentSearchRepository: CommentSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restCommentMockMvc: MockMvc

    private lateinit var comment: Comment


    @AfterEach
    fun cleanupElasticSearchRepository() {
        commentSearchRepository.deleteAll()
        assertThat(commentSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        comment = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createComment() {
        val databaseSizeBeforeCreate = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll())
        // Create the Comment
        restCommentMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(comment))
        ).andExpect(status().isCreated)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testComment = commentList[commentList.size - 1]

        assertThat(testComment.comment).isEqualTo(DEFAULT_COMMENT)
        assertThat(testComment.like).isEqualTo(DEFAULT_LIKE)
        assertThat(testComment.disLike).isEqualTo(DEFAULT_DIS_LIKE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCommentWithExistingId() {
        // Create the Comment with an existing ID
        comment.id = 1L

        val databaseSizeBeforeCreate = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restCommentMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(comment))
        ).andExpect(status().isBadRequest)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllComments() {
        // Initialize the database
        commentRepository.saveAndFlush(comment)

        // Get all the commentList
        restCommentMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comment.id?.toInt())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())))
            .andExpect(jsonPath("$.[*].like").value(hasItem(DEFAULT_LIKE)))
            .andExpect(jsonPath("$.[*].disLike").value(hasItem(DEFAULT_DIS_LIKE)))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getComment() {
        // Initialize the database
        commentRepository.saveAndFlush(comment)

        val id = comment.id
        assertNotNull(id)

        // Get the comment
        restCommentMockMvc.perform(get(ENTITY_API_URL_ID, comment.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(comment.id?.toInt()))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()))
            .andExpect(jsonPath("$.like").value(DEFAULT_LIKE))
            .andExpect(jsonPath("$.disLike").value(DEFAULT_DIS_LIKE))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingComment() {
        // Get the comment
        restCommentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingComment() {
        // Initialize the database
        commentRepository.saveAndFlush(comment)

        val databaseSizeBeforeUpdate = commentRepository.findAll().size

        commentSearchRepository.save(comment)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll())
        // Update the comment
        val updatedComment = commentRepository.findById(comment.id).get()
        // Disconnect from session so that the updates on updatedComment are not directly saved in db
        em.detach(updatedComment)
        updatedComment.comment = UPDATED_COMMENT
        updatedComment.like = UPDATED_LIKE
        updatedComment.disLike = UPDATED_DIS_LIKE

        restCommentMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedComment.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedComment))
        ).andExpect(status().isOk)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
        val testComment = commentList[commentList.size - 1]
        assertThat(testComment.comment).isEqualTo(UPDATED_COMMENT)
        assertThat(testComment.like).isEqualTo(UPDATED_LIKE)
        assertThat(testComment.disLike).isEqualTo(UPDATED_DIS_LIKE)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val commentSearchList = IterableUtils.toList(commentSearchRepository.findAll())
            val testCommentSearch = commentSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testCommentSearch.comment).isEqualTo(UPDATED_COMMENT)
            assertThat(testCommentSearch.like).isEqualTo(UPDATED_LIKE)
            assertThat(testCommentSearch.disLike).isEqualTo(UPDATED_DIS_LIKE)
      }
    }

    @Test
    @Transactional
    fun putNonExistingComment() {
        val databaseSizeBeforeUpdate = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll())
        comment.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommentMockMvc.perform(put(ENTITY_API_URL_ID, comment.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(comment)))
            .andExpect(status().isBadRequest)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchComment() {
        val databaseSizeBeforeUpdate = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll())
        comment.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(comment))
        ).andExpect(status().isBadRequest)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamComment() {
        val databaseSizeBeforeUpdate = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll());
        comment.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(comment)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateCommentWithPatch() {
        commentRepository.saveAndFlush(comment)
        
        
val databaseSizeBeforeUpdate = commentRepository.findAll().size

// Update the comment using partial update
val partialUpdatedComment = Comment().apply {
    id = comment.id

    
        comment = UPDATED_COMMENT
        like = UPDATED_LIKE
}


restCommentMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedComment.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedComment)))
.andExpect(status().isOk)

// Validate the Comment in the database
val commentList = commentRepository.findAll()
assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
val testComment = commentList.last()
    assertThat(testComment.comment).isEqualTo(UPDATED_COMMENT)
    assertThat(testComment.like).isEqualTo(UPDATED_LIKE)
    assertThat(testComment.disLike).isEqualTo(DEFAULT_DIS_LIKE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateCommentWithPatch() {
        commentRepository.saveAndFlush(comment)
        
        
val databaseSizeBeforeUpdate = commentRepository.findAll().size

// Update the comment using partial update
val partialUpdatedComment = Comment().apply {
    id = comment.id

    
        comment = UPDATED_COMMENT
        like = UPDATED_LIKE
        disLike = UPDATED_DIS_LIKE
}


restCommentMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedComment.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedComment)))
.andExpect(status().isOk)

// Validate the Comment in the database
val commentList = commentRepository.findAll()
assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
val testComment = commentList.last()
    assertThat(testComment.comment).isEqualTo(UPDATED_COMMENT)
    assertThat(testComment.like).isEqualTo(UPDATED_LIKE)
    assertThat(testComment.disLike).isEqualTo(UPDATED_DIS_LIKE)
    }

    @Throws(Exception::class)
    fun patchNonExistingComment() {
        val databaseSizeBeforeUpdate = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll())
        comment.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommentMockMvc.perform(patch(ENTITY_API_URL_ID, comment.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(comment)))
            .andExpect(status().isBadRequest)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchComment() {
        val databaseSizeBeforeUpdate = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll())
        comment.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(comment)))
            .andExpect(status().isBadRequest)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamComment() {
        val databaseSizeBeforeUpdate = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll());
        comment.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(comment)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Comment in the database
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteComment() {
        // Initialize the database
        commentRepository.saveAndFlush(comment)
        commentRepository.save(comment)
        commentSearchRepository.save(comment)
        val databaseSizeBeforeDelete = commentRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(commentSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the comment
        restCommentMockMvc.perform(
            delete(ENTITY_API_URL_ID, comment.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val commentList = commentRepository.findAll()
        assertThat(commentList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(commentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchComment() {
        // Initialize the database
        comment = commentRepository.saveAndFlush(comment)
        commentSearchRepository.save(comment)
        // Search the comment
        restCommentMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${comment.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comment.id?.toInt())))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())))
            .andExpect(jsonPath("$.[*].like").value(hasItem(DEFAULT_LIKE)))
            .andExpect(jsonPath("$.[*].disLike").value(hasItem(DEFAULT_DIS_LIKE)))    }

    companion object {

        private const val DEFAULT_COMMENT = "AAAAAAAAAA"
        private const val UPDATED_COMMENT = "BBBBBBBBBB"

        private const val DEFAULT_LIKE: Int = 1
        private const val UPDATED_LIKE: Int = 2

        private const val DEFAULT_DIS_LIKE: Int = 1
        private const val UPDATED_DIS_LIKE: Int = 2


        private val ENTITY_API_URL: String = "/api/comments"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/comments"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Comment {
            val comment = Comment(
                comment = DEFAULT_COMMENT,

                like = DEFAULT_LIKE,

                disLike = DEFAULT_DIS_LIKE

            )


            return comment
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Comment {
            val comment = Comment(
                comment = UPDATED_COMMENT,

                like = UPDATED_LIKE,

                disLike = UPDATED_DIS_LIKE

            )


            return comment
        }

    }
}
