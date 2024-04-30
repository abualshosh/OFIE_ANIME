package com.ashraf.ofieanime.domain

import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.io.Serializable
import java.time.LocalDate

import com.ashraf.ofieanime.domain.enumeration.Type

import com.ashraf.ofieanime.domain.enumeration.SeasonType


/**
 * A Season.
 */
  
@Entity
@Table(name = "season")
  
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "season")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Season(

    
              
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
        var id: Long? = null,
                  
  
    @Column(name = "title_in_japan")
        var titleInJapan: String? = null,
                  
  
    @Column(name = "title_in_englis")
        var titleInEnglis: String? = null,
                  
  
    @Column(name = "relase_date")
        var relaseDate: LocalDate? = null,
                  
  
    @Column(name = "add_date")
        var addDate: LocalDate? = null,
                  
  
    @Column(name = "start_date")
        var startDate: LocalDate? = null,
                  
  
    @Column(name = "end_date")
        var endDate: LocalDate? = null,
                  
  
    @Column(name = "avrge_episode_length")
        var avrgeEpisodeLength: String? = null,
                  
  
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
        var type: Type? = null,
                  
  
    @Enumerated(EnumType.STRING)
    @Column(name = "season_type")
        var seasonType: SeasonType? = null,
                  
  
    @Column(name = "cover")
        var cover: String? = null,
            
    // jhipster-needle-entity-add-field - JHipster will add fields here
) :  Serializable {

      
  
  
  
    @OneToMany(mappedBy = "season")
  

        @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
                                                       
               
               @org.springframework.data.annotation.Transient
            @JsonIgnoreProperties(value = [
                "urlLinks",
                "history",
                "season",
                "comments",
            ], allowSetters = true)
    var episodes: MutableSet<Episode>? = mutableSetOf()
      
  
  
  
    @ManyToOne
    @JsonIgnoreProperties(value = [
        "source",
        "characters",
        "seasons",
        "tags",
        "studio",
        "favirote",
        "comments",
      ], allowSetters = true)
    var anime: Anime? = null
      
  
  
  
    @ManyToOne
    @JsonIgnoreProperties(value = [
        "seasons",
      ], allowSetters = true)
    var yearlySeason: YearlySeason? = null
      
  
  
  
    @OneToMany(mappedBy = "season")
  

              @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
                                                       
                              
               @org.springframework.data.annotation.Transient
            @JsonIgnoreProperties(value = [
                "episode",
                "anime",
                "season",
                "profile",
            ], allowSetters = true)
    var comments: MutableSet<Comment>? = mutableSetOf()
  
  
    
                fun addEpisode(episode: Episode) : Season {
        this.episodes?.add(episode)
        episode.season = this
        return this;
    }
                fun removeEpisode(episode: Episode) : Season{
        this.episodes?.remove(episode)
        episode.season = null
        return this;
    }
            fun anime(anime: Anime?): Season {
        this.anime = anime
        return this
    }
        fun yearlySeason(yearlySeason: YearlySeason?): Season {
        this.yearlySeason = yearlySeason
        return this
    }
                fun addComment(comment: Comment) : Season {
        this.comments?.add(comment)
        comment.season = this
        return this;
    }
                fun removeComment(comment: Comment) : Season{
        this.comments?.remove(comment)
        comment.season = null
        return this;
    }
    
        // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is Season) return false
      return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Season{" +
            "id=" + id +
            ", titleInJapan='" + titleInJapan + "'" +
            ", titleInEnglis='" + titleInEnglis + "'" +
            ", relaseDate='" + relaseDate + "'" +
            ", addDate='" + addDate + "'" +
            ", startDate='" + startDate + "'" +
            ", endDate='" + endDate + "'" +
            ", avrgeEpisodeLength='" + avrgeEpisodeLength + "'" +
            ", type='" + type + "'" +
            ", seasonType='" + seasonType + "'" +
            ", cover='" + cover + "'" +
            "}";
    }

    companion object {
        private const val serialVersionUID = 1L
            }
}
