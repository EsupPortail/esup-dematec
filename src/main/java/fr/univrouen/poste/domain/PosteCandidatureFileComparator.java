package fr.univrouen.poste.domain;

import java.util.Comparator;

public class PosteCandidatureFileComparator implements Comparator<PosteCandidatureFile> {

	@Override
	public int compare(PosteCandidatureFile o1, PosteCandidatureFile o2) {
		int cp = o1.getFileType().getListIndex().compareTo(o2.getFileType().getListIndex());
		if(cp==0) {
			cp  = o2.getSendTime().compareTo(o1.getSendTime());
		}
		if(cp==0) {
			cp  = o1.getId().compareTo(o2.getId());
		}
		return cp;
	}

}
