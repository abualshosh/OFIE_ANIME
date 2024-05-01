package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Episode
import com.ashraf.ofieanime.repository.EpisodeRepository
import com.ashraf.ofieanime.repository.search.EpisodeSearchRepository
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

private const val ENTITY_NAME = "episode"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Episode].
 */
@RestController
@RequestMapping("/api")
@Transactional
class EpisodeResource(
    private val episodeRepository: EpisodeRepository,
    private val episodeSearchRepository: EpisodeSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "episode"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /episodes` : Create a new episode.
     *
     * @param episode the episode to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new episode, or with status `400 (Bad Request)` if the episode has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/episodes")
    fun createEpisode(@RequestBody episode: Episode): ResponseEntity<Episode> {
        log.debug("REST request to save Episode : $episode")
        if (episode.id != null) {
            throw BadRequestAlertException(
                "A new episode cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = episodeRepository.save(episode)
        episodeSearchRepository.index(result)
        return ResponseEntity.created(URI("/api/episodes/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /episodes/:id} : Updates an existing episode.
     *
     * @param id the id of the episode to save.
     * @param episode the episode to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated episode,
     * or with status `400 (Bad Request)` if the episode is not valid,
     * or with status `500 (Internal Server Error)` if the episode couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/episodes/{id}")
    fun updateEpisode(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody episode: Episode
    ): ResponseEntity<Episode> {
        log.debug("REST request to update Episode : {}, {}", id, episode)
        if (episode.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, episode.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!episodeRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = episodeRepository.save(episode)
        episodeSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    episode.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /episodes/:id} : Partial updates given fields of an existing episode, field will ignore if it is null
     *
     * @param id the id of the episode to save.
     * @param episode the episode to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated episode,
     * or with status {@code 400 (Bad Request)} if the episode is not valid,
     * or with status {@code 404 (Not Found)} if the episode is not found,
     * or with status {@code 500 (Internal Server Error)} if the episode couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/episodes/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateEpisode(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody episode: Episode
    ): ResponseEntity<Episode> {
        log.debug("REST request to partial update Episode partially : {}, {}", id, episode)
        if (episode.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, episode.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!episodeRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = episodeRepository.findById(episode.id)
            .map {

                if (episode.title != null) {
                    it.title = episode.title
                }
                if (episode.episodeLink != null) {
                    it.episodeLink = episode.episodeLink
                }
                if (episode.relaseDate != null) {
                    it.relaseDate = episode.relaseDate
                }

                it
            }
            .map { episodeRepository.save(it) }
            .map {
                episodeSearchRepository.save(it)

                it
            }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, episode.id.toString())
        )
    }

    /**
     * `GET  /episodes` : get all the episodes.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of episodes in body.
     */
    @GetMapping("/episodes") fun getAllEpisodes(): MutableList<Episode> {

        log.debug("REST request to get all Episodes")
        return episodeRepository.findAll()
    }

    /**
     * `GET  /episodes/:id` : get the "id" episode.
     *
     * @param id the id of the episode to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the episode, or with status `404 (Not Found)`.
     */
    @GetMapping("/episodes/{id}")
    fun getEpisode(@PathVariable id: Long): ResponseEntity<Episode> {
        log.debug("REST request to get Episode : $id")
        val episode = episodeRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(episode)
    }
    /**
     *  `DELETE  /episodes/:id` : delete the "id" episode.
     *
     * @param id the id of the episode to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/episodes/{id}")
    fun deleteEpisode(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Episode : $id")

        episodeRepository.deleteById(id)
        episodeSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/episodes?query=:query` : search for the episode corresponding
     * to the query.
     *
     * @param query the query of the episode search.
     * @return the result of the search.
     */
    @GetMapping("/_search/episodes")
    fun searchEpisodes(@RequestParam query: String): MutableList<Episode> {
        log.debug("REST request to search Episodes for query $query")
        return episodeSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
    }
}
