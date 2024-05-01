package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Favirote
import com.ashraf.ofieanime.repository.FaviroteRepository
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.stream.Stream

/**
 * Spring Data Elasticsearch repository for the [Favirote] entity.
 */
interface FaviroteSearchRepository : ElasticsearchRepository<Favirote, Long>, FaviroteSearchRepositoryInternal

interface FaviroteSearchRepositoryInternal {
    fun search(query: String): Stream<Favirote> fun search(query: Query): Stream<Favirote> fun index(entity: Favirote)
}

class FaviroteSearchRepositoryInternalImpl(
    val elasticsearchTemplate: ElasticsearchRestTemplate,
    val repository: FaviroteRepository
) : FaviroteSearchRepositoryInternal {

    override fun search(query: String): Stream<Favirote> {
        val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
        return search(nativeSearchQuery)
    }

    override fun search(query: Query): Stream<Favirote> {
        return elasticsearchTemplate
            .search(query, Favirote::class.java)
            .map(SearchHit<Favirote>::getContent)
            .stream()
    }

    override fun index(entity: Favirote) {
        entity.id?.let {
            repository.findById(it).ifPresent(elasticsearchTemplate::save)
        }
    }
}
