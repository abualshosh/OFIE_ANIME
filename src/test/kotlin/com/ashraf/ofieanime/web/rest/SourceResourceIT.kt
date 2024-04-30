package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Source
import com.ashraf.ofieanime.repository.SourceRepository
import com.ashraf.ofieanime.repository.search.SourceSearchRepository
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
 * Integration tests for the [SourceResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SourceResourceIT {
    @Autowired
    private lateinit var sourceRepository: SourceRepository
    @Autowired
    private lateinit var sourceSearchRepository: SourceSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restSourceMockMvc: MockMvc

    private lateinit var source: Source


    @AfterEach
    fun cleanupElasticSearchRepository() {
        sourceSearchRepository.deleteAll()
        assertThat(sourceSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        source = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSource() {
        val databaseSizeBeforeCreate = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        // Create the Source
        restSourceMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(source))
        ).andExpect(status().isCreated)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testSource = sourceList[sourceList.size - 1]

        assertThat(testSource.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSourceWithExistingId() {
        // Create the Source with an existing ID
        source.id = 1L

        val databaseSizeBeforeCreate = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restSourceMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(source))
        ).andExpect(status().isBadRequest)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllSources() {
        // Initialize the database
        sourceRepository.saveAndFlush(source)

        // Get all the sourceList
        restSourceMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(source.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getSource() {
        // Initialize the database
        sourceRepository.saveAndFlush(source)

        val id = source.id
        assertNotNull(id)

        // Get the source
        restSourceMockMvc.perform(get(ENTITY_API_URL_ID, source.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(source.id?.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingSource() {
        // Get the source
        restSourceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingSource() {
        // Initialize the database
        sourceRepository.saveAndFlush(source)

        val databaseSizeBeforeUpdate = sourceRepository.findAll().size

        sourceSearchRepository.save(source)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        // Update the source
        val updatedSource = sourceRepository.findById(source.id).get()
        // Disconnect from session so that the updates on updatedSource are not directly saved in db
        em.detach(updatedSource)
        updatedSource.name = UPDATED_NAME

        restSourceMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedSource.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedSource))
        ).andExpect(status().isOk)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
        val testSource = sourceList[sourceList.size - 1]
        assertThat(testSource.name).isEqualTo(UPDATED_NAME)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val sourceSearchList = IterableUtils.toList(sourceSearchRepository.findAll())
            val testSourceSearch = sourceSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testSourceSearch.name).isEqualTo(UPDATED_NAME)
      }
    }

    @Test
    @Transactional
    fun putNonExistingSource() {
        val databaseSizeBeforeUpdate = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        source.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSourceMockMvc.perform(put(ENTITY_API_URL_ID, source.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(source)))
            .andExpect(status().isBadRequest)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchSource() {
        val databaseSizeBeforeUpdate = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        source.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSourceMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(source))
        ).andExpect(status().isBadRequest)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamSource() {
        val databaseSizeBeforeUpdate = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll());
        source.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSourceMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(source)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateSourceWithPatch() {
        sourceRepository.saveAndFlush(source)
        
        
val databaseSizeBeforeUpdate = sourceRepository.findAll().size

// Update the source using partial update
val partialUpdatedSource = Source().apply {
    id = source.id

    
        name = UPDATED_NAME
}


restSourceMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedSource.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedSource)))
.andExpect(status().isOk)

// Validate the Source in the database
val sourceList = sourceRepository.findAll()
assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
val testSource = sourceList.last()
    assertThat(testSource.name).isEqualTo(UPDATED_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateSourceWithPatch() {
        sourceRepository.saveAndFlush(source)
        
        
val databaseSizeBeforeUpdate = sourceRepository.findAll().size

// Update the source using partial update
val partialUpdatedSource = Source().apply {
    id = source.id

    
        name = UPDATED_NAME
}


restSourceMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedSource.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedSource)))
.andExpect(status().isOk)

// Validate the Source in the database
val sourceList = sourceRepository.findAll()
assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
val testSource = sourceList.last()
    assertThat(testSource.name).isEqualTo(UPDATED_NAME)
    }

    @Throws(Exception::class)
    fun patchNonExistingSource() {
        val databaseSizeBeforeUpdate = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        source.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSourceMockMvc.perform(patch(ENTITY_API_URL_ID, source.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(source)))
            .andExpect(status().isBadRequest)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchSource() {
        val databaseSizeBeforeUpdate = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        source.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSourceMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(source)))
            .andExpect(status().isBadRequest)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamSource() {
        val databaseSizeBeforeUpdate = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll());
        source.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSourceMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(source)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Source in the database
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteSource() {
        // Initialize the database
        sourceRepository.saveAndFlush(source)
        sourceRepository.save(source)
        sourceSearchRepository.save(source)
        val databaseSizeBeforeDelete = sourceRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(sourceSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the source
        restSourceMockMvc.perform(
            delete(ENTITY_API_URL_ID, source.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val sourceList = sourceRepository.findAll()
        assertThat(sourceList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(sourceSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchSource() {
        // Initialize the database
        source = sourceRepository.saveAndFlush(source)
        sourceSearchRepository.save(source)
        // Search the source
        restSourceMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${source.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(source.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"


        private val ENTITY_API_URL: String = "/api/sources"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/sources"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Source {
            val source = Source(
                name = DEFAULT_NAME

            )


            return source
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Source {
            val source = Source(
                name = UPDATED_NAME

            )


            return source
        }

    }
}
