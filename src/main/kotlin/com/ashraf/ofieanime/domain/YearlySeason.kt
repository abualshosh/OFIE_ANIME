package com.ashraf.ofieanime.domain

import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.io.Serializable
import java.time.LocalDate


/**
 * A YearlySeason.
 */
  
@Entity
@Table(name = "yearly_season")
  
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "yearlyseason")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class YearlySeason(

    
              
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
        var id: Long? = null,
                  
  
    @Column(name = "name")
        var name: LocalDate? = null,
            
    // jhipster-needle-entity-add-field - JHipster will add fields here
) :  Serializable {

      
  
  
  
    @OneToMany(mappedBy = "yearlySeason")
  

        @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
          
               
               @org.springframework.data.annotation.Transient
            @JsonIgnoreProperties(value = [
                "episodes",
                "anime",
                "yearlySeason",
                "comments",
            ], allowSetters = true)
    var seasons: MutableSet<Season>? = mutableSetOf()
  
  
    
                fun addSeason(season: Season) : YearlySeason {
        this.seasons?.add(season)
        season.yearlySeason = this
        return this;
    }
                fun removeSeason(season: Season) : YearlySeason{
        this.seasons?.remove(season)
        season.yearlySeason = null
        return this;
    }
    
        // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is YearlySeason) return false
      return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "YearlySeason{" +
            "id=" + id +
            ", name='" + name + "'" +
            "}";
    }

    companion object {
        private const val serialVersionUID = 1L
            }
}
