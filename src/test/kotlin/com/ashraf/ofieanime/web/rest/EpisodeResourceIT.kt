package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Episode
import com.ashraf.ofieanime.repository.EpisodeRepository
import com.ashraf.ofieanime.repository.search.EpisodeSearchRepository
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
 * Integration tests for the [EpisodeResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EpisodeResourceIT {
    @Autowired
    private lateinit var episodeRepository: EpisodeRepository
    @Autowired
    private lateinit var episodeSearchRepository: EpisodeSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restEpisodeMockMvc: MockMvc

    private lateinit var episode: Episode


    @AfterEach
    fun cleanupElasticSearchRepository() {
        episodeSearchRepository.deleteAll()
        assertThat(episodeSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        episode = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createEpisode() {
        val databaseSizeBeforeCreate = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        // Create the Episode
        restEpisodeMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(episode))
        ).andExpect(status().isCreated)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testEpisode = episodeList[episodeList.size - 1]

        assertThat(testEpisode.title).isEqualTo(DEFAULT_TITLE)
        assertThat(testEpisode.episodeLink).isEqualTo(DEFAULT_EPISODE_LINK)
        assertThat(testEpisode.relaseDate).isEqualTo(DEFAULT_RELASE_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createEpisodeWithExistingId() {
        // Create the Episode with an existing ID
        episode.id = 1L

        val databaseSizeBeforeCreate = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restEpisodeMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(episode))
        ).andExpect(status().isBadRequest)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllEpisodes() {
        // Initialize the database
        episodeRepository.saveAndFlush(episode)

        // Get all the episodeList
        restEpisodeMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(episode.id?.toInt())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].episodeLink").value(hasItem(DEFAULT_EPISODE_LINK.toString())))
            .andExpect(jsonPath("$.[*].relaseDate").value(hasItem(DEFAULT_RELASE_DATE.toString())))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getEpisode() {
        // Initialize the database
        episodeRepository.saveAndFlush(episode)

        val id = episode.id
        assertNotNull(id)

        // Get the episode
        restEpisodeMockMvc.perform(get(ENTITY_API_URL_ID, episode.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(episode.id?.toInt()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.episodeLink").value(DEFAULT_EPISODE_LINK.toString()))
            .andExpect(jsonPath("$.relaseDate").value(DEFAULT_RELASE_DATE.toString()))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingEpisode() {
        // Get the episode
        restEpisodeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingEpisode() {
        // Initialize the database
        episodeRepository.saveAndFlush(episode)

        val databaseSizeBeforeUpdate = episodeRepository.findAll().size

        episodeSearchRepository.save(episode)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        // Update the episode
        val updatedEpisode = episodeRepository.findById(episode.id).get()
        // Disconnect from session so that the updates on updatedEpisode are not directly saved in db
        em.detach(updatedEpisode)
        updatedEpisode.title = UPDATED_TITLE
        updatedEpisode.episodeLink = UPDATED_EPISODE_LINK
        updatedEpisode.relaseDate = UPDATED_RELASE_DATE

        restEpisodeMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedEpisode.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedEpisode))
        ).andExpect(status().isOk)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
        val testEpisode = episodeList[episodeList.size - 1]
        assertThat(testEpisode.title).isEqualTo(UPDATED_TITLE)
        assertThat(testEpisode.episodeLink).isEqualTo(UPDATED_EPISODE_LINK)
        assertThat(testEpisode.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val episodeSearchList = IterableUtils.toList(episodeSearchRepository.findAll())
            val testEpisodeSearch = episodeSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testEpisodeSearch.title).isEqualTo(UPDATED_TITLE)
            assertThat(testEpisodeSearch.episodeLink).isEqualTo(UPDATED_EPISODE_LINK)
            assertThat(testEpisodeSearch.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
      }
    }

    @Test
    @Transactional
    fun putNonExistingEpisode() {
        val databaseSizeBeforeUpdate = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        episode.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEpisodeMockMvc.perform(put(ENTITY_API_URL_ID, episode.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(episode)))
            .andExpect(status().isBadRequest)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchEpisode() {
        val databaseSizeBeforeUpdate = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        episode.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEpisodeMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(episode))
        ).andExpect(status().isBadRequest)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamEpisode() {
        val databaseSizeBeforeUpdate = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll());
        episode.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEpisodeMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(episode)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateEpisodeWithPatch() {
        episodeRepository.saveAndFlush(episode)
        
        
val databaseSizeBeforeUpdate = episodeRepository.findAll().size

// Update the episode using partial update
val partialUpdatedEpisode = Episode().apply {
    id = episode.id

    
        episodeLink = UPDATED_EPISODE_LINK
        relaseDate = UPDATED_RELASE_DATE
}


restEpisodeMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedEpisode.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedEpisode)))
.andExpect(status().isOk)

// Validate the Episode in the database
val episodeList = episodeRepository.findAll()
assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
val testEpisode = episodeList.last()
    assertThat(testEpisode.title).isEqualTo(DEFAULT_TITLE)
    assertThat(testEpisode.episodeLink).isEqualTo(UPDATED_EPISODE_LINK)
    assertThat(testEpisode.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateEpisodeWithPatch() {
        episodeRepository.saveAndFlush(episode)
        
        
val databaseSizeBeforeUpdate = episodeRepository.findAll().size

// Update the episode using partial update
val partialUpdatedEpisode = Episode().apply {
    id = episode.id

    
        title = UPDATED_TITLE
        episodeLink = UPDATED_EPISODE_LINK
        relaseDate = UPDATED_RELASE_DATE
}


restEpisodeMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedEpisode.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedEpisode)))
.andExpect(status().isOk)

// Validate the Episode in the database
val episodeList = episodeRepository.findAll()
assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
val testEpisode = episodeList.last()
    assertThat(testEpisode.title).isEqualTo(UPDATED_TITLE)
    assertThat(testEpisode.episodeLink).isEqualTo(UPDATED_EPISODE_LINK)
    assertThat(testEpisode.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
    }

    @Throws(Exception::class)
    fun patchNonExistingEpisode() {
        val databaseSizeBeforeUpdate = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        episode.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEpisodeMockMvc.perform(patch(ENTITY_API_URL_ID, episode.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(episode)))
            .andExpect(status().isBadRequest)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchEpisode() {
        val databaseSizeBeforeUpdate = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        episode.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEpisodeMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(episode)))
            .andExpect(status().isBadRequest)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamEpisode() {
        val databaseSizeBeforeUpdate = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll());
        episode.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEpisodeMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(episode)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Episode in the database
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteEpisode() {
        // Initialize the database
        episodeRepository.saveAndFlush(episode)
        episodeRepository.save(episode)
        episodeSearchRepository.save(episode)
        val databaseSizeBeforeDelete = episodeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(episodeSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the episode
        restEpisodeMockMvc.perform(
            delete(ENTITY_API_URL_ID, episode.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val episodeList = episodeRepository.findAll()
        assertThat(episodeList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(episodeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchEpisode() {
        // Initialize the database
        episode = episodeRepository.saveAndFlush(episode)
        episodeSearchRepository.save(episode)
        // Search the episode
        restEpisodeMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${episode.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(episode.id?.toInt())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].episodeLink").value(hasItem(DEFAULT_EPISODE_LINK.toString())))
            .andExpect(jsonPath("$.[*].relaseDate").value(hasItem(DEFAULT_RELASE_DATE.toString())))    }

    companion object {

        private const val DEFAULT_TITLE = "AAAAAAAAAA"
        private const val UPDATED_TITLE = "BBBBBBBBBB"

        private const val DEFAULT_EPISODE_LINK = "AAAAAAAAAA"
        private const val UPDATED_EPISODE_LINK = "BBBBBBBBBB"

        private val DEFAULT_RELASE_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_RELASE_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())


        private val ENTITY_API_URL: String = "/api/episodes"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/episodes"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Episode {
            val episode = Episode(
                title = DEFAULT_TITLE,

                episodeLink = DEFAULT_EPISODE_LINK,

                relaseDate = DEFAULT_RELASE_DATE

            )


            return episode
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Episode {
            val episode = Episode(
                title = UPDATED_TITLE,

                episodeLink = UPDATED_EPISODE_LINK,

                relaseDate = UPDATED_RELASE_DATE

            )


            return episode
        }

    }
}
