package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Season
import com.ashraf.ofieanime.repository.SeasonRepository
import com.ashraf.ofieanime.repository.search.SeasonSearchRepository
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

import com.ashraf.ofieanime.domain.enumeration.Type
import com.ashraf.ofieanime.domain.enumeration.SeasonType

/**
 * Integration tests for the [SeasonResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SeasonResourceIT {
    @Autowired
    private lateinit var seasonRepository: SeasonRepository
    @Autowired
    private lateinit var seasonSearchRepository: SeasonSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restSeasonMockMvc: MockMvc

    private lateinit var season: Season


    @AfterEach
    fun cleanupElasticSearchRepository() {
        seasonSearchRepository.deleteAll()
        assertThat(seasonSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        season = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSeason() {
        val databaseSizeBeforeCreate = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        // Create the Season
        restSeasonMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(season))
        ).andExpect(status().isCreated)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testSeason = seasonList[seasonList.size - 1]

        assertThat(testSeason.titleInJapan).isEqualTo(DEFAULT_TITLE_IN_JAPAN)
        assertThat(testSeason.titleInEnglis).isEqualTo(DEFAULT_TITLE_IN_ENGLIS)
        assertThat(testSeason.relaseDate).isEqualTo(DEFAULT_RELASE_DATE)
        assertThat(testSeason.addDate).isEqualTo(DEFAULT_ADD_DATE)
        assertThat(testSeason.startDate).isEqualTo(DEFAULT_START_DATE)
        assertThat(testSeason.endDate).isEqualTo(DEFAULT_END_DATE)
        assertThat(testSeason.avrgeEpisodeLength).isEqualTo(DEFAULT_AVRGE_EPISODE_LENGTH)
        assertThat(testSeason.type).isEqualTo(DEFAULT_TYPE)
        assertThat(testSeason.seasonType).isEqualTo(DEFAULT_SEASON_TYPE)
        assertThat(testSeason.cover).isEqualTo(DEFAULT_COVER)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createSeasonWithExistingId() {
        // Create the Season with an existing ID
        season.id = 1L

        val databaseSizeBeforeCreate = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restSeasonMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(season))
        ).andExpect(status().isBadRequest)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllSeasons() {
        // Initialize the database
        seasonRepository.saveAndFlush(season)

        // Get all the seasonList
        restSeasonMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(season.id?.toInt())))
            .andExpect(jsonPath("$.[*].titleInJapan").value(hasItem(DEFAULT_TITLE_IN_JAPAN)))
            .andExpect(jsonPath("$.[*].titleInEnglis").value(hasItem(DEFAULT_TITLE_IN_ENGLIS)))
            .andExpect(jsonPath("$.[*].relaseDate").value(hasItem(DEFAULT_RELASE_DATE.toString())))
            .andExpect(jsonPath("$.[*].addDate").value(hasItem(DEFAULT_ADD_DATE.toString())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].avrgeEpisodeLength").value(hasItem(DEFAULT_AVRGE_EPISODE_LENGTH)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].seasonType").value(hasItem(DEFAULT_SEASON_TYPE.toString())))
            .andExpect(jsonPath("$.[*].cover").value(hasItem(DEFAULT_COVER)))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getSeason() {
        // Initialize the database
        seasonRepository.saveAndFlush(season)

        val id = season.id
        assertNotNull(id)

        // Get the season
        restSeasonMockMvc.perform(get(ENTITY_API_URL_ID, season.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(season.id?.toInt()))
            .andExpect(jsonPath("$.titleInJapan").value(DEFAULT_TITLE_IN_JAPAN))
            .andExpect(jsonPath("$.titleInEnglis").value(DEFAULT_TITLE_IN_ENGLIS))
            .andExpect(jsonPath("$.relaseDate").value(DEFAULT_RELASE_DATE.toString()))
            .andExpect(jsonPath("$.addDate").value(DEFAULT_ADD_DATE.toString()))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.avrgeEpisodeLength").value(DEFAULT_AVRGE_EPISODE_LENGTH))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.seasonType").value(DEFAULT_SEASON_TYPE.toString()))
            .andExpect(jsonPath("$.cover").value(DEFAULT_COVER))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingSeason() {
        // Get the season
        restSeasonMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingSeason() {
        // Initialize the database
        seasonRepository.saveAndFlush(season)

        val databaseSizeBeforeUpdate = seasonRepository.findAll().size

        seasonSearchRepository.save(season)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        // Update the season
        val updatedSeason = seasonRepository.findById(season.id).get()
        // Disconnect from session so that the updates on updatedSeason are not directly saved in db
        em.detach(updatedSeason)
        updatedSeason.titleInJapan = UPDATED_TITLE_IN_JAPAN
        updatedSeason.titleInEnglis = UPDATED_TITLE_IN_ENGLIS
        updatedSeason.relaseDate = UPDATED_RELASE_DATE
        updatedSeason.addDate = UPDATED_ADD_DATE
        updatedSeason.startDate = UPDATED_START_DATE
        updatedSeason.endDate = UPDATED_END_DATE
        updatedSeason.avrgeEpisodeLength = UPDATED_AVRGE_EPISODE_LENGTH
        updatedSeason.type = UPDATED_TYPE
        updatedSeason.seasonType = UPDATED_SEASON_TYPE
        updatedSeason.cover = UPDATED_COVER

        restSeasonMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedSeason.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedSeason))
        ).andExpect(status().isOk)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
        val testSeason = seasonList[seasonList.size - 1]
        assertThat(testSeason.titleInJapan).isEqualTo(UPDATED_TITLE_IN_JAPAN)
        assertThat(testSeason.titleInEnglis).isEqualTo(UPDATED_TITLE_IN_ENGLIS)
        assertThat(testSeason.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
        assertThat(testSeason.addDate).isEqualTo(UPDATED_ADD_DATE)
        assertThat(testSeason.startDate).isEqualTo(UPDATED_START_DATE)
        assertThat(testSeason.endDate).isEqualTo(UPDATED_END_DATE)
        assertThat(testSeason.avrgeEpisodeLength).isEqualTo(UPDATED_AVRGE_EPISODE_LENGTH)
        assertThat(testSeason.type).isEqualTo(UPDATED_TYPE)
        assertThat(testSeason.seasonType).isEqualTo(UPDATED_SEASON_TYPE)
        assertThat(testSeason.cover).isEqualTo(UPDATED_COVER)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val seasonSearchList = IterableUtils.toList(seasonSearchRepository.findAll())
            val testSeasonSearch = seasonSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testSeasonSearch.titleInJapan).isEqualTo(UPDATED_TITLE_IN_JAPAN)
            assertThat(testSeasonSearch.titleInEnglis).isEqualTo(UPDATED_TITLE_IN_ENGLIS)
            assertThat(testSeasonSearch.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
            assertThat(testSeasonSearch.addDate).isEqualTo(UPDATED_ADD_DATE)
            assertThat(testSeasonSearch.startDate).isEqualTo(UPDATED_START_DATE)
            assertThat(testSeasonSearch.endDate).isEqualTo(UPDATED_END_DATE)
            assertThat(testSeasonSearch.avrgeEpisodeLength).isEqualTo(UPDATED_AVRGE_EPISODE_LENGTH)
            assertThat(testSeasonSearch.type).isEqualTo(UPDATED_TYPE)
            assertThat(testSeasonSearch.seasonType).isEqualTo(UPDATED_SEASON_TYPE)
            assertThat(testSeasonSearch.cover).isEqualTo(UPDATED_COVER)
      }
    }

    @Test
    @Transactional
    fun putNonExistingSeason() {
        val databaseSizeBeforeUpdate = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        season.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSeasonMockMvc.perform(put(ENTITY_API_URL_ID, season.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(season)))
            .andExpect(status().isBadRequest)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchSeason() {
        val databaseSizeBeforeUpdate = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        season.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSeasonMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(season))
        ).andExpect(status().isBadRequest)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamSeason() {
        val databaseSizeBeforeUpdate = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll());
        season.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSeasonMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(season)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateSeasonWithPatch() {
        seasonRepository.saveAndFlush(season)
        
        
val databaseSizeBeforeUpdate = seasonRepository.findAll().size

// Update the season using partial update
val partialUpdatedSeason = Season().apply {
    id = season.id

    
        titleInJapan = UPDATED_TITLE_IN_JAPAN
        titleInEnglis = UPDATED_TITLE_IN_ENGLIS
        avrgeEpisodeLength = UPDATED_AVRGE_EPISODE_LENGTH
        type = UPDATED_TYPE
        seasonType = UPDATED_SEASON_TYPE
        cover = UPDATED_COVER
}


restSeasonMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedSeason.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedSeason)))
.andExpect(status().isOk)

// Validate the Season in the database
val seasonList = seasonRepository.findAll()
assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
val testSeason = seasonList.last()
    assertThat(testSeason.titleInJapan).isEqualTo(UPDATED_TITLE_IN_JAPAN)
    assertThat(testSeason.titleInEnglis).isEqualTo(UPDATED_TITLE_IN_ENGLIS)
    assertThat(testSeason.relaseDate).isEqualTo(DEFAULT_RELASE_DATE)
    assertThat(testSeason.addDate).isEqualTo(DEFAULT_ADD_DATE)
    assertThat(testSeason.startDate).isEqualTo(DEFAULT_START_DATE)
    assertThat(testSeason.endDate).isEqualTo(DEFAULT_END_DATE)
    assertThat(testSeason.avrgeEpisodeLength).isEqualTo(UPDATED_AVRGE_EPISODE_LENGTH)
    assertThat(testSeason.type).isEqualTo(UPDATED_TYPE)
    assertThat(testSeason.seasonType).isEqualTo(UPDATED_SEASON_TYPE)
    assertThat(testSeason.cover).isEqualTo(UPDATED_COVER)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateSeasonWithPatch() {
        seasonRepository.saveAndFlush(season)
        
        
val databaseSizeBeforeUpdate = seasonRepository.findAll().size

// Update the season using partial update
val partialUpdatedSeason = Season().apply {
    id = season.id

    
        titleInJapan = UPDATED_TITLE_IN_JAPAN
        titleInEnglis = UPDATED_TITLE_IN_ENGLIS
        relaseDate = UPDATED_RELASE_DATE
        addDate = UPDATED_ADD_DATE
        startDate = UPDATED_START_DATE
        endDate = UPDATED_END_DATE
        avrgeEpisodeLength = UPDATED_AVRGE_EPISODE_LENGTH
        type = UPDATED_TYPE
        seasonType = UPDATED_SEASON_TYPE
        cover = UPDATED_COVER
}


restSeasonMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedSeason.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedSeason)))
.andExpect(status().isOk)

// Validate the Season in the database
val seasonList = seasonRepository.findAll()
assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
val testSeason = seasonList.last()
    assertThat(testSeason.titleInJapan).isEqualTo(UPDATED_TITLE_IN_JAPAN)
    assertThat(testSeason.titleInEnglis).isEqualTo(UPDATED_TITLE_IN_ENGLIS)
    assertThat(testSeason.relaseDate).isEqualTo(UPDATED_RELASE_DATE)
    assertThat(testSeason.addDate).isEqualTo(UPDATED_ADD_DATE)
    assertThat(testSeason.startDate).isEqualTo(UPDATED_START_DATE)
    assertThat(testSeason.endDate).isEqualTo(UPDATED_END_DATE)
    assertThat(testSeason.avrgeEpisodeLength).isEqualTo(UPDATED_AVRGE_EPISODE_LENGTH)
    assertThat(testSeason.type).isEqualTo(UPDATED_TYPE)
    assertThat(testSeason.seasonType).isEqualTo(UPDATED_SEASON_TYPE)
    assertThat(testSeason.cover).isEqualTo(UPDATED_COVER)
    }

    @Throws(Exception::class)
    fun patchNonExistingSeason() {
        val databaseSizeBeforeUpdate = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        season.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSeasonMockMvc.perform(patch(ENTITY_API_URL_ID, season.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(season)))
            .andExpect(status().isBadRequest)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchSeason() {
        val databaseSizeBeforeUpdate = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        season.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSeasonMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(season)))
            .andExpect(status().isBadRequest)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamSeason() {
        val databaseSizeBeforeUpdate = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll());
        season.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSeasonMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(season)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Season in the database
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteSeason() {
        // Initialize the database
        seasonRepository.saveAndFlush(season)
        seasonRepository.save(season)
        seasonSearchRepository.save(season)
        val databaseSizeBeforeDelete = seasonRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(seasonSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the season
        restSeasonMockMvc.perform(
            delete(ENTITY_API_URL_ID, season.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val seasonList = seasonRepository.findAll()
        assertThat(seasonList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(seasonSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchSeason() {
        // Initialize the database
        season = seasonRepository.saveAndFlush(season)
        seasonSearchRepository.save(season)
        // Search the season
        restSeasonMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${season.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(season.id?.toInt())))
            .andExpect(jsonPath("$.[*].titleInJapan").value(hasItem(DEFAULT_TITLE_IN_JAPAN)))
            .andExpect(jsonPath("$.[*].titleInEnglis").value(hasItem(DEFAULT_TITLE_IN_ENGLIS)))
            .andExpect(jsonPath("$.[*].relaseDate").value(hasItem(DEFAULT_RELASE_DATE.toString())))
            .andExpect(jsonPath("$.[*].addDate").value(hasItem(DEFAULT_ADD_DATE.toString())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].avrgeEpisodeLength").value(hasItem(DEFAULT_AVRGE_EPISODE_LENGTH)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].seasonType").value(hasItem(DEFAULT_SEASON_TYPE.toString())))
            .andExpect(jsonPath("$.[*].cover").value(hasItem(DEFAULT_COVER)))    }

    companion object {

        private const val DEFAULT_TITLE_IN_JAPAN = "AAAAAAAAAA"
        private const val UPDATED_TITLE_IN_JAPAN = "BBBBBBBBBB"

        private const val DEFAULT_TITLE_IN_ENGLIS = "AAAAAAAAAA"
        private const val UPDATED_TITLE_IN_ENGLIS = "BBBBBBBBBB"

        private val DEFAULT_RELASE_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_RELASE_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_ADD_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_ADD_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_START_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_START_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_END_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_END_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private const val DEFAULT_AVRGE_EPISODE_LENGTH = "AAAAAAAAAA"
        private const val UPDATED_AVRGE_EPISODE_LENGTH = "BBBBBBBBBB"

        private val DEFAULT_TYPE: Type = Type.MOVIE
        private val UPDATED_TYPE: Type = Type.EPISODE

        private val DEFAULT_SEASON_TYPE: SeasonType = SeasonType.OVA
        private val UPDATED_SEASON_TYPE: SeasonType = SeasonType.ONA

        private const val DEFAULT_COVER = "AAAAAAAAAA"
        private const val UPDATED_COVER = "BBBBBBBBBB"


        private val ENTITY_API_URL: String = "/api/seasons"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/seasons"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Season {
            val season = Season(
                titleInJapan = DEFAULT_TITLE_IN_JAPAN,

                titleInEnglis = DEFAULT_TITLE_IN_ENGLIS,

                relaseDate = DEFAULT_RELASE_DATE,

                addDate = DEFAULT_ADD_DATE,

                startDate = DEFAULT_START_DATE,

                endDate = DEFAULT_END_DATE,

                avrgeEpisodeLength = DEFAULT_AVRGE_EPISODE_LENGTH,

                type = DEFAULT_TYPE,

                seasonType = DEFAULT_SEASON_TYPE,

                cover = DEFAULT_COVER

            )


            return season
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Season {
            val season = Season(
                titleInJapan = UPDATED_TITLE_IN_JAPAN,

                titleInEnglis = UPDATED_TITLE_IN_ENGLIS,

                relaseDate = UPDATED_RELASE_DATE,

                addDate = UPDATED_ADD_DATE,

                startDate = UPDATED_START_DATE,

                endDate = UPDATED_END_DATE,

                avrgeEpisodeLength = UPDATED_AVRGE_EPISODE_LENGTH,

                type = UPDATED_TYPE,

                seasonType = UPDATED_SEASON_TYPE,

                cover = UPDATED_COVER

            )


            return season
        }

    }
}
