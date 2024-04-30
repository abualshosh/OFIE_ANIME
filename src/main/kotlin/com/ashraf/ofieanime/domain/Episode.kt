package com.ashraf.ofieanime.domain

import javax.persistence.*
import org.hibernate.annotations.Type
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.io.Serializable
import java.time.LocalDate


/**
 * A Episode.
 */
  
@Entity
@Table(name = "episode")
  
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "episode")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Episode(

    
              
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
        var id: Long? = null,
                  
  
    @Column(name = "title")
        var title: String? = null,
                  
  
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "episode_link")
        var episodeLink: String? = null,
                  
  
    @Column(name = "relase_date")
        var relaseDate: LocalDate? = null,
            
    // jhipster-needle-entity-add-field - JHipster will add fields here
) :  Serializable {

      
  
  
  
    @OneToMany(mappedBy = "episode")
  

        @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
                    
               
               @org.springframework.data.annotation.Transient
            @JsonIgnoreProperties(value = [
                "episode",
            ], allowSetters = true)
    var urlLinks: MutableSet<UrlLink>? = mutableSetOf()
      
  
  
  
    @ManyToOne
    @JsonIgnoreProperties(value = [
        "profile",
        "episodes",
      ], allowSetters = true)
    var history: History? = null
      
  
  
  
    @ManyToOne
    @JsonIgnoreProperties(value = [
        "episodes",
        "anime",
        "yearlySeason",
        "comments",
      ], allowSetters = true)
    var season: Season? = null
      
  
  
  
    @OneToMany(mappedBy = "episode")
  

              @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
                    
                              
               @org.springframework.data.annotation.Transient
            @JsonIgnoreProperties(value = [
                "episode",
                "anime",
                "season",
                "profile",
            ], allowSetters = true)
    var comments: MutableSet<Comment>? = mutableSetOf()
  
  
    
                fun addUrlLink(urlLink: UrlLink) : Episode {
        this.urlLinks?.add(urlLink)
        urlLink.episode = this
        return this;
    }
                fun removeUrlLink(urlLink: UrlLink) : Episode{
        this.urlLinks?.remove(urlLink)
        urlLink.episode = null
        return this;
    }
            fun history(history: History?): Episode {
        this.history = history
        return this
    }
        fun season(season: Season?): Episode {
        this.season = season
        return this
    }
                fun addComment(comment: Comment) : Episode {
        this.comments?.add(comment)
        comment.episode = this
        return this;
    }
                fun removeComment(comment: Comment) : Episode{
        this.comments?.remove(comment)
        comment.episode = null
        return this;
    }
    
        // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is Episode) return false
      return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Episode{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", episodeLink='" + episodeLink + "'" +
            ", relaseDate='" + relaseDate + "'" +
            "}";
    }

    companion object {
        private const val serialVersionUID = 1L
            }
}
