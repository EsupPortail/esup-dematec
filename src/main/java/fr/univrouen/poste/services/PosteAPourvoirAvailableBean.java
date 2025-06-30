package fr.univrouen.poste.services;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import fr.univrouen.poste.domain.PosteAPourvoir;

public class PosteAPourvoirAvailableBean {
	
	PosteAPourvoir poste;
	
	Boolean candidat = false;
	
	Boolean canBeUnsubscribed = false;
	

	public PosteAPourvoir getPoste() {
        return this.poste;
    }

	public void setPoste(PosteAPourvoir poste) {
        this.poste = poste;
    }

	public Boolean getCandidat() {
        return this.candidat;
    }

	public void setCandidat(Boolean candidat) {
        this.candidat = candidat;
    }

	public Boolean getCanBeUnsubscribed() {
        return this.canBeUnsubscribed;
    }

	public void setCanBeUnsubscribed(Boolean canBeUnsubscribed) {
        this.canBeUnsubscribed = canBeUnsubscribed;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
