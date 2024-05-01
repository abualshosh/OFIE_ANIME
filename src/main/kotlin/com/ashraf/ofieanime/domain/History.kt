package com.ashraf.ofieanime.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

/**
 * A History.
 */

@Entity
@Table(name = "history")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "history")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class History(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "date")
    var date: LocalDate? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    @JsonIgnoreProperties(
        value = [
            "user",
            "favirote",
            "comment",
            "history",
        ],
        allowSetters = true
    )
    @OneToOne(mappedBy = "history")

    @org.springframework.data.annotation.Transient
    var profile: Profile? = null

    @OneToMany(mappedBy = "history")

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(
        value = [
            "urlLinks",
            "history",
            "season",
            "comments",
        ],
        allowSetters = true
    )
    var episodes: MutableSet<Episode>? = mutableSetOf()

    fun profile(profile: Profile?): History {
        this.profile = profile
        return this
    }
    fun addEpisode(episode: Episode): History {
        this.episodes?.add(episode)
        episode.history = this
        return this
    }
    fun removeEpisode(episode: Episode): History {
        this.episodes?.remove(episode)
        episode.history = null
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is History) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "History{" +
            "id=" + id +
            ", date='" + date + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
