package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Character
import com.ashraf.ofieanime.repository.CharacterRepository
import com.ashraf.ofieanime.repository.search.CharacterSearchRepository
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

private const val ENTITY_NAME = "character"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Character].
 */
@RestController
@RequestMapping("/api")
@Transactional
class CharacterResource(
        private val characterRepository: CharacterRepository,
        private val characterSearchRepository: CharacterSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "character"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /characters` : Create a new character.
     *
     * @param character the character to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new character, or with status `400 (Bad Request)` if the character has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/characters")
    fun createCharacter(@RequestBody character: Character): ResponseEntity<Character> {
        log.debug("REST request to save Character : $character")
        if (character.id != null) {
            throw BadRequestAlertException(
                "A new character cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = characterRepository.save(character)
        characterSearchRepository.index(result)
            return ResponseEntity.created(URI("/api/characters/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /characters/:id} : Updates an existing character.
     *
     * @param id the id of the character to save.
     * @param character the character to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated character,
     * or with status `400 (Bad Request)` if the character is not valid,
     * or with status `500 (Internal Server Error)` if the character couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/characters/{id}")
    fun updateCharacter(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody character: Character
    ): ResponseEntity<Character> {
        log.debug("REST request to update Character : {}, {}", id, character)
        if (character.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, character.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }


        if (!characterRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = characterRepository.save(character)
        characterSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     character.id.toString()
                )
            )
            .body(result)
    }

    /**
    * {@code PATCH  /characters/:id} : Partial updates given fields of an existing character, field will ignore if it is null
    *
    * @param id the id of the character to save.
    * @param character the character to update.
    * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated character,
    * or with status {@code 400 (Bad Request)} if the character is not valid,
    * or with status {@code 404 (Not Found)} if the character is not found,
    * or with status {@code 500 (Internal Server Error)} if the character couldn't be updated.
    * @throws URISyntaxException if the Location URI syntax is incorrect.
    */
    @PatchMapping(value = ["/characters/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateCharacter(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody character:Character
    ): ResponseEntity<Character> {
        log.debug("REST request to partial update Character partially : {}, {}", id, character)
        if (character.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, character.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!characterRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }



         val result = characterRepository.findById(character.id)
            .map {

                  if (character.name!= null) {
                     it.name = character.name
                  }
                  if (character.picture!= null) {
                     it.picture = character.picture
                  }

               it
            }
            .map { characterRepository.save(it) }
            .map {
                  characterSearchRepository.save(it)

                  it

            }


        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, character.id.toString())
        )
    }

    /**
     * `GET  /characters` : get all the characters.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of characters in body.
     */
    @GetMapping("/characters")    
    fun getAllCharacters(): MutableList<Character> {
        
        

            log.debug("REST request to get all Characters")
                        return characterRepository.findAll()
    }

    /**
     * `GET  /characters/:id` : get the "id" character.
     *
     * @param id the id of the character to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the character, or with status `404 (Not Found)`.
     */
    @GetMapping("/characters/{id}")
    fun getCharacter(@PathVariable id: Long): ResponseEntity<Character> {
        log.debug("REST request to get Character : $id")
        val character = characterRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(character)
    }
    /**
     *  `DELETE  /characters/:id` : delete the "id" character.
     *
     * @param id the id of the character to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/characters/{id}")
    fun deleteCharacter(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Character : $id")

        characterRepository.deleteById(id)
        characterSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/characters?query=:query` : search for the character corresponding
     * to the query.
     *
     * @param query the query of the character search.
     * @return the result of the search.
     */
    @GetMapping("/_search/characters")
    fun searchCharacters(@RequestParam query: String): MutableList<Character> {
        log.debug("REST request to search Characters for query $query")
            return characterSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
}
}
