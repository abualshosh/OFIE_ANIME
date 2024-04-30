package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Character
import com.ashraf.ofieanime.repository.CharacterRepository
import com.ashraf.ofieanime.repository.search.CharacterSearchRepository
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
 * Integration tests for the [CharacterResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CharacterResourceIT {
    @Autowired
    private lateinit var characterRepository: CharacterRepository
    @Autowired
    private lateinit var characterSearchRepository: CharacterSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restCharacterMockMvc: MockMvc

    private lateinit var character: Character


    @AfterEach
    fun cleanupElasticSearchRepository() {
        characterSearchRepository.deleteAll()
        assertThat(characterSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        character = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCharacter() {
        val databaseSizeBeforeCreate = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll())
        // Create the Character
        restCharacterMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(character))
        ).andExpect(status().isCreated)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testCharacter = characterList[characterList.size - 1]

        assertThat(testCharacter.name).isEqualTo(DEFAULT_NAME)
        assertThat(testCharacter.picture).isEqualTo(DEFAULT_PICTURE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCharacterWithExistingId() {
        // Create the Character with an existing ID
        character.id = 1L

        val databaseSizeBeforeCreate = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restCharacterMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(character))
        ).andExpect(status().isBadRequest)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }


    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllCharacters() {
        // Initialize the database
        characterRepository.saveAndFlush(character)

        // Get all the characterList
        restCharacterMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(character.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].picture").value(hasItem(DEFAULT_PICTURE)))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getCharacter() {
        // Initialize the database
        characterRepository.saveAndFlush(character)

        val id = character.id
        assertNotNull(id)

        // Get the character
        restCharacterMockMvc.perform(get(ENTITY_API_URL_ID, character.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(character.id?.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.picture").value(DEFAULT_PICTURE))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingCharacter() {
        // Get the character
        restCharacterMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingCharacter() {
        // Initialize the database
        characterRepository.saveAndFlush(character)

        val databaseSizeBeforeUpdate = characterRepository.findAll().size

        characterSearchRepository.save(character)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll())
        // Update the character
        val updatedCharacter = characterRepository.findById(character.id).get()
        // Disconnect from session so that the updates on updatedCharacter are not directly saved in db
        em.detach(updatedCharacter)
        updatedCharacter.name = UPDATED_NAME
        updatedCharacter.picture = UPDATED_PICTURE

        restCharacterMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedCharacter.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedCharacter))
        ).andExpect(status().isOk)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
        val testCharacter = characterList[characterList.size - 1]
        assertThat(testCharacter.name).isEqualTo(UPDATED_NAME)
        assertThat(testCharacter.picture).isEqualTo(UPDATED_PICTURE)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val characterSearchList = IterableUtils.toList(characterSearchRepository.findAll())
            val testCharacterSearch = characterSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testCharacterSearch.name).isEqualTo(UPDATED_NAME)
            assertThat(testCharacterSearch.picture).isEqualTo(UPDATED_PICTURE)
      }
    }

    @Test
    @Transactional
    fun putNonExistingCharacter() {
        val databaseSizeBeforeUpdate = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll())
        character.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCharacterMockMvc.perform(put(ENTITY_API_URL_ID, character.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(character)))
            .andExpect(status().isBadRequest)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchCharacter() {
        val databaseSizeBeforeUpdate = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll())
        character.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCharacterMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(character))
        ).andExpect(status().isBadRequest)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamCharacter() {
        val databaseSizeBeforeUpdate = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll());
        character.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCharacterMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(character)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateCharacterWithPatch() {
        characterRepository.saveAndFlush(character)
        
        
val databaseSizeBeforeUpdate = characterRepository.findAll().size

// Update the character using partial update
val partialUpdatedCharacter = Character().apply {
    id = character.id

    
        picture = UPDATED_PICTURE
}


restCharacterMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedCharacter.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedCharacter)))
.andExpect(status().isOk)

// Validate the Character in the database
val characterList = characterRepository.findAll()
assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
val testCharacter = characterList.last()
    assertThat(testCharacter.name).isEqualTo(DEFAULT_NAME)
    assertThat(testCharacter.picture).isEqualTo(UPDATED_PICTURE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateCharacterWithPatch() {
        characterRepository.saveAndFlush(character)
        
        
val databaseSizeBeforeUpdate = characterRepository.findAll().size

// Update the character using partial update
val partialUpdatedCharacter = Character().apply {
    id = character.id

    
        name = UPDATED_NAME
        picture = UPDATED_PICTURE
}


restCharacterMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedCharacter.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedCharacter)))
.andExpect(status().isOk)

// Validate the Character in the database
val characterList = characterRepository.findAll()
assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
val testCharacter = characterList.last()
    assertThat(testCharacter.name).isEqualTo(UPDATED_NAME)
    assertThat(testCharacter.picture).isEqualTo(UPDATED_PICTURE)
    }

    @Throws(Exception::class)
    fun patchNonExistingCharacter() {
        val databaseSizeBeforeUpdate = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll())
        character.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCharacterMockMvc.perform(patch(ENTITY_API_URL_ID, character.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(character)))
            .andExpect(status().isBadRequest)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchCharacter() {
        val databaseSizeBeforeUpdate = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll())
        character.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCharacterMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(character)))
            .andExpect(status().isBadRequest)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamCharacter() {
        val databaseSizeBeforeUpdate = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll());
        character.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCharacterMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(character)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Character in the database
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteCharacter() {
        // Initialize the database
        characterRepository.saveAndFlush(character)
        characterRepository.save(character)
        characterSearchRepository.save(character)
        val databaseSizeBeforeDelete = characterRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(characterSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the character
        restCharacterMockMvc.perform(
            delete(ENTITY_API_URL_ID, character.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val characterList = characterRepository.findAll()
        assertThat(characterList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(characterSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchCharacter() {
        // Initialize the database
        character = characterRepository.saveAndFlush(character)
        characterSearchRepository.save(character)
        // Search the character
        restCharacterMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${character.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(character.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].picture").value(hasItem(DEFAULT_PICTURE)))    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private const val DEFAULT_PICTURE = "AAAAAAAAAA"
        private const val UPDATED_PICTURE = "BBBBBBBBBB"


        private val ENTITY_API_URL: String = "/api/characters"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/characters"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Character {
            val character = Character(
                name = DEFAULT_NAME,

                picture = DEFAULT_PICTURE

            )


            return character
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Character {
            val character = Character(
                name = UPDATED_NAME,

                picture = UPDATED_PICTURE

            )


            return character
        }

    }
}
