package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Source
import com.ashraf.ofieanime.repository.SourceRepository
import com.ashraf.ofieanime.repository.search.SourceSearchRepository
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

private const val ENTITY_NAME = "source"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Source].
 */
@RestController
@RequestMapping("/api")
@Transactional
class SourceResource(
    private val sourceRepository: SourceRepository,
    private val sourceSearchRepository: SourceSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "source"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /sources` : Create a new source.
     *
     * @param source the source to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new source, or with status `400 (Bad Request)` if the source has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/sources")
    fun createSource(@RequestBody source: Source): ResponseEntity<Source> {
        log.debug("REST request to save Source : $source")
        if (source.id != null) {
            throw BadRequestAlertException(
                "A new source cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = sourceRepository.save(source)
        sourceSearchRepository.index(result)
        return ResponseEntity.created(URI("/api/sources/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /sources/:id} : Updates an existing source.
     *
     * @param id the id of the source to save.
     * @param source the source to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated source,
     * or with status `400 (Bad Request)` if the source is not valid,
     * or with status `500 (Internal Server Error)` if the source couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/sources/{id}")
    fun updateSource(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody source: Source
    ): ResponseEntity<Source> {
        log.debug("REST request to update Source : {}, {}", id, source)
        if (source.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, source.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!sourceRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = sourceRepository.save(source)
        sourceSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    source.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /sources/:id} : Partial updates given fields of an existing source, field will ignore if it is null
     *
     * @param id the id of the source to save.
     * @param source the source to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated source,
     * or with status {@code 400 (Bad Request)} if the source is not valid,
     * or with status {@code 404 (Not Found)} if the source is not found,
     * or with status {@code 500 (Internal Server Error)} if the source couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/sources/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateSource(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody source: Source
    ): ResponseEntity<Source> {
        log.debug("REST request to partial update Source partially : {}, {}", id, source)
        if (source.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, source.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!sourceRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = sourceRepository.findById(source.id)
            .map {

                if (source.name != null) {
                    it.name = source.name
                }

                it
            }
            .map { sourceRepository.save(it) }
            .map {
                sourceSearchRepository.save(it)

                it
            }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, source.id.toString())
        )
    }

    /**
     * `GET  /sources` : get all the sources.
     *

     * @param filter the filter of the request.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of sources in body.
     */
    @GetMapping("/sources") fun getAllSources(@RequestParam(required = false) filter: String?): MutableList<Source> {

        if ("anime-is-null".equals(filter)) {
            log.debug("REST request to get all Sources where anime is null")
            return sourceRepository.findAll()
                .asSequence()
                .filter { it.anime == null }
                .toMutableList()
        } else {
            log.debug("REST request to get all Sources")
            return sourceRepository.findAll()
        }
    }

    /**
     * `GET  /sources/:id` : get the "id" source.
     *
     * @param id the id of the source to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the source, or with status `404 (Not Found)`.
     */
    @GetMapping("/sources/{id}")
    fun getSource(@PathVariable id: Long): ResponseEntity<Source> {
        log.debug("REST request to get Source : $id")
        val source = sourceRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(source)
    }
    /**
     *  `DELETE  /sources/:id` : delete the "id" source.
     *
     * @param id the id of the source to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/sources/{id}")
    fun deleteSource(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Source : $id")

        sourceRepository.deleteById(id)
        sourceSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/sources?query=:query` : search for the source corresponding
     * to the query.
     *
     * @param query the query of the source search.
     * @return the result of the search.
     */
    @GetMapping("/_search/sources")
    fun searchSources(@RequestParam query: String): MutableList<Source> {
        log.debug("REST request to search Sources for query $query")
        return sourceSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
    }
}
