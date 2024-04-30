package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Studio
import com.ashraf.ofieanime.repository.StudioRepository

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
 * Spring Data Elasticsearch repository for the [Studio] entity.
 */
interface StudioSearchRepository :  ElasticsearchRepository<Studio, Long>, StudioSearchRepositoryInternal{}

interface StudioSearchRepositoryInternal {
  fun search(query: String): Stream<Studio> 

  fun search(query: Query): Stream<Studio> 

  fun index(entity: Studio)
}

class StudioSearchRepositoryInternalImpl(
  val elasticsearchTemplate: ElasticsearchRestTemplate, 
  val repository: StudioRepository
): StudioSearchRepositoryInternal {

  override fun search(query: String): Stream<Studio>  {
      val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
      return search(nativeSearchQuery)
  }

  override fun search(query: Query): Stream<Studio> {
      return elasticsearchTemplate
          .search(query, Studio::class.java)
          .map(SearchHit<Studio>::getContent)
          .stream()
  }

  override fun index(entity: Studio) {
    entity.id?.let { 
      repository.findById(it).ifPresent(elasticsearchTemplate::save)
    }
  }
}
