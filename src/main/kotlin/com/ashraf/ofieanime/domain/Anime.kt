package com.ashraf.ofieanime.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Type
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

/**
 * A Anime.
 */

@Entity
@Table(name = "anime")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "anime")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Anime(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "title")
    var title: String? = null,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "discription")
    var discription: String? = null,

    @Column(name = "cover")
    var cover: String? = null,

    @Column(name = "relase_date")
    var relaseDate: LocalDate? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    @JsonIgnoreProperties(
        value = [
            "anime",
        ],
        allowSetters = true
    )
    @OneToOne
    @JoinColumn(unique = true)
    var source: Source? = null

    @OneToMany(mappedBy = "anime")

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(
        value = [
            "anime",
        ],
        allowSetters = true
    )
    var characters: MutableSet<Character>? = mutableSetOf()

    @OneToMany(mappedBy = "anime")

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(
        value = [
            "episodes",
            "anime",
            "yearlySeason",
            "comments",
        ],
        allowSetters = true
    )
    var seasons: MutableSet<Season>? = mutableSetOf()

    @OneToMany(mappedBy = "anime")

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(
        value = [
            "anime",
        ],
        allowSetters = true
    )
    var tags: MutableSet<Tag>? = mutableSetOf()

    @ManyToOne
    @JsonIgnoreProperties(
        value = [
            "anime",
        ],
        allowSetters = true
    )
    var studio: Studio? = null

    @ManyToOne
    @JsonIgnoreProperties(
        value = [
            "anime",
            "profile",
        ],
        allowSetters = true
    )
    var favirote: Favirote? = null

    @OneToMany(mappedBy = "anime")

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(
        value = [
            "episode",
            "anime",
            "season",
            "profile",
        ],
        allowSetters = true
    )
    var comments: MutableSet<Comment>? = mutableSetOf()

    fun source(source: Source?): Anime {
        this.source = source
        return this
    }
    fun addCharacter(character: Character): Anime {
        this.characters?.add(character)
        character.anime = this
        return this
    }
    fun removeCharacter(character: Character): Anime {
        this.characters?.remove(character)
        character.anime = null
        return this
    }
    fun addSeason(season: Season): Anime {
        this.seasons?.add(season)
        season.anime = this
        return this
    }
    fun removeSeason(season: Season): Anime {
        this.seasons?.remove(season)
        season.anime = null
        return this
    }
    fun addTag(tag: Tag): Anime {
        this.tags?.add(tag)
        tag.anime = this
        return this
    }
    fun removeTag(tag: Tag): Anime {
        this.tags?.remove(tag)
        tag.anime = null
        return this
    }
    fun studio(studio: Studio?): Anime {
        this.studio = studio
        return this
    }
    fun favirote(favirote: Favirote?): Anime {
        this.favirote = favirote
        return this
    }
    fun addComment(comment: Comment): Anime {
        this.comments?.add(comment)
        comment.anime = this
        return this
    }
    fun removeComment(comment: Comment): Anime {
        this.comments?.remove(comment)
        comment.anime = null
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Anime) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Anime{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", discription='" + discription + "'" +
            ", cover='" + cover + "'" +
            ", relaseDate='" + relaseDate + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
