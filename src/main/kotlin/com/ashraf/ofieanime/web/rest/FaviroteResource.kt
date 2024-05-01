package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Favirote
import com.ashraf.ofieanime.repository.FaviroteRepository
import com.ashraf.ofieanime.repository.search.FaviroteSearchRepository
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

private const val ENTITY_NAME = "favirote"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Favirote].
 */
@RestController
@RequestMapping("/api")
@Transactional
class FaviroteResource(
    private val faviroteRepository: FaviroteRepository,
    private val faviroteSearchRepository: FaviroteSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "favirote"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /favirotes` : Create a new favirote.
     *
     * @param favirote the favirote to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new favirote, or with status `400 (Bad Request)` if the favirote has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/favirotes")
    fun createFavirote(@RequestBody favirote: Favirote): ResponseEntity<Favirote> {
        log.debug("REST request to save Favirote : $favirote")
        if (favirote.id != null) {
            throw BadRequestAlertException(
                "A new favirote cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = faviroteRepository.save(favirote)
        faviroteSearchRepository.index(result)
        return ResponseEntity.created(URI("/api/favirotes/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /favirotes/:id} : Updates an existing favirote.
     *
     * @param id the id of the favirote to save.
     * @param favirote the favirote to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated favirote,
     * or with status `400 (Bad Request)` if the favirote is not valid,
     * or with status `500 (Internal Server Error)` if the favirote couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/favirotes/{id}")
    fun updateFavirote(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody favirote: Favirote
    ): ResponseEntity<Favirote> {
        log.debug("REST request to update Favirote : {}, {}", id, favirote)
        if (favirote.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, favirote.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!faviroteRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = faviroteRepository.save(favirote)
        faviroteSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    favirote.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /favirotes/:id} : Partial updates given fields of an existing favirote, field will ignore if it is null
     *
     * @param id the id of the favirote to save.
     * @param favirote the favirote to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated favirote,
     * or with status {@code 400 (Bad Request)} if the favirote is not valid,
     * or with status {@code 404 (Not Found)} if the favirote is not found,
     * or with status {@code 500 (Internal Server Error)} if the favirote couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/favirotes/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateFavirote(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody favirote: Favirote
    ): ResponseEntity<Favirote> {
        log.debug("REST request to partial update Favirote partially : {}, {}", id, favirote)
        if (favirote.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, favirote.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!faviroteRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = faviroteRepository.findById(favirote.id)
            .map {

                if (favirote.addDate != null) {
                    it.addDate = favirote.addDate
                }

                it
            }
            .map { faviroteRepository.save(it) }
            .map {
                faviroteSearchRepository.save(it)

                it
            }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, favirote.id.toString())
        )
    }

    /**
     * `GET  /favirotes` : get all the favirotes.
     *

     * @param filter the filter of the request.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of favirotes in body.
     */
    @GetMapping("/favirotes") fun getAllFavirotes(@RequestParam(required = false) filter: String?): MutableList<Favirote> {

        if ("profile-is-null".equals(filter)) {
            log.debug("REST request to get all Favirotes where profile is null")
            return faviroteRepository.findAll()
                .asSequence()
                .filter { it.profile == null }
                .toMutableList()
        } else {
            log.debug("REST request to get all Favirotes")
            return faviroteRepository.findAll()
        }
    }

    /**
     * `GET  /favirotes/:id` : get the "id" favirote.
     *
     * @param id the id of the favirote to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the favirote, or with status `404 (Not Found)`.
     */
    @GetMapping("/favirotes/{id}")
    fun getFavirote(@PathVariable id: Long): ResponseEntity<Favirote> {
        log.debug("REST request to get Favirote : $id")
        val favirote = faviroteRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(favirote)
    }
    /**
     *  `DELETE  /favirotes/:id` : delete the "id" favirote.
     *
     * @param id the id of the favirote to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/favirotes/{id}")
    fun deleteFavirote(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Favirote : $id")

        faviroteRepository.deleteById(id)
        faviroteSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/favirotes?query=:query` : search for the favirote corresponding
     * to the query.
     *
     * @param query the query of the favirote search.
     * @return the result of the search.
     */
    @GetMapping("/_search/favirotes")
    fun searchFavirotes(@RequestParam query: String): MutableList<Favirote> {
        log.debug("REST request to search Favirotes for query $query")
        return faviroteSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
    }
}
