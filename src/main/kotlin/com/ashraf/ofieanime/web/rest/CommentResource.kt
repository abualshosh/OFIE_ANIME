package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.domain.Comment
import com.ashraf.ofieanime.repository.CommentRepository
import com.ashraf.ofieanime.repository.search.CommentSearchRepository
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

private const val ENTITY_NAME = "comment"
/**
 * REST controller for managing [com.ashraf.ofieanime.domain.Comment].
 */
@RestController
@RequestMapping("/api")
@Transactional
class CommentResource(
    private val commentRepository: CommentRepository,
    private val commentSearchRepository: CommentSearchRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "comment"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /comments` : Create a new comment.
     *
     * @param comment the comment to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new comment, or with status `400 (Bad Request)` if the comment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/comments")
    fun createComment(@RequestBody comment: Comment): ResponseEntity<Comment> {
        log.debug("REST request to save Comment : $comment")
        if (comment.id != null) {
            throw BadRequestAlertException(
                "A new comment cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = commentRepository.save(comment)
        commentSearchRepository.index(result)
        return ResponseEntity.created(URI("/api/comments/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /comments/:id} : Updates an existing comment.
     *
     * @param id the id of the comment to save.
     * @param comment the comment to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated comment,
     * or with status `400 (Bad Request)` if the comment is not valid,
     * or with status `500 (Internal Server Error)` if the comment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/comments/{id}")
    fun updateComment(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody comment: Comment
    ): ResponseEntity<Comment> {
        log.debug("REST request to update Comment : {}, {}", id, comment)
        if (comment.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, comment.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!commentRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = commentRepository.save(comment)
        commentSearchRepository.index(result)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                    comment.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /comments/:id} : Partial updates given fields of an existing comment, field will ignore if it is null
     *
     * @param id the id of the comment to save.
     * @param comment the comment to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated comment,
     * or with status {@code 400 (Bad Request)} if the comment is not valid,
     * or with status {@code 404 (Not Found)} if the comment is not found,
     * or with status {@code 500 (Internal Server Error)} if the comment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/comments/{id}"], consumes = ["application/json", "application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateComment(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody comment: Comment
    ): ResponseEntity<Comment> {
        log.debug("REST request to partial update Comment partially : {}, {}", id, comment)
        if (comment.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, comment.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!commentRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = commentRepository.findById(comment.id)
            .map {

                if (comment.comment != null) {
                    it.comment = comment.comment
                }
                if (comment.like != null) {
                    it.like = comment.like
                }
                if (comment.disLike != null) {
                    it.disLike = comment.disLike
                }

                it
            }
            .map { commentRepository.save(it) }
            .map {
                commentSearchRepository.save(it)

                it
            }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, comment.id.toString())
        )
    }

    /**
     * `GET  /comments` : get all the comments.
     *

     * @param filter the filter of the request.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of comments in body.
     */
    @GetMapping("/comments") fun getAllComments(@RequestParam(required = false) filter: String?): MutableList<Comment> {

        if ("profile-is-null".equals(filter)) {
            log.debug("REST request to get all Comments where profile is null")
            return commentRepository.findAll()
                .asSequence()
                .filter { it.profile == null }
                .toMutableList()
        } else {
            log.debug("REST request to get all Comments")
            return commentRepository.findAll()
        }
    }

    /**
     * `GET  /comments/:id` : get the "id" comment.
     *
     * @param id the id of the comment to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the comment, or with status `404 (Not Found)`.
     */
    @GetMapping("/comments/{id}")
    fun getComment(@PathVariable id: Long): ResponseEntity<Comment> {
        log.debug("REST request to get Comment : $id")
        val comment = commentRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(comment)
    }
    /**
     *  `DELETE  /comments/:id` : delete the "id" comment.
     *
     * @param id the id of the comment to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/comments/{id}")
    fun deleteComment(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Comment : $id")

        commentRepository.deleteById(id)
        commentSearchRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }

    /**
     * `SEARCH  /_search/comments?query=:query` : search for the comment corresponding
     * to the query.
     *
     * @param query the query of the comment search.
     * @return the result of the search.
     */
    @GetMapping("/_search/comments")
    fun searchComments(@RequestParam query: String): MutableList<Comment> {
        log.debug("REST request to search Comments for query $query")
        return commentSearchRepository.search(query)
            .collect(Collectors.toList())
            .toMutableList()
    }
}
