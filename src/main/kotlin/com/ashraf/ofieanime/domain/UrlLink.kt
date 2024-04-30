package com.ashraf.ofieanime.domain

import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.io.Serializable

import com.ashraf.ofieanime.domain.enumeration.UrlLinkType


/**
 * A UrlLink.
 */
  
@Entity
@Table(name = "url_link")
  
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "urllink")
@SuppressWarnings("common-java:DuplicatedBlocks")
data class UrlLink(

    
              
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
        var id: Long? = null,
                  
  
    @Enumerated(EnumType.STRING)
    @Column(name = "link_type")
        var linkType: UrlLinkType? = null,
            
    // jhipster-needle-entity-add-field - JHipster will add fields here
) :  Serializable {

      
  
  
  
    @ManyToOne
    @JsonIgnoreProperties(value = [
        "urlLinks",
        "history",
        "season",
        "comments",
      ], allowSetters = true)
    var episode: Episode? = null
  
  
    
        fun episode(episode: Episode?): UrlLink {
        this.episode = episode
        return this
    }

        // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is UrlLink) return false
      return id != null && other.id != null && id == other.id
    }

    override fun toString(): String {
        return "UrlLink{" +
            "id=" + id +
            ", linkType='" + linkType + "'" +
            "}";
    }

    companion object {
        private const val serialVersionUID = 1L
            }
}
