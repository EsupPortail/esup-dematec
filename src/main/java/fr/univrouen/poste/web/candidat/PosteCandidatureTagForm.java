package fr.univrouen.poste.web.candidat;

import java.util.Map;

import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;

public class PosteCandidatureTagForm {
	
	 private Map<PosteCandidatureTag, PosteCandidatureTagValue> tags;

	public Map<PosteCandidatureTag, PosteCandidatureTagValue> getTags() {
		return tags;
	}

	public void setTags(Map<PosteCandidatureTag, PosteCandidatureTagValue> tags) {
		this.tags = tags;
	}

}
