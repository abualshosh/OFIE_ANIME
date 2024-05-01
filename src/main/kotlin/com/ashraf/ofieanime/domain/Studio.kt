package com.ashraf.ofieanime.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.*

/**
 * A Studio.
 */

@Entity
@Table(name = "studio")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "studio")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Studio(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "name")
    var name: String? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    @OneToMany(mappedBy = "studio")

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

    @org.springframework.data.annotation.Transient
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
    var anime: MutableSet<Anime>? = mutableSetOf()

    fun addAnime(anime: Anime): Studio {
        this.anime?.add(anime)
        anime.studio = this
        return this
    }
    fun removeAnime(anime: Anime): Studio {
        this.anime?.remove(anime)
        anime.studio = null
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Studio) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Studio{" +
            "id=" + id +
            ", name='" + name + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
