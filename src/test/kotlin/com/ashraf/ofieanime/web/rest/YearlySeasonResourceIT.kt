package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.YearlySeason
import com.ashraf.ofieanime.repository.YearlySeasonRepository
import com.ashraf.ofieanime.repository.search.YearlySeasonSearchRepository
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
import java.time.LocalDate
import java.time.ZoneId
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
 * Integration tests for the [YearlySeasonResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class YearlySeasonResourceIT {
    @Autowired
    private lateinit var yearlySeasonRepository: YearlySeasonRepository
    @Autowired
    private lateinit var yearlySeasonSearchRepository: YearlySeasonSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restYearlySeasonMockMvc: MockMvc

    private lateinit var yearlySeason: YearlySeason


    @AfterEach
    fun cleanupElasticSearchRepository() {
        yearlySeasonSearchRepository.deleteAll()
        assertThat(yearlySeasonSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        yearlySeason = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createYearlySeason() {
        val databaseSizeBeforeCreate = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        // Create the YearlySeason
        restYearlySeasonMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(yearlySeason))
        ).andExpect(status().isCreated)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testYearlySeason = yearlySeasonList[yearlySeasonList.size - 1]

        assertThat(testYearlySeason.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createYearlySeasonWithExistingId() {
        // Create the YearlySeason with an existing ID
        yearlySeason.id = 1L

        val databaseSizeBeforeCreate = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restYearlySeasonMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(yearlySeason))
        ).andExpect(status().isBadRequest)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllYearlySeasons() {
        // Initialize the database
        yearlySeasonRepository.saveAndFlush(yearlySeason)

        // Get all the yearlySeasonList
        restYearlySeasonMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(yearlySeason.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getYearlySeason() {
        // Initialize the database
        yearlySeasonRepository.saveAndFlush(yearlySeason)

        val id = yearlySeason.id
        assertNotNull(id)

        // Get the yearlySeason
        restYearlySeasonMockMvc.perform(get(ENTITY_API_URL_ID, yearlySeason.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(yearlySeason.id?.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingYearlySeason() {
        // Get the yearlySeason
        restYearlySeasonMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingYearlySeason() {
        // Initialize the database
        yearlySeasonRepository.saveAndFlush(yearlySeason)

        val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size

        yearlySeasonSearchRepository.save(yearlySeason)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        // Update the yearlySeason
        val updatedYearlySeason = yearlySeasonRepository.findById(yearlySeason.id).get()
        // Disconnect from session so that the updates on updatedYearlySeason are not directly saved in db
        em.detach(updatedYearlySeason)
        updatedYearlySeason.name = UPDATED_NAME

        restYearlySeasonMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedYearlySeason.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedYearlySeason))
        ).andExpect(status().isOk)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
        val testYearlySeason = yearlySeasonList[yearlySeasonList.size - 1]
        assertThat(testYearlySeason.name).isEqualTo(UPDATED_NAME)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val yearlySeasonSearchList = IterableUtils.toList(yearlySeasonSearchRepository.findAll())
            val testYearlySeasonSearch = yearlySeasonSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testYearlySeasonSearch.name).isEqualTo(UPDATED_NAME)
      }
    }

    @Test
    @Transactional
    fun putNonExistingYearlySeason() {
        val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        yearlySeason.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restYearlySeasonMockMvc.perform(put(ENTITY_API_URL_ID, yearlySeason.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(yearlySeason)))
            .andExpect(status().isBadRequest)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchYearlySeason() {
        val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        yearlySeason.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restYearlySeasonMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(yearlySeason))
        ).andExpect(status().isBadRequest)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamYearlySeason() {
        val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll());
        yearlySeason.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restYearlySeasonMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(yearlySeason)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateYearlySeasonWithPatch() {
        yearlySeasonRepository.saveAndFlush(yearlySeason)
        
        
val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size

// Update the yearlySeason using partial update
val partialUpdatedYearlySeason = YearlySeason().apply {
    id = yearlySeason.id

    
        name = UPDATED_NAME
}


restYearlySeasonMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedYearlySeason.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedYearlySeason)))
.andExpect(status().isOk)

// Validate the YearlySeason in the database
val yearlySeasonList = yearlySeasonRepository.findAll()
assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
val testYearlySeason = yearlySeasonList.last()
    assertThat(testYearlySeason.name).isEqualTo(UPDATED_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateYearlySeasonWithPatch() {
        yearlySeasonRepository.saveAndFlush(yearlySeason)
        
        
val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size

// Update the yearlySeason using partial update
val partialUpdatedYearlySeason = YearlySeason().apply {
    id = yearlySeason.id

    
        name = UPDATED_NAME
}


restYearlySeasonMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedYearlySeason.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedYearlySeason)))
.andExpect(status().isOk)

// Validate the YearlySeason in the database
val yearlySeasonList = yearlySeasonRepository.findAll()
assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
val testYearlySeason = yearlySeasonList.last()
    assertThat(testYearlySeason.name).isEqualTo(UPDATED_NAME)
    }

    @Throws(Exception::class)
    fun patchNonExistingYearlySeason() {
        val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        yearlySeason.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restYearlySeasonMockMvc.perform(patch(ENTITY_API_URL_ID, yearlySeason.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(yearlySeason)))
            .andExpect(status().isBadRequest)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchYearlySeason() {
        val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        yearlySeason.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restYearlySeasonMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(yearlySeason)))
            .andExpect(status().isBadRequest)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamYearlySeason() {
        val databaseSizeBeforeUpdate = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll());
        yearlySeason.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restYearlySeasonMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(yearlySeason)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the YearlySeason in the database
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteYearlySeason() {
        // Initialize the database
        yearlySeasonRepository.saveAndFlush(yearlySeason)
        yearlySeasonRepository.save(yearlySeason)
        yearlySeasonSearchRepository.save(yearlySeason)
        val databaseSizeBeforeDelete = yearlySeasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the yearlySeason
        restYearlySeasonMockMvc.perform(
            delete(ENTITY_API_URL_ID, yearlySeason.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val yearlySeasonList = yearlySeasonRepository.findAll()
        assertThat(yearlySeasonList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(yearlySeasonSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchYearlySeason() {
        // Initialize the database
        yearlySeason = yearlySeasonRepository.saveAndFlush(yearlySeason)
        yearlySeasonSearchRepository.save(yearlySeason)
        // Search the yearlySeason
        restYearlySeasonMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${yearlySeason.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(yearlySeason.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))    }

    companion object {

        private val DEFAULT_NAME: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_NAME: LocalDate = LocalDate.now(ZoneId.systemDefault())


        private val ENTITY_API_URL: String = "/api/yearly-seasons"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/yearly-seasons"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): YearlySeason {
            val yearlySeason = YearlySeason(
                name = DEFAULT_NAME

            )


            return yearlySeason
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): YearlySeason {
            val yearlySeason = YearlySeason(
                name = UPDATED_NAME

            )


            return yearlySeason
        }

    }
}
