package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.YearlySeason
import com.ashraf.ofieanime.repository.YearlySeasonRepository
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.stream.Stream

/**
 * Spring Data Elasticsearch repository for the [YearlySeason] entity.
 */
interface YearlySeasonSearchRepository : ElasticsearchRepository<YearlySeason, Long>, YearlySeasonSearchRepositoryInternal

interface YearlySeasonSearchRepositoryInternal {
    fun search(query: String): Stream<YearlySeason> fun search(query: Query): Stream<YearlySeason> fun index(entity: YearlySeason)
}

class YearlySeasonSearchRepositoryInternalImpl(
    val elasticsearchTemplate: ElasticsearchRestTemplate,
    val repository: YearlySeasonRepository
) : YearlySeasonSearchRepositoryInternal {

    override fun search(query: String): Stream<YearlySeason> {
        val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
        return search(nativeSearchQuery)
    }

    override fun search(query: Query): Stream<YearlySeason> {
        return elasticsearchTemplate
            .search(query, YearlySeason::class.java)
            .map(SearchHit<YearlySeason>::getContent)
            .stream()
    }

    override fun index(entity: YearlySeason) {
        entity.id?.let {
            repository.findById(it).ifPresent(elasticsearchTemplate::save)
        }
    }
}
