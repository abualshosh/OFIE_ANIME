package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Tag
import com.ashraf.ofieanime.repository.TagRepository
import com.ashraf.ofieanime.repository.search.TagSearchRepository
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
 * Integration tests for the [TagResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TagResourceIT {
    @Autowired
    private lateinit var tagRepository: TagRepository
    @Autowired
    private lateinit var tagSearchRepository: TagSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restTagMockMvc: MockMvc

    private lateinit var tag: Tag


    @AfterEach
    fun cleanupElasticSearchRepository() {
        tagSearchRepository.deleteAll()
        assertThat(tagSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        tag = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createTag() {
        val databaseSizeBeforeCreate = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll())
        // Create the Tag
        restTagMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(tag))
        ).andExpect(status().isCreated)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testTag = tagList[tagList.size - 1]

        assertThat(testTag.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createTagWithExistingId() {
        // Create the Tag with an existing ID
        tag.id = 1L

        val databaseSizeBeforeCreate = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restTagMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(tag))
        ).andExpect(status().isBadRequest)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllTags() {
        // Initialize the database
        tagRepository.saveAndFlush(tag)

        // Get all the tagList
        restTagMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tag.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getTag() {
        // Initialize the database
        tagRepository.saveAndFlush(tag)

        val id = tag.id
        assertNotNull(id)

        // Get the tag
        restTagMockMvc.perform(get(ENTITY_API_URL_ID, tag.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tag.id?.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingTag() {
        // Get the tag
        restTagMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingTag() {
        // Initialize the database
        tagRepository.saveAndFlush(tag)

        val databaseSizeBeforeUpdate = tagRepository.findAll().size

        tagSearchRepository.save(tag)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll())
        // Update the tag
        val updatedTag = tagRepository.findById(tag.id).get()
        // Disconnect from session so that the updates on updatedTag are not directly saved in db
        em.detach(updatedTag)
        updatedTag.name = UPDATED_NAME

        restTagMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedTag.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedTag))
        ).andExpect(status().isOk)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
        val testTag = tagList[tagList.size - 1]
        assertThat(testTag.name).isEqualTo(UPDATED_NAME)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val tagSearchList = IterableUtils.toList(tagSearchRepository.findAll())
            val testTagSearch = tagSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testTagSearch.name).isEqualTo(UPDATED_NAME)
      }
    }

    @Test
    @Transactional
    fun putNonExistingTag() {
        val databaseSizeBeforeUpdate = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll())
        tag.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTagMockMvc.perform(put(ENTITY_API_URL_ID, tag.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(tag)))
            .andExpect(status().isBadRequest)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchTag() {
        val databaseSizeBeforeUpdate = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll())
        tag.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTagMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(tag))
        ).andExpect(status().isBadRequest)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamTag() {
        val databaseSizeBeforeUpdate = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll());
        tag.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTagMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(tag)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateTagWithPatch() {
        tagRepository.saveAndFlush(tag)
        
        
val databaseSizeBeforeUpdate = tagRepository.findAll().size

// Update the tag using partial update
val partialUpdatedTag = Tag().apply {
    id = tag.id

}


restTagMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedTag.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedTag)))
.andExpect(status().isOk)

// Validate the Tag in the database
val tagList = tagRepository.findAll()
assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
val testTag = tagList.last()
    assertThat(testTag.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateTagWithPatch() {
        tagRepository.saveAndFlush(tag)
        
        
val databaseSizeBeforeUpdate = tagRepository.findAll().size

// Update the tag using partial update
val partialUpdatedTag = Tag().apply {
    id = tag.id

    
        name = UPDATED_NAME
}


restTagMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedTag.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedTag)))
.andExpect(status().isOk)

// Validate the Tag in the database
val tagList = tagRepository.findAll()
assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
val testTag = tagList.last()
    assertThat(testTag.name).isEqualTo(UPDATED_NAME)
    }

    @Throws(Exception::class)
    fun patchNonExistingTag() {
        val databaseSizeBeforeUpdate = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll())
        tag.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTagMockMvc.perform(patch(ENTITY_API_URL_ID, tag.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(tag)))
            .andExpect(status().isBadRequest)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchTag() {
        val databaseSizeBeforeUpdate = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll())
        tag.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTagMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(tag)))
            .andExpect(status().isBadRequest)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamTag() {
        val databaseSizeBeforeUpdate = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll());
        tag.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTagMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(tag)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteTag() {
        // Initialize the database
        tagRepository.saveAndFlush(tag)
        tagRepository.save(tag)
        tagSearchRepository.save(tag)
        val databaseSizeBeforeDelete = tagRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(tagSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the tag
        restTagMockMvc.perform(
            delete(ENTITY_API_URL_ID, tag.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(tagSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchTag() {
        // Initialize the database
        tag = tagRepository.saveAndFlush(tag)
        tagSearchRepository.save(tag)
        // Search the tag
        restTagMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${tag.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tag.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"


        private val ENTITY_API_URL: String = "/api/tags"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/tags"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Tag {
            val tag = Tag(
                name = DEFAULT_NAME

            )


            return tag
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Tag {
            val tag = Tag(
                name = UPDATED_NAME

            )


            return tag
        }

    }
}
