package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.UrlLink
import com.ashraf.ofieanime.repository.UrlLinkRepository
import com.ashraf.ofieanime.repository.search.UrlLinkSearchRepository
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

private const val ENTITY_NAME = "urlLink"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.UrlLink].
 */
@RestController
@RequestMapping("/api")
@Transactional
class UrlLinkResource(
    private val urlLinkRepository: UrlLinkRepository,
    private val urlLinkSearchRepository: UrlLinkSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "urlLink"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /url-links` : Create a new urlLink.
     *
     * @param urlLink the urlLink to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new urlLink, or with status `400 (Bad Request)` if the urlLink has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/url-links")
    fun createUrlLink(@RequestBody urlLink: UrlLink): ResponseEntity<UrlLink> {
        log.debug("REST request to save UrlLink : $urlLink")
        if (urlLink.id != null) {
            throw BadRequestAlertException(
                "A new urlLink cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = urlLinkRepository.save(urlLink)
        urlLinkSearchRepository.index(result)
        return ResponseEntity.created(URI("/api/url-links/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /url-links/:id} : Updates an existing urlLink.
     *
     * @param id the id of the urlLink to save.
     * @param urlLink the urlLink to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated urlLink,
     * or with status `400 (Bad Request)` if the urlLink is not valid,
     * or with status `500 (Internal Server Error)` if the urlLink couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/url-links/{id}")
    fun updateUrlLink(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody urlLink: UrlLink
    ): ResponseEntity<UrlLink> {
        log.debug("REST request to update UrlLink : {}, {}", id, urlLink)
        if (urlLink.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, urlLink.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!urlLinkRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = urlLinkRepository.save(urlLink)
        urlLinkSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    urlLink.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /url-links/:id} : Partial updates given fields of an existing urlLink, field will ignore if it is null
     *
     * @param id the id of the urlLink to save.
     * @param urlLink the urlLink to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated urlLink,
     * or with status {@code 400 (Bad Request)} if the urlLink is not valid,
     * or with status {@code 404 (Not Found)} if the urlLink is not found,
     * or with status {@code 500 (Internal Server Error)} if the urlLink couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/url-links/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateUrlLink(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody urlLink: UrlLink
    ): ResponseEntity<UrlLink> {
        log.debug("REST request to partial update UrlLink partially : {}, {}", id, urlLink)
        if (urlLink.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, urlLink.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!urlLinkRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = urlLinkRepository.findById(urlLink.id)
            .map {

                if (urlLink.linkType != null) {
                    it.linkType = urlLink.linkType
                }

                it
            }
            .map { urlLinkRepository.save(it) }
            .map {
                urlLinkSearchRepository.save(it)

                it
            }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, urlLink.id.toString())
        )
    }

    /**
     * `GET  /url-links` : get all the urlLinks.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of urlLinks in body.
     */
    @GetMapping("/url-links") fun getAllUrlLinks(): MutableList<UrlLink> {

        log.debug("REST request to get all UrlLinks")
        return urlLinkRepository.findAll()
    }

    /**
     * `GET  /url-links/:id` : get the "id" urlLink.
     *
     * @param id the id of the urlLink to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the urlLink, or with status `404 (Not Found)`.
     */
    @GetMapping("/url-links/{id}")
    fun getUrlLink(@PathVariable id: Long): ResponseEntity<UrlLink> {
        log.debug("REST request to get UrlLink : $id")
        val urlLink = urlLinkRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(urlLink)
    }
    /**
     *  `DELETE  /url-links/:id` : delete the "id" urlLink.
     *
     * @param id the id of the urlLink to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/url-links/{id}")
    fun deleteUrlLink(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete UrlLink : $id")

        urlLinkRepository.deleteById(id)
        urlLinkSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/url-links?query=:query` : search for the urlLink corresponding
     * to the query.
     *
     * @param query the query of the urlLink search.
     * @return the result of the search.
     */
    @GetMapping("/_search/url-links")
    fun searchUrlLinks(@RequestParam query: String): MutableList<UrlLink> {
        log.debug("REST request to search UrlLinks for query $query")
        return urlLinkSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
    }
}
