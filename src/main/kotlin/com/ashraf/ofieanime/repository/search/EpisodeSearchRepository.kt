package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Episode
import com.ashraf.ofieanime.repository.EpisodeRepository

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
 * Spring Data Elasticsearch repository for the [Episode] entity.
 */
interface EpisodeSearchRepository :  ElasticsearchRepository<Episode, Long>, EpisodeSearchRepositoryInternal{}

interface EpisodeSearchRepositoryInternal {
  fun search(query: String): Stream<Episode> 

  fun search(query: Query): Stream<Episode> 

  fun index(entity: Episode)
}

class EpisodeSearchRepositoryInternalImpl(
  val elasticsearchTemplate: ElasticsearchRestTemplate, 
  val repository: EpisodeRepository
): EpisodeSearchRepositoryInternal {

  override fun search(query: String): Stream<Episode>  {
      val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
      return search(nativeSearchQuery)
  }

  override fun search(query: Query): Stream<Episode> {
      return elasticsearchTemplate
          .search(query, Episode::class.java)
          .map(SearchHit<Episode>::getContent)
          .stream()
  }

  override fun index(entity: Episode) {
    entity.id?.let { 
      repository.findById(it).ifPresent(elasticsearchTemplate::save)
    }
  }
}
