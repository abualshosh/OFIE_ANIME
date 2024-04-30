package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Season
import com.ashraf.ofieanime.repository.SeasonRepository
import com.ashraf.ofieanime.repository.search.SeasonSearchRepository
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

private const val ENTITY_NAME = "season"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Season].
 */
@RestController
@RequestMapping("/api")
@Transactional
class SeasonResource(
        private val seasonRepository: SeasonRepository,
        private val seasonSearchRepository: SeasonSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "season"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /seasons` : Create a new season.
     *
     * @param season the season to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new season, or with status `400 (Bad Request)` if the season has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/seasons")
    fun createSeason(@RequestBody season: Season): ResponseEntity<Season> {
        log.debug("REST request to save Season : $season")
        if (season.id != null) {
            throw BadRequestAlertException(
                "A new season cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = seasonRepository.save(season)
        seasonSearchRepository.index(result)
            return ResponseEntity.created(URI("/api/seasons/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /seasons/:id} : Updates an existing season.
     *
     * @param id the id of the season to save.
     * @param season the season to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated season,
     * or with status `400 (Bad Request)` if the season is not valid,
     * or with status `500 (Internal Server Error)` if the season couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/seasons/{id}")
    fun updateSeason(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody season: Season
    ): ResponseEntity<Season> {
        log.debug("REST request to update Season : {}, {}", id, season)
        if (season.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, season.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }


        if (!seasonRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = seasonRepository.save(season)
        seasonSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     season.id.toString()
                )
            )
            .body(result)
    }

    /**
    * {@code PATCH  /seasons/:id} : Partial updates given fields of an existing season, field will ignore if it is null
    *
    * @param id the id of the season to save.
    * @param season the season to update.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated season,
    * or with status {@code 400 (Bad Request)} if the season is not valid,
    * or with status {@code 404 (Not Found)} if the season is not found,
    * or with status {@code 500 (Internal Server Error)} if the season couldn't be updated.
    * @throws URISyntaxException if the Location URI syntax is incorrect.
    */
    @PatchMapping(value = ["/seasons/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateSeason(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody season:Season
    ): ResponseEntity<Season> {
        log.debug("REST request to partial update Season partially : {}, {}", id, season)
        if (season.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, season.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!seasonRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }



         val result = seasonRepository.findById(season.id)
            .map {

                  if (season.titleInJapan!= null) {
                     it.titleInJapan = season.titleInJapan
                  }
                  if (season.titleInEnglis!= null) {
                     it.titleInEnglis = season.titleInEnglis
                  }
                  if (season.relaseDate!= null) {
                     it.relaseDate = season.relaseDate
                  }
                  if (season.addDate!= null) {
                     it.addDate = season.addDate
                  }
                  if (season.startDate!= null) {
                     it.startDate = season.startDate
                  }
                  if (season.endDate!= null) {
                     it.endDate = season.endDate
                  }
                  if (season.avrgeEpisodeLength!= null) {
                     it.avrgeEpisodeLength = season.avrgeEpisodeLength
                  }
                  if (season.type!= null) {
                     it.type = season.type
                  }
                  if (season.seasonType!= null) {
                     it.seasonType = season.seasonType
                  }
                  if (season.cover!= null) {
                     it.cover = season.cover
                  }

               it
            }
            .map { seasonRepository.save(it) }
            .map {
                  seasonSearchRepository.save(it)

                  it

            }


        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, season.id.toString())
        )
    }

    /**
     * `GET  /seasons` : get all the seasons.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of seasons in body.
     */
    @GetMapping("/seasons")    
    fun getAllSeasons(): MutableList<Season> {
        
        

            log.debug("REST request to get all Seasons")
                        return seasonRepository.findAll()
    }

    /**
     * `GET  /seasons/:id` : get the "id" season.
     *
     * @param id the id of the season to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the season, or with status `404 (Not Found)`.
     */
    @GetMapping("/seasons/{id}")
    fun getSeason(@PathVariable id: Long): ResponseEntity<Season> {
        log.debug("REST request to get Season : $id")
        val season = seasonRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(season)
    }
    /**
     *  `DELETE  /seasons/:id` : delete the "id" season.
     *
     * @param id the id of the season to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/seasons/{id}")
    fun deleteSeason(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Season : $id")

        seasonRepository.deleteById(id)
        seasonSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/seasons?query=:query` : search for the season corresponding
     * to the query.
     *
     * @param query the query of the season search.
     * @return the result of the search.
     */
    @GetMapping("/_search/seasons")
    fun searchSeasons(@RequestParam query: String): MutableList<Season> {
        log.debug("REST request to search Seasons for query $query")
            return seasonSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
}
}
