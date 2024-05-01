package com.ashraf.ofieanime.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Type
import java.io.Serializable
import javax.persistence.*

/**
 * A Comment.
 */

@Entity
@Table(name = "comment")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "comment")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Comment(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "comment")
    var comment: String? = null,

    @Column(name = "jhi_like")
    var like: Int? = null,

    @Column(name = "dis_like")
    var disLike: Int? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    @ManyToOne
    @JsonIgnoreProperties(
        value = [
            "urlLinks",
            "history",
            "season",
            "comments",
        ],
        allowSetters = true
    )
    var episode: Episode? = null

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

    @ManyToOne
    @JsonIgnoreProperties(
        value = [
            "episodes",
            "anime",
            "yearlySeason",
            "comments",
        ],
        allowSetters = true
    )
    var season: Season? = null

    @JsonIgnoreProperties(
        value = [
            "user",
            "favirote",
            "comment",
            "history",
        ],
        allowSetters = true
    )
    @OneToOne(mappedBy = "comment")

    @org.springframework.data.annotation.Transient
    var profile: Profile? = null

    fun episode(episode: Episode?): Comment {
        this.episode = episode
        return this
    }
    fun anime(anime: Anime?): Comment {
        this.anime = anime
        return this
    }
    fun season(season: Season?): Comment {
        this.season = season
        return this
    }
    fun profile(profile: Profile?): Comment {
        this.profile = profile
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Comment) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Comment{" +
            "id=" + id +
            ", comment='" + comment + "'" +
            ", like=" + like +
            ", disLike=" + disLike +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
