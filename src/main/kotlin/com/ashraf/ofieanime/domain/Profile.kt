package com.ashraf.ofieanime.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.*

/**
 * A Profile.
 */

@Entity
@Table(name = "profile")

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "profile")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Profile(

    @Id
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "pictue")
    var pictue: String? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    @OneToOne

    @MapsId
    @JoinColumn(name = "id")
    var user: User? = null

    @JsonIgnoreProperties(
        value = [
            "anime",
            "profile",
        ],
        allowSetters = true
    )
    @OneToOne
    @JoinColumn(unique = true)
    var favirote: Favirote? = null

    @JsonIgnoreProperties(
        value = [
            "episode",
            "anime",
            "season",
            "profile",
        ],
        allowSetters = true
    )
    @OneToOne
    @JoinColumn(unique = true)
    var comment: Comment? = null

    @JsonIgnoreProperties(
        value = [
            "profile",
            "episodes",
        ],
        allowSetters = true
    )
    @OneToOne
    @JoinColumn(unique = true)
    var history: History? = null

    fun user(user: User?): Profile {
        this.user = user
        return this
    }
    fun favirote(favirote: Favirote?): Profile {
        this.favirote = favirote
        return this
    }
    fun comment(comment: Comment?): Profile {
        this.comment = comment
        return this
    }
    fun history(history: History?): Profile {
        this.history = history
        return this
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Profile) return false
        return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Profile{" +
            "id=" + id +
            ", pictue='" + pictue + "'" +
            "}"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
