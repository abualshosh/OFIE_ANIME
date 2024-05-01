package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Anime
import com.ashraf.ofieanime.repository.AnimeRepository
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.stream.Stream

/**
 * Spring Data Elasticsearch repository for the [Anime] entity.
 */
interface AnimeSearchRepository : ElasticsearchRepository<Anime, Long>, AnimeSearchRepositoryInternal

interface AnimeSearchRepositoryInternal {
    fun search(query: String): Stream<Anime> fun search(query: Query): Stream<Anime> fun index(entity: Anime)
}

class AnimeSearchRepositoryInternalImpl(
    val elasticsearchTemplate: ElasticsearchRestTemplate,
    val repository: AnimeRepository
) : AnimeSearchRepositoryInternal {

    override fun search(query: String): Stream<Anime> {
        val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
        return search(nativeSearchQuery)
    }

    override fun search(query: Query): Stream<Anime> {
        return elasticsearchTemplate
            .search(query, Anime::class.java)
            .map(SearchHit<Anime>::getContent)
            .stream()
    }

    override fun index(entity: Anime) {
        entity.id?.let {
            repository.findById(it).ifPresent(elasticsearchTemplate::save)
        }
    }
}
