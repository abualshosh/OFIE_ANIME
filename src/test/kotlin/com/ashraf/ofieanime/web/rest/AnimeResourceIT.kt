package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Anime
import com.ashraf.ofieanime.repository.AnimeRepository
import com.ashraf.ofieanime.repository.search.AnimeSearchRepository
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
 * Integration tests for the [AnimeResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AnimeResourceIT {
    @Autowired
    private lateinit var animeRepository: AnimeRepository
    @Autowired
    private lateinit var animeSearchRepository: AnimeSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restAnimeMockMvc: MockMvc

    private lateinit var anime: Anime


    @AfterEach
    fun cleanupElasticSearchRepository() {
        animeSearchRepository.deleteAll()
        assertThat(animeSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        anime = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createAnime() {
        val databaseSizeBeforeCreate = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll())
        // Create the Anime
        restAnimeMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(anime))
        ).andExpect(status().isCreated)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testAnime = animeList[animeList.size - 1]

        assertThat(testAnime.title).isEqualTo(DEFAULT_TITLE)
        assertThat(testAnime.discription).isEqualTo(DEFAULT_DISCRIPTION)
        assertThat(testAnime.cover).isEqualTo(DEFAULT_COVER)
        assertThat(testAnime.relaseDate).isEqualTo(DEFAULT_RELASE_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createAnimeWithExistingId() {
        // Create the Anime with an existing ID
        anime.id = 1L

        val databaseSizeBeforeCreate = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restAnimeMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(anime))
        ).andExpect(status().isBadRequest)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAnime() {
        // Initialize the database
        animeRepository.saveAndFlush(anime)

        // Get all the animeList
        restAnimeMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(anime.id?.toInt())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].discription").value(hasItem(DEFAULT_DISCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].cover").value(hasItem(DEFAULT_COVER)))
            .andExpect(jsonPath("$.[*].relaseDate").value(hasItem(DEFAULT_RELASE_DATE.toString())))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAnime() {
        // Initialize the database
        animeRepository.saveAndFlush(anime)

        val id = anime.id
        assertNotNull(id)

        // Get the anime
        restAnimeMockMvc.perform(get(ENTITY_API_URL_ID, anime.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(anime.id?.toInt()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.discription").value(DEFAULT_DISCRIPTION.toString()))
            .andExpect(jsonPath("$.cover").value(DEFAULT_COVER))
            .andExpect(jsonPath("$.relaseDate").value(DEFAULT_RELASE_DATE.toString()))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingAnime() {
        // Get the anime
        restAnimeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingAnime() {
        // Initialize the database
        animeRepository.saveAndFlush(anime)

        val databaseSizeBeforeUpdate = animeRepository.findAll().size

        animeSearchRepository.save(anime)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll())
        // Update the anime
        val updatedAnime = animeRepository.findById(anime.id).get()
        // Disconnect from session so that the updates on updatedAnime are not directly saved in db
        em.detach(updatedAnime)
        updatedAnime.title = UPDATED_TITLE
        updatedAnime.discription = UPDATED_DISCRIPTION
        updatedAnime.cover = UPDATED_COVER
        updatedAnime.relaseDate = UPDATED_RELASE_DATE

        restAnimeMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedAnime.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedAnime))
        ).andExpect(status().isOk)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
        val testAnime = animeList[animeList.size - 1]
        assertThat(testAnime.title).isEqualTo(UPDATED_TITLE)
        assertThat(testAnime.discription).isEqualTo(UPDATED_DISCRIPTION)
        assertThat(testAnime.cover).isEqualTo(UPDATED_COVER)
        assertThat(testAnime.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val animeSearchList = IterableUtils.toList(animeSearchRepository.findAll())
            val testAnimeSearch = animeSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testAnimeSearch.title).isEqualTo(UPDATED_TITLE)
            assertThat(testAnimeSearch.discription).isEqualTo(UPDATED_DISCRIPTION)
            assertThat(testAnimeSearch.cover).isEqualTo(UPDATED_COVER)
            assertThat(testAnimeSearch.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
      }
    }

    @Test
    @Transactional
    fun putNonExistingAnime() {
        val databaseSizeBeforeUpdate = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll())
        anime.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnimeMockMvc.perform(put(ENTITY_API_URL_ID, anime.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(anime)))
            .andExpect(status().isBadRequest)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchAnime() {
        val databaseSizeBeforeUpdate = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll())
        anime.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnimeMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(anime))
        ).andExpect(status().isBadRequest)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamAnime() {
        val databaseSizeBeforeUpdate = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll());
        anime.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnimeMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(anime)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateAnimeWithPatch() {
        animeRepository.saveAndFlush(anime)
        
        
val databaseSizeBeforeUpdate = animeRepository.findAll().size

// Update the anime using partial update
val partialUpdatedAnime = Anime().apply {
    id = anime.id

    
        title = UPDATED_TITLE
        discription = UPDATED_DISCRIPTION
        relaseDate = UPDATED_RELASE_DATE
}


restAnimeMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedAnime.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedAnime)))
.andExpect(status().isOk)

// Validate the Anime in the database
val animeList = animeRepository.findAll()
assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
val testAnime = animeList.last()
    assertThat(testAnime.title).isEqualTo(UPDATED_TITLE)
    assertThat(testAnime.discription).isEqualTo(UPDATED_DISCRIPTION)
    assertThat(testAnime.cover).isEqualTo(DEFAULT_COVER)
    assertThat(testAnime.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateAnimeWithPatch() {
        animeRepository.saveAndFlush(anime)
        
        
val databaseSizeBeforeUpdate = animeRepository.findAll().size

// Update the anime using partial update
val partialUpdatedAnime = Anime().apply {
    id = anime.id

    
        title = UPDATED_TITLE
        discription = UPDATED_DISCRIPTION
        cover = UPDATED_COVER
        relaseDate = UPDATED_RELASE_DATE
}


restAnimeMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedAnime.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedAnime)))
.andExpect(status().isOk)

// Validate the Anime in the database
val animeList = animeRepository.findAll()
assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
val testAnime = animeList.last()
    assertThat(testAnime.title).isEqualTo(UPDATED_TITLE)
    assertThat(testAnime.discription).isEqualTo(UPDATED_DISCRIPTION)
    assertThat(testAnime.cover).isEqualTo(UPDATED_COVER)
    assertThat(testAnime.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
    }

    @Throws(Exception::class)
    fun patchNonExistingAnime() {
        val databaseSizeBeforeUpdate = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll())
        anime.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnimeMockMvc.perform(patch(ENTITY_API_URL_ID, anime.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(anime)))
            .andExpect(status().isBadRequest)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchAnime() {
        val databaseSizeBeforeUpdate = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll())
        anime.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnimeMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(anime)))
            .andExpect(status().isBadRequest)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamAnime() {
        val databaseSizeBeforeUpdate = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll());
        anime.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnimeMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(anime)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Anime in the database
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteAnime() {
        // Initialize the database
        animeRepository.saveAndFlush(anime)
        animeRepository.save(anime)
        animeSearchRepository.save(anime)
        val databaseSizeBeforeDelete = animeRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(animeSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the anime
        restAnimeMockMvc.perform(
            delete(ENTITY_API_URL_ID, anime.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val animeList = animeRepository.findAll()
        assertThat(animeList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(animeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchAnime() {
        // Initialize the database
        anime = animeRepository.saveAndFlush(anime)
        animeSearchRepository.save(anime)
        // Search the anime
        restAnimeMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${anime.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(anime.id?.toInt())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].discription").value(hasItem(DEFAULT_DISCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].cover").value(hasItem(DEFAULT_COVER)))
            .andExpect(jsonPath("$.[*].relaseDate").value(hasItem(DEFAULT_RELASE_DATE.toString())))    }

    companion object {

        private const val DEFAULT_TITLE = "AAAAAAAAAA"
        private const val UPDATED_TITLE = "BBBBBBBBBB"

        private const val DEFAULT_DISCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DISCRIPTION = "BBBBBBBBBB"

        private const val DEFAULT_COVER = "AAAAAAAAAA"
        private const val UPDATED_COVER = "BBBBBBBBBB"

        private val DEFAULT_RELASE_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_RELASE_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())


        private val ENTITY_API_URL: String = "/api/anime"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/anime"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Anime {
            val anime = Anime(
                title = DEFAULT_TITLE,

                discription = DEFAULT_DISCRIPTION,

                cover = DEFAULT_COVER,

                relaseDate = DEFAULT_RELASE_DATE

            )


            return anime
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Anime {
            val anime = Anime(
                title = UPDATED_TITLE,

                discription = UPDATED_DISCRIPTION,

                cover = UPDATED_COVER,

                relaseDate = UPDATED_RELASE_DATE

            )


            return anime
        }

    }
}
