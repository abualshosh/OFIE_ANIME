package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.History
import com.ashraf.ofieanime.repository.HistoryRepository
import com.ashraf.ofieanime.repository.search.HistorySearchRepository
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
 * Integration tests for the [HistoryResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class HistoryResourceIT {
    @Autowired
    private lateinit var historyRepository: HistoryRepository
    @Autowired
    private lateinit var historySearchRepository: HistorySearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restHistoryMockMvc: MockMvc

    private lateinit var history: History


    @AfterEach
    fun cleanupElasticSearchRepository() {
        historySearchRepository.deleteAll()
        assertThat(historySearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        history = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createHistory() {
        val databaseSizeBeforeCreate = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll())
        // Create the History
        restHistoryMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(history))
        ).andExpect(status().isCreated)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testHistory = historyList[historyList.size - 1]

        assertThat(testHistory.date).isEqualTo(DEFAULT_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createHistoryWithExistingId() {
        // Create the History with an existing ID
        history.id = 1L

        val databaseSizeBeforeCreate = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restHistoryMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(history))
        ).andExpect(status().isBadRequest)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllHistories() {
        // Initialize the database
        historyRepository.saveAndFlush(history)

        // Get all the historyList
        restHistoryMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(history.id?.toInt())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getHistory() {
        // Initialize the database
        historyRepository.saveAndFlush(history)

        val id = history.id
        assertNotNull(id)

        // Get the history
        restHistoryMockMvc.perform(get(ENTITY_API_URL_ID, history.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(history.id?.toInt()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingHistory() {
        // Get the history
        restHistoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingHistory() {
        // Initialize the database
        historyRepository.saveAndFlush(history)

        val databaseSizeBeforeUpdate = historyRepository.findAll().size

        historySearchRepository.save(history)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll())
        // Update the history
        val updatedHistory = historyRepository.findById(history.id).get()
        // Disconnect from session so that the updates on updatedHistory are not directly saved in db
        em.detach(updatedHistory)
        updatedHistory.date = UPDATED_DATE

        restHistoryMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedHistory.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedHistory))
        ).andExpect(status().isOk)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
        val testHistory = historyList[historyList.size - 1]
        assertThat(testHistory.date).isEqualTo(UPDATED_DATE)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val historySearchList = IterableUtils.toList(historySearchRepository.findAll())
            val testHistorySearch = historySearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testHistorySearch.date).isEqualTo(UPDATED_DATE)
      }
    }

    @Test
    @Transactional
    fun putNonExistingHistory() {
        val databaseSizeBeforeUpdate = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll())
        history.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHistoryMockMvc.perform(put(ENTITY_API_URL_ID, history.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(history)))
            .andExpect(status().isBadRequest)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchHistory() {
        val databaseSizeBeforeUpdate = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll())
        history.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistoryMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(history))
        ).andExpect(status().isBadRequest)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamHistory() {
        val databaseSizeBeforeUpdate = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll());
        history.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistoryMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(history)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateHistoryWithPatch() {
        historyRepository.saveAndFlush(history)
        
        
val databaseSizeBeforeUpdate = historyRepository.findAll().size

// Update the history using partial update
val partialUpdatedHistory = History().apply {
    id = history.id

    
        date = UPDATED_DATE
}


restHistoryMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedHistory.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedHistory)))
.andExpect(status().isOk)

// Validate the History in the database
val historyList = historyRepository.findAll()
assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
val testHistory = historyList.last()
    assertThat(testHistory.date).isEqualTo(UPDATED_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateHistoryWithPatch() {
        historyRepository.saveAndFlush(history)
        
        
val databaseSizeBeforeUpdate = historyRepository.findAll().size

// Update the history using partial update
val partialUpdatedHistory = History().apply {
    id = history.id

    
        date = UPDATED_DATE
}


restHistoryMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedHistory.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedHistory)))
.andExpect(status().isOk)

// Validate the History in the database
val historyList = historyRepository.findAll()
assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
val testHistory = historyList.last()
    assertThat(testHistory.date).isEqualTo(UPDATED_DATE)
    }

    @Throws(Exception::class)
    fun patchNonExistingHistory() {
        val databaseSizeBeforeUpdate = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll())
        history.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHistoryMockMvc.perform(patch(ENTITY_API_URL_ID, history.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(history)))
            .andExpect(status().isBadRequest)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchHistory() {
        val databaseSizeBeforeUpdate = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll())
        history.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistoryMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(history)))
            .andExpect(status().isBadRequest)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamHistory() {
        val databaseSizeBeforeUpdate = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll());
        history.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHistoryMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(history)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the History in the database
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteHistory() {
        // Initialize the database
        historyRepository.saveAndFlush(history)
        historyRepository.save(history)
        historySearchRepository.save(history)
        val databaseSizeBeforeDelete = historyRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(historySearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the history
        restHistoryMockMvc.perform(
            delete(ENTITY_API_URL_ID, history.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val historyList = historyRepository.findAll()
        assertThat(historyList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(historySearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchHistory() {
        // Initialize the database
        history = historyRepository.saveAndFlush(history)
        historySearchRepository.save(history)
        // Search the history
        restHistoryMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${history.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(history.id?.toInt())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))    }

    companion object {

        private val DEFAULT_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())


        private val ENTITY_API_URL: String = "/api/histories"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/histories"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): History {
            val history = History(
                date = DEFAULT_DATE

            )


            return history
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): History {
            val history = History(
                date = UPDATED_DATE

            )


            return history
        }

    }
}
