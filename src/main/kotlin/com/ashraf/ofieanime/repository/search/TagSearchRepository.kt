package com.ashraf.ofieanime.repository.search

import com.ashraf.ofieanime.domain.Tag
import com.ashraf.ofieanime.repository.TagRepository
import org.elasticsearch.index.query.QueryBuilders.queryStringQuery
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.stream.Stream

/**
 * Spring Data Elasticsearch repository for the [Tag] entity.
 */
interface TagSearchRepository : ElasticsearchRepository<Tag, Long>, TagSearchRepositoryInternal

interface TagSearchRepositoryInternal {
    fun search(query: String): Stream<Tag> fun search(query: Query): Stream<Tag> fun index(entity: Tag)
}

class TagSearchRepositoryInternalImpl(
    val elasticsearchTemplate: ElasticsearchRestTemplate,
    val repository: TagRepository
) : TagSearchRepositoryInternal {

    override fun search(query: String): Stream<Tag> {
        val nativeSearchQuery = NativeSearchQuery(queryStringQuery(query))
        return search(nativeSearchQuery)
    }

    override fun search(query: Query): Stream<Tag> {
        return elasticsearchTemplate
            .search(query, Tag::class.java)
            .map(SearchHit<Tag>::getContent)
            .stream()
    }

    override fun index(entity: Tag) {
        entity.id?.let {
            repository.findById(it).ifPresent(elasticsearchTemplate::save)
        }
    }
}
