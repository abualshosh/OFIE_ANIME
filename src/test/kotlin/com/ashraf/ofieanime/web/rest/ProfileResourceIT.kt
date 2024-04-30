package com.ashraf.ofieanime.web.rest


import com.ashraf.ofieanime.IntegrationTest
import com.ashraf.ofieanime.domain.Profile
import com.ashraf.ofieanime.domain.User
import com.ashraf.ofieanime.repository.ProfileRepository
import com.ashraf.ofieanime.repository.UserRepository
import com.ashraf.ofieanime.repository.search.ProfileSearchRepository
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
 * Integration tests for the [ProfileResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProfileResourceIT {
    @Autowired
    private lateinit var profileRepository: ProfileRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var profileSearchRepository: ProfileSearchRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator


    @Autowired
    private lateinit var em: EntityManager


    @Autowired
    private lateinit var restProfileMockMvc: MockMvc

    private lateinit var profile: Profile


    @AfterEach
    fun cleanupElasticSearchRepository() {
        profileSearchRepository.deleteAll()
        assertThat(profileSearchRepository.count()).isEqualTo(0)
    }


    @BeforeEach
    fun initTest() {
        profile = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProfile() {
        val databaseSizeBeforeCreate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        // Create the Profile
        restProfileMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(profile))
        ).andExpect(status().isCreated)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeCreate + 1)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1)
        }
        val testProfile = profileList[profileList.size - 1]

        assertThat(testProfile.pictue).isEqualTo(DEFAULT_PICTUE)

        // Validate the id for MapsId, the ids must be same
        assertThat(testProfile.id).isEqualTo(testProfile.user?.id)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProfileWithExistingId() {
        // Create the Profile with an existing ID
        profile.id = 1L

        val databaseSizeBeforeCreate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        // An entity with an existing ID cannot be created, so this API call must fail
        restProfileMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(profile))
        ).andExpect(status().isBadRequest)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeCreate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun updateProfileMapsIdAssociationWithNewId() {
        // Initialize the database
        profileRepository.saveAndFlush(profile)
        val databaseSizeBeforeCreate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        // Add a new parent entity
        val user = UserResourceIT.createEntity(em)
        em.persist(user)
        em.flush()

        // Load the profile
        val updatedProfile = profileRepository.findById(profile.id).get()
        assertThat(updatedProfile).isNotNull
        // Disconnect from session so that the updates on updatedProfile are not directly saved in db
        em.detach(updatedProfile)

        // Update the User with new association value
        updatedProfile.user = user

        // Update the entity
        restProfileMockMvc.perform(put(ENTITY_API_URL_ID,updatedProfile.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(updatedProfile)))
            .andExpect(status().isOk)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeCreate)
        val testProfile = profileList[profileList.size - 1]

        // Validate the id for MapsId, the ids must be same
        // Uncomment the following line for assertion. However, please note that there is a known issue and uncommenting will fail the test.
        // Please look at https://github.com/jhipster/generator-jhipster/issues/9100. You can modify this test as necessary.
        // assertThat(testProfile.id).isEqualTo(testProfile.user?.id)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProfiles() {
        // Initialize the database
        profileRepository.saveAndFlush(profile)

        // Get all the profileList
        restProfileMockMvc.perform(get(ENTITY_API_URL+ "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(profile.id?.toInt())))
            .andExpect(jsonPath("$.[*].pictue").value(hasItem(DEFAULT_PICTUE)))    }
    
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getProfile() {
        // Initialize the database
        profileRepository.saveAndFlush(profile)

        val id = profile.id
        assertNotNull(id)

        // Get the profile
        restProfileMockMvc.perform(get(ENTITY_API_URL_ID, profile.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(profile.id?.toInt()))
            .andExpect(jsonPath("$.pictue").value(DEFAULT_PICTUE))    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingProfile() {
        // Get the profile
        restProfileMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putExistingProfile() {
        // Initialize the database
        profileRepository.saveAndFlush(profile)

        val databaseSizeBeforeUpdate = profileRepository.findAll().size

        profileSearchRepository.save(profile)
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        // Update the profile
        val updatedProfile = profileRepository.findById(profile.id).get()
        // Disconnect from session so that the updates on updatedProfile are not directly saved in db
        em.detach(updatedProfile)
        updatedProfile.pictue = UPDATED_PICTUE

        restProfileMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedProfile.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedProfile))
        ).andExpect(status().isOk)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
        val testProfile = profileList[profileList.size - 1]
        assertThat(testProfile.pictue).isEqualTo(UPDATED_PICTUE)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
            assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
            val profileSearchList = IterableUtils.toList(profileSearchRepository.findAll())
            val testProfileSearch = profileSearchList.get(searchDatabaseSizeAfter - 1)
            assertThat(testProfileSearch.pictue).isEqualTo(UPDATED_PICTUE)
      }
    }

    @Test
    @Transactional
    fun putNonExistingProfile() {
        val databaseSizeBeforeUpdate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        profile.id = count.incrementAndGet()


        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfileMockMvc.perform(put(ENTITY_API_URL_ID, profile.id).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(profile)))
            .andExpect(status().isBadRequest)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchProfile() {
        val databaseSizeBeforeUpdate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        profile.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfileMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(profile))
        ).andExpect(status().isBadRequest)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamProfile() {
        val databaseSizeBeforeUpdate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        profile.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfileMockMvc.perform(put(ENTITY_API_URL).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonBytes(profile)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateProfileWithPatch() {
        profileRepository.saveAndFlush(profile)
        
        
val databaseSizeBeforeUpdate = profileRepository.findAll().size

// Update the profile using partial update
val partialUpdatedProfile = Profile().apply {
    id = profile.id

}


restProfileMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedProfile.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedProfile)))
.andExpect(status().isOk)

// Validate the Profile in the database
val profileList = profileRepository.findAll()
assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
val testProfile = profileList.last()
    assertThat(testProfile.pictue).isEqualTo(DEFAULT_PICTUE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateProfileWithPatch() {
        profileRepository.saveAndFlush(profile)
        
        
val databaseSizeBeforeUpdate = profileRepository.findAll().size

// Update the profile using partial update
val partialUpdatedProfile = Profile().apply {
    id = profile.id

    
        pictue = UPDATED_PICTUE
}


restProfileMockMvc.perform(patch(ENTITY_API_URL_ID, partialUpdatedProfile.id).with(csrf())
.contentType("application/merge-patch+json")
.content(convertObjectToJsonBytes(partialUpdatedProfile)))
.andExpect(status().isOk)

// Validate the Profile in the database
val profileList = profileRepository.findAll()
assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
val testProfile = profileList.last()
    assertThat(testProfile.pictue).isEqualTo(UPDATED_PICTUE)
    }

    @Throws(Exception::class)
    fun patchNonExistingProfile() {
        val databaseSizeBeforeUpdate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        profile.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfileMockMvc.perform(patch(ENTITY_API_URL_ID, profile.id).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(profile)))
            .andExpect(status().isBadRequest)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchProfile() {
        val databaseSizeBeforeUpdate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        profile.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfileMockMvc.perform(patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(profile)))
            .andExpect(status().isBadRequest)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamProfile() {
        val databaseSizeBeforeUpdate = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll());
        profile.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfileMockMvc.perform(patch(ENTITY_API_URL).with(csrf())
            .contentType("application/merge-patch+json")
            .content(convertObjectToJsonBytes(profile)))
            .andExpect(status().isMethodNotAllowed)

        // Validate the Profile in the database
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeUpdate)
        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteProfile() {
        // Initialize the database
        profileRepository.saveAndFlush(profile)
        profileRepository.save(profile)
        profileSearchRepository.save(profile)
        val databaseSizeBeforeDelete = profileRepository.findAll().size
        val searchDatabaseSizeBefore = IterableUtil.sizeOf(profileSearchRepository.findAll())
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete)
        // Delete the profile
        restProfileMockMvc.perform(
            delete(ENTITY_API_URL_ID, profile.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val profileList = profileRepository.findAll()
        assertThat(profileList).hasSize(databaseSizeBeforeDelete - 1)

        val searchDatabaseSizeAfter = IterableUtil.sizeOf(profileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore-1);
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun searchProfile() {
        // Initialize the database
        profile = profileRepository.saveAndFlush(profile)
        profileSearchRepository.save(profile)
        // Search the profile
        restProfileMockMvc.perform(get("$ENTITY_SEARCH_API_URL?query=id:${profile.id}"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(profile.id?.toInt())))
            .andExpect(jsonPath("$.[*].pictue").value(hasItem(DEFAULT_PICTUE)))    }

    companion object {

        private const val DEFAULT_PICTUE = "AAAAAAAAAA"
        private const val UPDATED_PICTUE = "BBBBBBBBBB"


        private val ENTITY_API_URL: String = "/api/profiles"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"
        private val ENTITY_SEARCH_API_URL: String = "/api/_search/profiles"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + ( 2 * Integer.MAX_VALUE ))




        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Profile {
            val profile = Profile(
                pictue = DEFAULT_PICTUE

            )


            // Add required entity
            val user = UserResourceIT.createEntity(em)
            em.persist(user)
            em.flush()
            profile.user = user
            return profile
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Profile {
            val profile = Profile(
                pictue = UPDATED_PICTUE

            )


            // Add required entity
            val user = UserResourceIT.createEntity(em)
            em.persist(user)
            em.flush()
            profile.user = user
            return profile
        }

    }
}
