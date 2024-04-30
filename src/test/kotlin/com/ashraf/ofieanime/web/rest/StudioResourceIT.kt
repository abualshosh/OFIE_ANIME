package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Studio
import com.ashraf.ofieanime.repository.StudioRepository
import com.ashraf.ofieanime.repository.search.StudioSearchRepository
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
 * Integration tests for the [StudioResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class StudioResourceIT {
    @Autowired
    private lateinit var studioRepository: StudioRepository
    @Autowired
    private lateinit var studioSearchRepository: StudioSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restStudioMockMvc: MockMvc

    private lateinit var studio: Studio


    @AfterEach
    fun cleanupElasticSearchRepository() {
        studioSearchRepository.deleteAll()
        assertThat(studioSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        studio = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createStudio() {
        val databaseSizeBeforeCreate = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll())
        // Create the Studio
        restStudioMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(studio))
        ).andExpect(status().isCreated)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testStudio = studioList[studioList.size - 1]

        assertThat(testStudio.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createStudioWithExistingId() {
        // Create the Studio with an existing ID
        studio.id = 1L

        val databaseSizeBeforeCreate = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restStudioMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(studio))
        ).andExpect(status().isBadRequest)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllStudios() {
        // Initialize the database
        studioRepository.saveAndFlush(studio)

        // Get all the studioList
        restStudioMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(studio.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getStudio() {
        // Initialize the database
        studioRepository.saveAndFlush(studio)

        val id = studio.id
        assertNotNull(id)

        // Get the studio
        restStudioMockMvc.perform(get(ENTITY_API_URL_ID, studio.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(studio.id?.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingStudio() {
        // Get the studio
        restStudioMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingStudio() {
        // Initialize the database
        studioRepository.saveAndFlush(studio)

        val databaseSizeBeforeUpdate = studioRepository.findAll().size

        studioSearchRepository.save(studio)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll())
        // Update the studio
        val updatedStudio = studioRepository.findById(studio.id).get()
        // Disconnect from session so that the updates on updatedStudio are not directly saved in db
        em.detach(updatedStudio)
        updatedStudio.name = UPDATED_NAME

        restStudioMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedStudio.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedStudio))
        ).andExpect(status().isOk)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
        val testStudio = studioList[studioList.size - 1]
        assertThat(testStudio.name).isEqualTo(UPDATED_NAME)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val studioSearchList = IterableUtils.toList(studioSearchRepository.findAll())
            val testStudioSearch = studioSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testStudioSearch.name).isEqualTo(UPDATED_NAME)
      }
    }

    @Test
    @Transactional
    fun putNonExistingStudio() {
        val databaseSizeBeforeUpdate = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll())
        studio.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStudioMockMvc.perform(put(ENTITY_API_URL_ID, studio.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(studio)))
            .andExpect(status().isBadRequest)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchStudio() {
        val databaseSizeBeforeUpdate = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll())
        studio.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStudioMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(studio))
        ).andExpect(status().isBadRequest)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamStudio() {
        val databaseSizeBeforeUpdate = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll());
        studio.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStudioMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(studio)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateStudioWithPatch() {
        studioRepository.saveAndFlush(studio)
        
        
val databaseSizeBeforeUpdate = studioRepository.findAll().size

// Update the studio using partial update
val partialUpdatedStudio = Studio().apply {
    id = studio.id

}


restStudioMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedStudio.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedStudio)))
.andExpect(status().isOk)

// Validate the Studio in the database
val studioList = studioRepository.findAll()
assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
val testStudio = studioList.last()
    assertThat(testStudio.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateStudioWithPatch() {
        studioRepository.saveAndFlush(studio)
        
        
val databaseSizeBeforeUpdate = studioRepository.findAll().size

// Update the studio using partial update
val partialUpdatedStudio = Studio().apply {
    id = studio.id

    
        name = UPDATED_NAME
}


restStudioMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedStudio.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedStudio)))
.andExpect(status().isOk)

// Validate the Studio in the database
val studioList = studioRepository.findAll()
assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
val testStudio = studioList.last()
    assertThat(testStudio.name).isEqualTo(UPDATED_NAME)
    }

    @Throws(Exception::class)
    fun patchNonExistingStudio() {
        val databaseSizeBeforeUpdate = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll())
        studio.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStudioMockMvc.perform(patch(ENTITY_API_URL_ID, studio.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(studio)))
            .andExpect(status().isBadRequest)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchStudio() {
        val databaseSizeBeforeUpdate = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll())
        studio.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStudioMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(studio)))
            .andExpect(status().isBadRequest)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamStudio() {
        val databaseSizeBeforeUpdate = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll());
        studio.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStudioMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(studio)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Studio in the database
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteStudio() {
        // Initialize the database
        studioRepository.saveAndFlush(studio)
        studioRepository.save(studio)
        studioSearchRepository.save(studio)
        val databaseSizeBeforeDelete = studioRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(studioSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the studio
        restStudioMockMvc.perform(
            delete(ENTITY_API_URL_ID, studio.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val studioList = studioRepository.findAll()
        assertThat(studioList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(studioSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchStudio() {
        // Initialize the database
        studio = studioRepository.saveAndFlush(studio)
        studioSearchRepository.save(studio)
        // Search the studio
        restStudioMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${studio.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(studio.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"


        private val ENTITY_API_URL: String = "/api/studios"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/studios"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Studio {
            val studio = Studio(
                name = DEFAULT_NAME

            )


            return studio
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Studio {
            val studio = Studio(
                name = UPDATED_NAME

            )


            return studio
        }

    }
}
