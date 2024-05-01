package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.UrlLink
import com.ashraf.ofieanime.repository.UrlLinkRepository
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.stream.Stream

/**
 * Spring Data Elasticsearch repository for the [UrlLink] entity.
 */
interface UrlLinkSearchRepository : ElasticsearchRepository<UrlLink, Long>, UrlLinkSearchRepositoryInternal

interface UrlLinkSearchRepositoryInternal {
    fun search(query: String): Stream<UrlLink> fun search(query: Query): Stream<UrlLink> fun index(entity: UrlLink)
}

class UrlLinkSearchRepositoryInternalImpl(
    val elasticsearchTemplate: ElasticsearchRestTemplate,
    val repository: UrlLinkRepository
) : UrlLinkSearchRepositoryInternal {

    override fun search(query: String): Stream<UrlLink> {
        val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
        return search(nativeSearchQuery)
    }

    override fun search(query: Query): Stream<UrlLink> {
        return elasticsearchTemplate
            .search(query, UrlLink::class.java)
            .map(SearchHit<UrlLink>::getContent)
            .stream()
    }

    override fun index(entity: UrlLink) {
        entity.id?.let {
            repository.findById(it).ifPresent(elasticsearchTemplate::save)
        }
    }
}
