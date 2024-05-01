package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.History
import com.ashraf.ofieanime.repository.HistoryRepository
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.stream.Stream

/**
 * Spring Data Elasticsearch repository for the [History] entity.
 */
interface HistorySearchRepository : ElasticsearchRepository<History, Long>, HistorySearchRepositoryInternal

interface HistorySearchRepositoryInternal {
    fun search(query: String): Stream<History> fun search(query: Query): Stream<History> fun index(entity: History)
}

class HistorySearchRepositoryInternalImpl(
    val elasticsearchTemplate: ElasticsearchRestTemplate,
    val repository: HistoryRepository
) : HistorySearchRepositoryInternal {

    override fun search(query: String): Stream<History> {
        val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
        return search(nativeSearchQuery)
    }

    override fun search(query: Query): Stream<History> {
        return elasticsearchTemplate
            .search(query, History::class.java)
            .map(SearchHit<History>::getContent)
            .stream()
    }

    override fun index(entity: History) {
        entity.id?.let {
            repository.findById(it).ifPresent(elasticsearchTemplate::save)
        }
    }
}
