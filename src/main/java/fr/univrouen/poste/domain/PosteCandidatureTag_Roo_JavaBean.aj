// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import java.util.Set;

privileged aspect PosteCandidatureTag_Roo_JavaBean {
    
    public String PosteCandidatureTag.getName() {
        return this.name;
    }
    
    public void PosteCandidatureTag.setName(String name) {
        this.name = name;
    }
    
    public Set<PosteCandidatureTagValue> PosteCandidatureTag.getValues() {
        return this.values;
    }
    
    public void PosteCandidatureTag.setValues(Set<PosteCandidatureTagValue> values) {
        this.values = values;
    }
    
}