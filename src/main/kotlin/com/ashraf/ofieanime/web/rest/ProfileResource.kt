package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Profile
import com.ashraf.ofieanime.repository.ProfileRepository
import com.ashraf.ofieanime.repository.UserRepository
import com.ashraf.ofieanime.repository.search.ProfileSearchRepository
import com.ashraf.ofieanime.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import java.util.stream.Collectors

private const val ENTITY_NAME = "profile"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Profile].
 */
@RestController
@RequestMapping("/api")
@Transactional
class ProfileResource(
    private val profileRepository: ProfileRepository,
    private val profileSearchRepository: ProfileSearchRepository,
    private val userRepository: UserRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "profile"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /profiles` : Create a new profile.
     *
     * @param profile the profile to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new profile, or with status `400 (Bad Request)` if the profile has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/profiles")
    fun createProfile(@RequestBody profile: Profile): ResponseEntity<Profile> {
        log.debug("REST request to save Profile : $profile")
        if (profile.id != null) {
            throw BadRequestAlertException(
                "A new profile cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        if (Objects.isNull(profile.user)) {
            throw BadRequestAlertException("Invalid association value provided", ENTITY_NAME, "null")
        }
        val userId = profile.user?.id
        if (userId != null) {
            userRepository.findById(userId)
                .ifPresent { profile.user = it }
        }
        val result = profileRepository.save(profile)
        profileSearchRepository.index(result)
        return ResponseEntity.created(URI("/api/profiles/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /profiles/:id} : Updates an existing profile.
     *
     * @param id the id of the profile to save.
     * @param profile the profile to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated profile,
     * or with status `400 (Bad Request)` if the profile is not valid,
     * or with status `500 (Internal Server Error)` if the profile couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/profiles/{id}")
    fun updateProfile(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody profile: Profile
    ): ResponseEntity<Profile> {
        log.debug("REST request to update Profile : {}, {}", id, profile)
        if (profile.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, profile.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!profileRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = profileRepository.save(profile)
        profileSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    profile.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /profiles/:id} : Partial updates given fields of an existing profile, field will ignore if it is null
     *
     * @param id the id of the profile to save.
     * @param profile the profile to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated profile,
     * or with status {@code 400 (Bad Request)} if the profile is not valid,
     * or with status {@code 404 (Not Found)} if the profile is not found,
     * or with status {@code 500 (Internal Server Error)} if the profile couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/profiles/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateProfile(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody profile: Profile
    ): ResponseEntity<Profile> {
        log.debug("REST request to partial update Profile partially : {}, {}", id, profile)
        if (profile.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, profile.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!profileRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = profileRepository.findById(profile.id)
            .map {

                if (profile.pictue != null) {
                    it.pictue = profile.pictue
                }

                it
            }
            .map { profileRepository.save(it) }
            .map {
                profileSearchRepository.save(it)

                it
            }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, profile.id.toString())
        )
    }

    /**
     * `GET  /profiles` : get all the profiles.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of profiles in body.
     */
    @GetMapping("/profiles")
    @Transactional(readOnly = true) fun getAllProfiles(): MutableList<Profile> {

        log.debug("REST request to get all Profiles")
        return profileRepository.findAll()
    }

    /**
     * `GET  /profiles/:id` : get the "id" profile.
     *
     * @param id the id of the profile to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the profile, or with status `404 (Not Found)`.
     */
    @GetMapping("/profiles/{id}")
    @Transactional(readOnly = true)
    fun getProfile(@PathVariable id: Long): ResponseEntity<Profile> {
        log.debug("REST request to get Profile : $id")
        val profile = profileRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(profile)
    }
    /**
     *  `DELETE  /profiles/:id` : delete the "id" profile.
     *
     * @param id the id of the profile to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/profiles/{id}")
    fun deleteProfile(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Profile : $id")

        profileRepository.deleteById(id)
        profileSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/profiles?query=:query` : search for the profile corresponding
     * to the query.
     *
     * @param query the query of the profile search.
     * @return the result of the search.
     */
    @GetMapping("/_search/profiles")
    fun searchProfiles(@RequestParam query: String): MutableList<Profile> {
        log.debug("REST request to search Profiles for query $query")
        return profileSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
    }
}
