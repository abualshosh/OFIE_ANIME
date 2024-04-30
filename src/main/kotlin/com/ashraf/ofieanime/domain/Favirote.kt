package com.ashraf.ofieanime.domain

import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.io.Serializable
import java.time.LocalDate


/**
 * A Favirote.
 */
  
@Entity
@Table(name = "favirote")
  
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "favirote")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class Favirote(

    
              
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
        var id: Long? = null,
                  
  
    @Column(name = "add_date")
        var addDate: LocalDate? = null,
            
    // jhipster-needle-entity-add-field - JHipster will add fields here
) :  Serializable {

      
  
  
  
    @OneToMany(mappedBy = "favirote")
  

        @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
          
               
               @org.springframework.data.annotation.Transient
            @JsonIgnoreProperties(value = [
                "source",
                "characters",
                "seasons",
                "tags",
                "studio",
                "favirote",
                "comments",
            ], allowSetters = true)
    var anime: MutableSet<Anime>? = mutableSetOf()
      
  
  
  
    @JsonIgnoreProperties(value = [
        "user",
        "favirote",
        "comment",
        "history",
    ], allowSetters = true)
    @OneToOne(mappedBy = "favirote")
          
                    
               @org.springframework.data.annotation.Transient
    var profile: Profile? = null
  
  
    
                fun addAnime(anime: Anime) : Favirote {
        this.anime?.add(anime)
        anime.favirote = this
        return this;
    }
                fun removeAnime(anime: Anime) : Favirote{
        this.anime?.remove(anime)
        anime.favirote = null
        return this;
    }
            fun profile(profile: Profile?): Favirote {
        this.profile = profile
        return this
    }

        // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is Favirote) return false
      return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "Favirote{" +
            "id=" + id +
            ", addDate='" + addDate + "'" +
            "}";
    }

    companion object {
        private const val serialVersionUID = 1L
            }
}
