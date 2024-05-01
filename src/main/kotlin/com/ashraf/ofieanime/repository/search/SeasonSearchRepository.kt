package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Season
import com.ashraf.ofieanime.repository.SeasonRepository
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.stream.Stream

/**
 * Spring Data Elasticsearch repository for the [Season] entity.
 */
interface SeasonSearchRepository : ElasticsearchRepository<Season, Long>, SeasonSearchRepositoryInternal

interface SeasonSearchRepositoryInternal {
    fun search(query: String): Stream<Season> fun search(query: Query): Stream<Season> fun index(entity: Season)
}

class SeasonSearchRepositoryInternalImpl(
    val elasticsearchTemplate: ElasticsearchRestTemplate,
    val repository: SeasonRepository
) : SeasonSearchRepositoryInternal {

    override fun search(query: String): Stream<Season> {
        val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
        return search(nativeSearchQuery)
    }

    override fun search(query: Query): Stream<Season> {
        return elasticsearchTemplate
            .search(query, Season::class.java)
            .map(SearchHit<Season>::getContent)
            .stream()
    }

    override fun index(entity: Season) {
        entity.id?.let {
            repository.findById(it).ifPresent(elasticsearchTemplate::save)
        }
    }
}
