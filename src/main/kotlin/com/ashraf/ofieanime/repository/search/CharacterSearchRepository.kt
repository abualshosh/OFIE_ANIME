package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Character
import com.ashraf.ofieanime.repository.CharacterRepository

import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream


import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
     
/**
 * Spring Data Elasticsearch repository for the [Character] entity.
 */
interface CharacterSearchRepository :  ElasticsearchRepository<Character, Long>, CharacterSearchRepositoryInternal{}

interface CharacterSearchRepositoryInternal {
  fun search(query: String): Stream<Character> 

  fun search(query: Query): Stream<Character> 

  fun index(entity: Character)
}

class CharacterSearchRepositoryInternalImpl(
  val elasticsearchTemplate: ElasticsearchRestTemplate, 
  val repository: CharacterRepository
): CharacterSearchRepositoryInternal {

  override fun search(query: String): Stream<Character>  {
      val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
      return search(nativeSearchQuery)
  }

  override fun search(query: Query): Stream<Character> {
      return elasticsearchTemplate
          .search(query, Character::class.java)
          .map(SearchHit<Character>::getContent)
          .stream()
  }

  override fun index(entity: Character) {
    entity.id?.let { 
      repository.findById(it).ifPresent(elasticsearchTemplate::save)
    }
  }
}
