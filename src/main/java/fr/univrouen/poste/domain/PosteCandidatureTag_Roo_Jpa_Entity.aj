// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.PosteCandidatureTag;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

privileged aspect PosteCandidatureTag_Roo_Jpa_Entity {
    
    declare @type: PosteCandidatureTag: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long PosteCandidatureTag.id;
    
    @Version
    @Column(name = "version")
    private Integer PosteCandidatureTag.version;
    
    public Long PosteCandidatureTag.getId() {
        return this.id;
    }
    
    public void PosteCandidatureTag.setId(Long id) {
        this.id = id;
    }
    
    public Integer PosteCandidatureTag.getVersion() {
        return this.version;
    }
    
    public void PosteCandidatureTag.setVersion(Integer version) {
        this.version = version;
    }
    
}
