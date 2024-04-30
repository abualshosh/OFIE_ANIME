package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Favirote
import com.ashraf.ofieanime.repository.FaviroteRepository
import com.ashraf.ofieanime.repository.search.FaviroteSearchRepository
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
 * Integration tests for the [FaviroteResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class FaviroteResourceIT {
    @Autowired
    private lateinit var faviroteRepository: FaviroteRepository
    @Autowired
    private lateinit var faviroteSearchRepository: FaviroteSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restFaviroteMockMvc: MockMvc

    private lateinit var favirote: Favirote


    @AfterEach
    fun cleanupElasticSearchRepository() {
        faviroteSearchRepository.deleteAll()
        assertThat(faviroteSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        favirote = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createFavirote() {
        val databaseSizeBeforeCreate = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        // Create the Favirote
        restFaviroteMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(favirote))
        ).andExpect(status().isCreated)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testFavirote = faviroteList[faviroteList.size - 1]

        assertThat(testFavirote.addDate).isEqualTo(DEFAULT_ADD_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createFaviroteWithExistingId() {
        // Create the Favirote with an existing ID
        favirote.id = 1L

        val databaseSizeBeforeCreate = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restFaviroteMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(favirote))
        ).andExpect(status().isBadRequest)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllFavirotes() {
        // Initialize the database
        faviroteRepository.saveAndFlush(favirote)

        // Get all the faviroteList
        restFaviroteMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(favirote.id?.toInt())))
            .andExpect(jsonPath("$.[*].addDate").value(hasItem(DEFAULT_ADD_DATE.toString())))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getFavirote() {
        // Initialize the database
        faviroteRepository.saveAndFlush(favirote)

        val id = favirote.id
        assertNotNull(id)

        // Get the favirote
        restFaviroteMockMvc.perform(get(ENTITY_API_URL_ID, favirote.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(favirote.id?.toInt()))
            .andExpect(jsonPath("$.addDate").value(DEFAULT_ADD_DATE.toString()))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingFavirote() {
        // Get the favirote
        restFaviroteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingFavirote() {
        // Initialize the database
        faviroteRepository.saveAndFlush(favirote)

        val databaseSizeBeforeUpdate = faviroteRepository.findAll().size

        faviroteSearchRepository.save(favirote)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        // Update the favirote
        val updatedFavirote = faviroteRepository.findById(favirote.id).get()
        // Disconnect from session so that the updates on updatedFavirote are not directly saved in db
        em.detach(updatedFavirote)
        updatedFavirote.addDate = UPDATED_ADD_DATE

        restFaviroteMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedFavirote.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedFavirote))
        ).andExpect(status().isOk)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
        val testFavirote = faviroteList[faviroteList.size - 1]
        assertThat(testFavirote.addDate).isEqualTo(UPDATED_ADD_DATE)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val faviroteSearchList = IterableUtils.toList(faviroteSearchRepository.findAll())
            val testFaviroteSearch = faviroteSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testFaviroteSearch.addDate).isEqualTo(UPDATED_ADD_DATE)
      }
    }

    @Test
    @Transactional
    fun putNonExistingFavirote() {
        val databaseSizeBeforeUpdate = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        favirote.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFaviroteMockMvc.perform(put(ENTITY_API_URL_ID, favirote.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(favirote)))
            .andExpect(status().isBadRequest)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchFavirote() {
        val databaseSizeBeforeUpdate = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        favirote.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFaviroteMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(favirote))
        ).andExpect(status().isBadRequest)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamFavirote() {
        val databaseSizeBeforeUpdate = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll());
        favirote.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFaviroteMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(favirote)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateFaviroteWithPatch() {
        faviroteRepository.saveAndFlush(favirote)
        
        
val databaseSizeBeforeUpdate = faviroteRepository.findAll().size

// Update the favirote using partial update
val partialUpdatedFavirote = Favirote().apply {
    id = favirote.id

}


restFaviroteMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedFavirote.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedFavirote)))
.andExpect(status().isOk)

// Validate the Favirote in the database
val faviroteList = faviroteRepository.findAll()
assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
val testFavirote = faviroteList.last()
    assertThat(testFavirote.addDate).isEqualTo(DEFAULT_ADD_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateFaviroteWithPatch() {
        faviroteRepository.saveAndFlush(favirote)
        
        
val databaseSizeBeforeUpdate = faviroteRepository.findAll().size

// Update the favirote using partial update
val partialUpdatedFavirote = Favirote().apply {
    id = favirote.id

    
        addDate = UPDATED_ADD_DATE
}


restFaviroteMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedFavirote.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedFavirote)))
.andExpect(status().isOk)

// Validate the Favirote in the database
val faviroteList = faviroteRepository.findAll()
assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
val testFavirote = faviroteList.last()
    assertThat(testFavirote.addDate).isEqualTo(UPDATED_ADD_DATE)
    }

    @Throws(Exception::class)
    fun patchNonExistingFavirote() {
        val databaseSizeBeforeUpdate = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        favirote.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFaviroteMockMvc.perform(patch(ENTITY_API_URL_ID, favirote.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(favirote)))
            .andExpect(status().isBadRequest)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchFavirote() {
        val databaseSizeBeforeUpdate = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        favirote.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFaviroteMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(favirote)))
            .andExpect(status().isBadRequest)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamFavirote() {
        val databaseSizeBeforeUpdate = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll());
        favirote.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFaviroteMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(favirote)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Favirote in the database
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteFavirote() {
        // Initialize the database
        faviroteRepository.saveAndFlush(favirote)
        faviroteRepository.save(favirote)
        faviroteSearchRepository.save(favirote)
        val databaseSizeBeforeDelete = faviroteRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(faviroteSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the favirote
        restFaviroteMockMvc.perform(
            delete(ENTITY_API_URL_ID, favirote.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val faviroteList = faviroteRepository.findAll()
        assertThat(faviroteList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(faviroteSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchFavirote() {
        // Initialize the database
        favirote = faviroteRepository.saveAndFlush(favirote)
        faviroteSearchRepository.save(favirote)
        // Search the favirote
        restFaviroteMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${favirote.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(favirote.id?.toInt())))
            .andExpect(jsonPath("$.[*].addDate").value(hasItem(DEFAULT_ADD_DATE.toString())))    }

    companion object {

        private val DEFAULT_ADD_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_ADD_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())


        private val ENTITY_API_URL: String = "/api/favirotes"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/favirotes"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Favirote {
            val favirote = Favirote(
                addDate = DEFAULT_ADD_DATE

            )


            return favirote
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Favirote {
            val favirote = Favirote(
                addDate = UPDATED_ADD_DATE

            )


            return favirote
        }

    }
}
