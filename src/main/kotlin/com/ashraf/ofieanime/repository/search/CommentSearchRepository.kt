package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Comment
import com.ashraf.ofieanime.repository.CommentRepository

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
 * Spring Data Elasticsearch repository for the [Comment] entity.
 */
interface CommentSearchRepository :  ElasticsearchRepository<Comment, Long>, CommentSearchRepositoryInternal{}

interface CommentSearchRepositoryInternal {
  fun search(query: String): Stream<Comment> 

  fun search(query: Query): Stream<Comment> 

  fun index(entity: Comment)
}

class CommentSearchRepositoryInternalImpl(
  val elasticsearchTemplate: ElasticsearchRestTemplate, 
  val repository: CommentRepository
): CommentSearchRepositoryInternal {

  override fun search(query: String): Stream<Comment>  {
      val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
      return search(nativeSearchQuery)
  }

  override fun search(query: Query): Stream<Comment> {
      return elasticsearchTemplate
          .search(query, Comment::class.java)
          .map(SearchHit<Comment>::getContent)
          .stream()
  }

  override fun index(entity: Comment) {
    entity.id?.let { 
      repository.findById(it).ifPresent(elasticsearchTemplate::save)
    }
  }
}
