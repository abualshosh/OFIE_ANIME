package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Anime
import com.ashraf.ofieanime.repository.AnimeRepository
import com.ashraf.ofieanime.repository.search.AnimeSearchRepository
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

private const val ENTITY_NAME = "anime"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Anime].
 */
@RestController
@RequestMapping("/api")
@Transactional
class AnimeResource(
        private val animeRepository: AnimeRepository,
        private val animeSearchRepository: AnimeSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "anime"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /anime` : Create a new anime.
     *
     * @param anime the anime to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new anime, or with status `400 (Bad Request)` if the anime has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/anime")
    fun createAnime(@RequestBody anime: Anime): ResponseEntity<Anime> {
        log.debug("REST request to save Anime : $anime")
        if (anime.id != null) {
            throw BadRequestAlertException(
                "A new anime cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = animeRepository.save(anime)
        animeSearchRepository.index(result)
            return ResponseEntity.created(URI("/api/anime/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /anime/:id} : Updates an existing anime.
     *
     * @param id the id of the anime to save.
     * @param anime the anime to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated anime,
     * or with status `400 (Bad Request)` if the anime is not valid,
     * or with status `500 (Internal Server Error)` if the anime couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/anime/{id}")
    fun updateAnime(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody anime: Anime
    ): ResponseEntity<Anime> {
        log.debug("REST request to update Anime : {}, {}", id, anime)
        if (anime.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, anime.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }


        if (!animeRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = animeRepository.save(anime)
        animeSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     anime.id.toString()
                )
            )
            .body(result)
    }

    /**
    * {@code PATCH  /anime/:id} : Partial updates given fields of an existing anime, field will ignore if it is null
    *
    * @param id the id of the anime to save.
    * @param anime the anime to update.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated anime,
    * or with status {@code 400 (Bad Request)} if the anime is not valid,
    * or with status {@code 404 (Not Found)} if the anime is not found,
    * or with status {@code 500 (Internal Server Error)} if the anime couldn't be updated.
    * @throws URISyntaxException if the Location URI syntax is incorrect.
    */
    @PatchMapping(value = ["/anime/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateAnime(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody anime:Anime
    ): ResponseEntity<Anime> {
        log.debug("REST request to partial update Anime partially : {}, {}", id, anime)
        if (anime.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, anime.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!animeRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }



         val result = animeRepository.findById(anime.id)
            .map {

                  if (anime.title!= null) {
                     it.title = anime.title
                  }
                  if (anime.discription!= null) {
                     it.discription = anime.discription
                  }
                  if (anime.cover!= null) {
                     it.cover = anime.cover
                  }
                  if (anime.relaseDate!= null) {
                     it.relaseDate = anime.relaseDate
                  }

               it
            }
            .map { animeRepository.save(it) }
            .map {
                  animeSearchRepository.save(it)

                  it

            }


        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, anime.id.toString())
        )
    }

    /**
     * `GET  /anime` : get all the anime.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of anime in body.
     */
    @GetMapping("/anime")    
    fun getAllAnime(): MutableList<Anime> {
        
        

            log.debug("REST request to get all Anime")
                        return animeRepository.findAll()
    }

    /**
     * `GET  /anime/:id` : get the "id" anime.
     *
     * @param id the id of the anime to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the anime, or with status `404 (Not Found)`.
     */
    @GetMapping("/anime/{id}")
    fun getAnime(@PathVariable id: Long): ResponseEntity<Anime> {
        log.debug("REST request to get Anime : $id")
        val anime = animeRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(anime)
    }
    /**
     *  `DELETE  /anime/:id` : delete the "id" anime.
     *
     * @param id the id of the anime to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/anime/{id}")
    fun deleteAnime(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Anime : $id")

        animeRepository.deleteById(id)
        animeSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/anime?query=:query` : search for the anime corresponding
     * to the query.
     *
     * @param query the query of the anime search.
     * @return the result of the search.
     */
    @GetMapping("/_search/anime")
    fun searchAnime(@RequestParam query: String): MutableList<Anime> {
        log.debug("REST request to search Anime for query $query")
            return animeSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
}
}
