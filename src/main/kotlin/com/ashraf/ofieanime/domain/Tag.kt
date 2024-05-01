package com.ashraf.ofieanime.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.*

/**
 * A Tag.
 */

@Entity
@Table(name = "tag")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "tag")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Tag(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "name")
    var name: String? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    @ManyToOne
    @JsonIgnoreProperties(
        value = [
            "source",
            "characters",
            "seasons",
            "tags",
            "studio",
            "favirote",
            "comments",
        ],
        allowSetters = true
    )
    var anime: Anime? = null

    fun anime(anime: Anime?): Tag {
        this.anime = anime
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Tag{" +
            "id=" + id +
            ", name='" + name + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
