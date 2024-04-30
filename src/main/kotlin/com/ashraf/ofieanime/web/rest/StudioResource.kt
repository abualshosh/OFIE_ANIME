package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Studio
import com.ashraf.ofieanime.repository.StudioRepository
import com.ashraf.ofieanime.repository.search.StudioSearchRepository
import com.ashraf.ofieanime.web.rest.errors.BadRequestAlertException

import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.ResponseUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

import java.net.URI
import java.net.URISyntaxException
import java.util.Objects
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery

private const val ENTITY_NAME = "studio"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Studio].
 */
@RestController
@RequestMapping("/api")
@Transactional
class StudioResource(
        private val studioRepository: StudioRepository,
        private val studioSearchRepository: StudioSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "studio"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /studios` : Create a new studio.
     *
     * @param studio the studio to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new studio, or with status `400 (Bad Request)` if the studio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/studios")
    fun createStudio(@RequestBody studio: Studio): ResponseEntity<Studio> {
        log.debug("REST request to save Studio : $studio")
        if (studio.id != null) {
            throw BadRequestAlertException(
                "A new studio cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = studioRepository.save(studio)
        studioSearchRepository.index(result)
            return ResponseEntity.created(URI("/api/studios/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /studios/:id} : Updates an existing studio.
     *
     * @param id the id of the studio to save.
     * @param studio the studio to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated studio,
     * or with status `400 (Bad Request)` if the studio is not valid,
     * or with status `500 (Internal Server Error)` if the studio couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/studios/{id}")
    fun updateStudio(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody studio: Studio
    ): ResponseEntity<Studio> {
        log.debug("REST request to update Studio : {}, {}", id, studio)
        if (studio.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, studio.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }


        if (!studioRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = studioRepository.save(studio)
        studioSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     studio.id.toString()
                )
            )
            .body(result)
    }

    /**
    * {@code PATCH  /studios/:id} : Partial updates given fields of an existing studio, field will ignore if it is null
    *
    * @param id the id of the studio to save.
    * @param studio the studio to update.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated studio,
    * or with status {@code 400 (Bad Request)} if the studio is not valid,
    * or with status {@code 404 (Not Found)} if the studio is not found,
    * or with status {@code 500 (Internal Server Error)} if the studio couldn't be updated.
    * @throws URISyntaxException if the Location URI syntax is incorrect.
    */
    @PatchMapping(value = ["/studios/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateStudio(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody studio:Studio
    ): ResponseEntity<Studio> {
        log.debug("REST request to partial update Studio partially : {}, {}", id, studio)
        if (studio.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, studio.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!studioRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }



         val result = studioRepository.findById(studio.id)
            .map {

                  if (studio.name!= null) {
                     it.name = studio.name
                  }

               it
            }
            .map { studioRepository.save(it) }
            .map {
                  studioSearchRepository.save(it)

                  it

            }


        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, studio.id.toString())
        )
    }

    /**
     * `GET  /studios` : get all the studios.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of studios in body.
     */
    @GetMapping("/studios")    
    fun getAllStudios(): MutableList<Studio> {
        
        

            log.debug("REST request to get all Studios")
                        return studioRepository.findAll()
    }

    /**
     * `GET  /studios/:id` : get the "id" studio.
     *
     * @param id the id of the studio to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the studio, or with status `404 (Not Found)`.
     */
    @GetMapping("/studios/{id}")
    fun getStudio(@PathVariable id: Long): ResponseEntity<Studio> {
        log.debug("REST request to get Studio : $id")
        val studio = studioRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(studio)
    }
    /**
     *  `DELETE  /studios/:id` : delete the "id" studio.
     *
     * @param id the id of the studio to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/studios/{id}")
    fun deleteStudio(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Studio : $id")

        studioRepository.deleteById(id)
        studioSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/studios?query=:query` : search for the studio corresponding
     * to the query.
     *
     * @param query the query of the studio search.
     * @return the result of the search.
     */
    @GetMapping("/_search/studios")
    fun searchStudios(@RequestParam query: String): MutableList<Studio> {
        log.debug("REST request to search Studios for query $query")
            return studioSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
}
}
