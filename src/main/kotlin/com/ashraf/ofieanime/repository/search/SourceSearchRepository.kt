package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Source
import com.ashraf.ofieanime.repository.SourceRepository
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.stream.Stream

/**
 * Spring Data Elasticsearch repository for the [Source] entity.
 */
interface SourceSearchRepository : ElasticsearchRepository<Source, Long>, SourceSearchRepositoryInternal

interface SourceSearchRepositoryInternal {
    fun search(query: String): Stream<Source> fun search(query: Query): Stream<Source> fun index(entity: Source)
}

class SourceSearchRepositoryInternalImpl(
    val elasticsearchTemplate: ElasticsearchRestTemplate,
    val repository: SourceRepository
) : SourceSearchRepositoryInternal {

    override fun search(query: String): Stream<Source> {
        val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
        return search(nativeSearchQuery)
    }

    override fun search(query: Query): Stream<Source> {
        return elasticsearchTemplate
            .search(query, Source::class.java)
            .map(SearchHit<Source>::getContent)
            .stream()
    }

    override fun index(entity: Source) {
        entity.id?.let {
            repository.findById(it).ifPresent(elasticsearchTemplate::save)
        }
    }
}
