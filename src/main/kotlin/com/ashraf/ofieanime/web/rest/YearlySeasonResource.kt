package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.YearlySeason
import com.ashraf.ofieanime.repository.YearlySeasonRepository
import com.ashraf.ofieanime.repository.search.YearlySeasonSearchRepository
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

private const val ENTITY_NAME = "yearlySeason"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.YearlySeason].
 */
@RestController
@RequestMapping("/api")
@Transactional
class YearlySeasonResource(
    private val yearlySeasonRepository: YearlySeasonRepository,
    private val yearlySeasonSearchRepository: YearlySeasonSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "yearlySeason"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /yearly-seasons` : Create a new yearlySeason.
     *
     * @param yearlySeason the yearlySeason to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new yearlySeason, or with status `400 (Bad Request)` if the yearlySeason has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/yearly-seasons")
    fun createYearlySeason(@RequestBody yearlySeason: YearlySeason): ResponseEntity<YearlySeason> {
        log.debug("REST request to save YearlySeason : $yearlySeason")
        if (yearlySeason.id != null) {
            throw BadRequestAlertException(
                "A new yearlySeason cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = yearlySeasonRepository.save(yearlySeason)
        yearlySeasonSearchRepository.index(result)
        return ResponseEntity.created(URI("/api/yearly-seasons/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /yearly-seasons/:id} : Updates an existing yearlySeason.
     *
     * @param id the id of the yearlySeason to save.
     * @param yearlySeason the yearlySeason to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated yearlySeason,
     * or with status `400 (Bad Request)` if the yearlySeason is not valid,
     * or with status `500 (Internal Server Error)` if the yearlySeason couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/yearly-seasons/{id}")
    fun updateYearlySeason(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody yearlySeason: YearlySeason
    ): ResponseEntity<YearlySeason> {
        log.debug("REST request to update YearlySeason : {}, {}", id, yearlySeason)
        if (yearlySeason.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, yearlySeason.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!yearlySeasonRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = yearlySeasonRepository.save(yearlySeason)
        yearlySeasonSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    yearlySeason.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /yearly-seasons/:id} : Partial updates given fields of an existing yearlySeason, field will ignore if it is null
     *
     * @param id the id of the yearlySeason to save.
     * @param yearlySeason the yearlySeason to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated yearlySeason,
     * or with status {@code 400 (Bad Request)} if the yearlySeason is not valid,
     * or with status {@code 404 (Not Found)} if the yearlySeason is not found,
     * or with status {@code 500 (Internal Server Error)} if the yearlySeason couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/yearly-seasons/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateYearlySeason(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody yearlySeason: YearlySeason
    ): ResponseEntity<YearlySeason> {
        log.debug("REST request to partial update YearlySeason partially : {}, {}", id, yearlySeason)
        if (yearlySeason.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, yearlySeason.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!yearlySeasonRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = yearlySeasonRepository.findById(yearlySeason.id)
            .map {

                if (yearlySeason.name != null) {
                    it.name = yearlySeason.name
                }

                it
            }
            .map { yearlySeasonRepository.save(it) }
            .map {
                yearlySeasonSearchRepository.save(it)

                it
            }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, yearlySeason.id.toString())
        )
    }

    /**
     * `GET  /yearly-seasons` : get all the yearlySeasons.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of yearlySeasons in body.
     */
    @GetMapping("/yearly-seasons") fun getAllYearlySeasons(): MutableList<YearlySeason> {

        log.debug("REST request to get all YearlySeasons")
        return yearlySeasonRepository.findAll()
    }

    /**
     * `GET  /yearly-seasons/:id` : get the "id" yearlySeason.
     *
     * @param id the id of the yearlySeason to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the yearlySeason, or with status `404 (Not Found)`.
     */
    @GetMapping("/yearly-seasons/{id}")
    fun getYearlySeason(@PathVariable id: Long): ResponseEntity<YearlySeason> {
        log.debug("REST request to get YearlySeason : $id")
        val yearlySeason = yearlySeasonRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(yearlySeason)
    }
    /**
     *  `DELETE  /yearly-seasons/:id` : delete the "id" yearlySeason.
     *
     * @param id the id of the yearlySeason to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/yearly-seasons/{id}")
    fun deleteYearlySeason(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete YearlySeason : $id")

        yearlySeasonRepository.deleteById(id)
        yearlySeasonSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/yearly-seasons?query=:query` : search for the yearlySeason corresponding
     * to the query.
     *
     * @param query the query of the yearlySeason search.
     * @return the result of the search.
     */
    @GetMapping("/_search/yearly-seasons")
    fun searchYearlySeasons(@RequestParam query: String): MutableList<YearlySeason> {
        log.debug("REST request to search YearlySeasons for query $query")
        return yearlySeasonSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
    }
}
