package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.History
import com.ashraf.ofieanime.repository.HistoryRepository
import com.ashraf.ofieanime.repository.search.HistorySearchRepository
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

private const val ENTITY_NAME = "history"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.History].
 */
@RestController
@RequestMapping("/api")
@Transactional
class HistoryResource(
    private val historyRepository: HistoryRepository,
    private val historySearchRepository: HistorySearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "history"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /histories` : Create a new history.
     *
     * @param history the history to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new history, or with status `400 (Bad Request)` if the history has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/histories")
    fun createHistory(@RequestBody history: History): ResponseEntity<History> {
        log.debug("REST request to save History : $history")
        if (history.id != null) {
            throw BadRequestAlertException(
                "A new history cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = historyRepository.save(history)
        historySearchRepository.index(result)
        return ResponseEntity.created(URI("/api/histories/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /histories/:id} : Updates an existing history.
     *
     * @param id the id of the history to save.
     * @param history the history to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated history,
     * or with status `400 (Bad Request)` if the history is not valid,
     * or with status `500 (Internal Server Error)` if the history couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/histories/{id}")
    fun updateHistory(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody history: History
    ): ResponseEntity<History> {
        log.debug("REST request to update History : {}, {}", id, history)
        if (history.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, history.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!historyRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = historyRepository.save(history)
        historySearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    history.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /histories/:id} : Partial updates given fields of an existing history, field will ignore if it is null
     *
     * @param id the id of the history to save.
     * @param history the history to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated history,
     * or with status {@code 400 (Bad Request)} if the history is not valid,
     * or with status {@code 404 (Not Found)} if the history is not found,
     * or with status {@code 500 (Internal Server Error)} if the history couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/histories/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateHistory(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody history: History
    ): ResponseEntity<History> {
        log.debug("REST request to partial update History partially : {}, {}", id, history)
        if (history.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, history.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!historyRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = historyRepository.findById(history.id)
            .map {

                if (history.date != null) {
                    it.date = history.date
                }

                it
            }
            .map { historyRepository.save(it) }
            .map {
                historySearchRepository.save(it)

                it
            }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, history.id.toString())
        )
    }

    /**
     * `GET  /histories` : get all the histories.
     *

     * @param filter the filter of the request.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of histories in body.
     */
    @GetMapping("/histories") fun getAllHistories(@RequestParam(required = false) filter: String?): MutableList<History> {

        if ("profile-is-null".equals(filter)) {
            log.debug("REST request to get all Historys where profile is null")
            return historyRepository.findAll()
                .asSequence()
                .filter { it.profile == null }
                .toMutableList()
        } else {
            log.debug("REST request to get all Histories")
            return historyRepository.findAll()
        }
    }

    /**
     * `GET  /histories/:id` : get the "id" history.
     *
     * @param id the id of the history to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the history, or with status `404 (Not Found)`.
     */
    @GetMapping("/histories/{id}")
    fun getHistory(@PathVariable id: Long): ResponseEntity<History> {
        log.debug("REST request to get History : $id")
        val history = historyRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(history)
    }
    /**
     *  `DELETE  /histories/:id` : delete the "id" history.
     *
     * @param id the id of the history to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/histories/{id}")
    fun deleteHistory(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete History : $id")

        historyRepository.deleteById(id)
        historySearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/histories?query=:query` : search for the history corresponding
     * to the query.
     *
     * @param query the query of the history search.
     * @return the result of the search.
     */
    @GetMapping("/_search/histories")
    fun searchHistories(@RequestParam query: String): MutableList<History> {
        log.debug("REST request to search Histories for query $query")
        return historySearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
    }
}
