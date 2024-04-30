package com.ashraf.ofieanime.web.rest

import com.ashraf.ofieanime.repository.search.UserSearchRepository
import com.ashraf.ofieanime.service.UserService
import com.ashraf.ofieanime.service.dto.UserDTO
import org.elasticsearch.index.query.QueryBuilders.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import tech.jhipster.web.util.PaginationUtil
import java.util.*

@RestController
@RequestMapping("/api")
class PublicUserResource(
    private val userSearchRepository: UserSearchRepository,
    private val userService: UserService
) {
    companion object { private val ALLOWED_ORDERED_PROPERTIES = arrayOf("id", "login", "firstName", "lastName", "email", "activated", "langKey")
    }

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * {@code GET /users} : get all users with only the public informations - calling this are allowed for anyone.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    fun getAllPublicUsers(@org.springdoc.api.annotations.ParameterObject pageable: Pageable): ResponseEntity<List<UserDTO>> {
        log.debug("REST request to get all public User names")
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build()
        }

        val page = userService.getAllPublicUsers(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }
    private fun onlyContainsAllowedProperties(pageable: Pageable) =
        pageable.sort.map(Sort.Order::getProperty).all(ALLOWED_ORDERED_PROPERTIES::contains)

    /**
     * Gets a list of all roles.
     * @return a string list of all roles.
     */
    @GetMapping("/authorities")
    fun getAuthorities() = userService.getAuthorities()

    /**
     * {@code SEARCH /_search/users/:query} : search for the User corresponding to the query.
     *
     * @param query the query to search.
     * @return the result of the search.
     */
    @GetMapping("/_search/users/{query}")
    fun search(@PathVariable query: String): List<UserDTO> {

        return userSearchRepository.search(query).map { UserDTO(it) }
    }
}
