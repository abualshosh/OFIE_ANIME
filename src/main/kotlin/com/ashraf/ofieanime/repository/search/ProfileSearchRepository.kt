package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Profile
import com.ashraf.ofieanime.repository.ProfileRepository

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
 * Spring Data Elasticsearch repository for the [Profile] entity.
 */
interface ProfileSearchRepository :  ElasticsearchRepository<Profile, Long>, ProfileSearchRepositoryInternal{}

interface ProfileSearchRepositoryInternal {
  fun search(query: String): Stream<Profile> 

  fun search(query: Query): Stream<Profile> 

  fun index(entity: Profile)
}

class ProfileSearchRepositoryInternalImpl(
  val elasticsearchTemplate: ElasticsearchRestTemplate, 
  val repository: ProfileRepository
): ProfileSearchRepositoryInternal {

  override fun search(query: String): Stream<Profile>  {
      val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
      return search(nativeSearchQuery)
  }

  override fun search(query: Query): Stream<Profile> {
      return elasticsearchTemplate
          .search(query, Profile::class.java)
          .map(SearchHit<Profile>::getContent)
          .stream()
  }

  override fun index(entity: Profile) {
    entity.id?.let { 
      repository.findById(it).ifPresent(elasticsearchTemplate::save)
    }
  }
}
