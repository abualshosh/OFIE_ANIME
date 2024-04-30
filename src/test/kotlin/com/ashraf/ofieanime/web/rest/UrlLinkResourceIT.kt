package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.UrlLink
import com.ashraf.ofieanime.repository.UrlLinkRepository
import com.ashraf.ofieanime.repository.search.UrlLinkSearchRepository
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

import com.ashraf.ofieanime.domain.enumeration.UrlLinkType

/**
 * Integration tests for the [UrlLinkResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class UrlLinkResourceIT {
    @Autowired
    private lateinit var urlLinkRepository: UrlLinkRepository
    @Autowired
    private lateinit var urlLinkSearchRepository: UrlLinkSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restUrlLinkMockMvc: MockMvc

    private lateinit var urlLink: UrlLink


    @AfterEach
    fun cleanupElasticSearchRepository() {
        urlLinkSearchRepository.deleteAll()
        assertThat(urlLinkSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        urlLink = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createUrlLink() {
        val databaseSizeBeforeCreate = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        // Create the UrlLink
        restUrlLinkMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(urlLink))
        ).andExpect(status().isCreated)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testUrlLink = urlLinkList[urlLinkList.size - 1]

        assertThat(testUrlLink.linkType).isEqualTo(DEFAULT_LINK_TYPE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createUrlLinkWithExistingId() {
        // Create the UrlLink with an existing ID
        urlLink.id = 1L

        val databaseSizeBeforeCreate = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restUrlLinkMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(urlLink))
        ).andExpect(status().isBadRequest)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllUrlLinks() {
        // Initialize the database
        urlLinkRepository.saveAndFlush(urlLink)

        // Get all the urlLinkList
        restUrlLinkMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(urlLink.id?.toInt())))
            .andExpect(jsonPath("$.[*].linkType").value(hasItem(DEFAULT_LINK_TYPE.toString())))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getUrlLink() {
        // Initialize the database
        urlLinkRepository.saveAndFlush(urlLink)

        val id = urlLink.id
        assertNotNull(id)

        // Get the urlLink
        restUrlLinkMockMvc.perform(get(ENTITY_API_URL_ID, urlLink.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(urlLink.id?.toInt()))
            .andExpect(jsonPath("$.linkType").value(DEFAULT_LINK_TYPE.toString()))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingUrlLink() {
        // Get the urlLink
        restUrlLinkMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingUrlLink() {
        // Initialize the database
        urlLinkRepository.saveAndFlush(urlLink)

        val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size

        urlLinkSearchRepository.save(urlLink)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        // Update the urlLink
        val updatedUrlLink = urlLinkRepository.findById(urlLink.id).get()
        // Disconnect from session so that the updates on updatedUrlLink are not directly saved in db
        em.detach(updatedUrlLink)
        updatedUrlLink.linkType = UPDATED_LINK_TYPE

        restUrlLinkMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedUrlLink.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedUrlLink))
        ).andExpect(status().isOk)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
        val testUrlLink = urlLinkList[urlLinkList.size - 1]
        assertThat(testUrlLink.linkType).isEqualTo(UPDATED_LINK_TYPE)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val urlLinkSearchList = IterableUtils.toList(urlLinkSearchRepository.findAll())
            val testUrlLinkSearch = urlLinkSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testUrlLinkSearch.linkType).isEqualTo(UPDATED_LINK_TYPE)
      }
    }

    @Test
    @Transactional
    fun putNonExistingUrlLink() {
        val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        urlLink.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUrlLinkMockMvc.perform(put(ENTITY_API_URL_ID, urlLink.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(urlLink)))
            .andExpect(status().isBadRequest)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchUrlLink() {
        val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        urlLink.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUrlLinkMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(urlLink))
        ).andExpect(status().isBadRequest)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamUrlLink() {
        val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll());
        urlLink.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUrlLinkMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(urlLink)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateUrlLinkWithPatch() {
        urlLinkRepository.saveAndFlush(urlLink)
        
        
val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size

// Update the urlLink using partial update
val partialUpdatedUrlLink = UrlLink().apply {
    id = urlLink.id

}


restUrlLinkMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedUrlLink.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedUrlLink)))
.andExpect(status().isOk)

// Validate the UrlLink in the database
val urlLinkList = urlLinkRepository.findAll()
assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
val testUrlLink = urlLinkList.last()
    assertThat(testUrlLink.linkType).isEqualTo(DEFAULT_LINK_TYPE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateUrlLinkWithPatch() {
        urlLinkRepository.saveAndFlush(urlLink)
        
        
val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size

// Update the urlLink using partial update
val partialUpdatedUrlLink = UrlLink().apply {
    id = urlLink.id

    
        linkType = UPDATED_LINK_TYPE
}


restUrlLinkMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedUrlLink.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedUrlLink)))
.andExpect(status().isOk)

// Validate the UrlLink in the database
val urlLinkList = urlLinkRepository.findAll()
assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
val testUrlLink = urlLinkList.last()
    assertThat(testUrlLink.linkType).isEqualTo(UPDATED_LINK_TYPE)
    }

    @Throws(Exception::class)
    fun patchNonExistingUrlLink() {
        val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        urlLink.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUrlLinkMockMvc.perform(patch(ENTITY_API_URL_ID, urlLink.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(urlLink)))
            .andExpect(status().isBadRequest)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchUrlLink() {
        val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        urlLink.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUrlLinkMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(urlLink)))
            .andExpect(status().isBadRequest)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamUrlLink() {
        val databaseSizeBeforeUpdate = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll());
        urlLink.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUrlLinkMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(urlLink)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the UrlLink in the database
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteUrlLink() {
        // Initialize the database
        urlLinkRepository.saveAndFlush(urlLink)
        urlLinkRepository.save(urlLink)
        urlLinkSearchRepository.save(urlLink)
        val databaseSizeBeforeDelete = urlLinkRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(urlLinkSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the urlLink
        restUrlLinkMockMvc.perform(
            delete(ENTITY_API_URL_ID, urlLink.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val urlLinkList = urlLinkRepository.findAll()
        assertThat(urlLinkList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(urlLinkSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchUrlLink() {
        // Initialize the database
        urlLink = urlLinkRepository.saveAndFlush(urlLink)
        urlLinkSearchRepository.save(urlLink)
        // Search the urlLink
        restUrlLinkMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${urlLink.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(urlLink.id?.toInt())))
            .andExpect(jsonPath("$.[*].linkType").value(hasItem(DEFAULT_LINK_TYPE.toString())))    }

    companion object {

        private val DEFAULT_LINK_TYPE: UrlLinkType = UrlLinkType.HD_1080
        private val UPDATED_LINK_TYPE: UrlLinkType = UrlLinkType.SD_720


        private val ENTITY_API_URL: String = "/api/url-links"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/url-links"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): UrlLink {
            val urlLink = UrlLink(
                linkType = DEFAULT_LINK_TYPE

            )


            return urlLink
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): UrlLink {
            val urlLink = UrlLink(
                linkType = UPDATED_LINK_TYPE

            )


            return urlLink
        }

    }
}
