package fr.univrouen.poste.services;

public class StatBean {
	
	Long posteNumber;
	Long userNumber;
	Long adminNumber;
	Long supermanagerNumber;
	Long managerNumber;
	Long membreNumber;
	Long candidatNumber;
	Long userActifNumber;
	Long candidatActifNumber;
	Long posteCandidatureNumber;
	Long posteCandidatureActifNumber;
	Long posteCandidatureFileNumber;
	String totalFileSizeFormatted;
	String maxFileSize;
	Long nbPages;
	Double pagesKilo;
	Long nbRames;
	Long moyNbPages;
	Long moyPagesGr;
	Long memberReviewFileNumber;
	String totalMemberReviewFileSizeFormatted;
	Long posteAPourvoirFileNumber;
	String totalposteAPourvoirFileSizeFormatted;
	
	public StatBean(Long posteNumber, Long userNumber, Long adminNumber, Long supermanagerNumber, Long managerNumber,
			Long membreNumber, Long candidatNumber, Long userActifNumber, Long candidatActifNumber,
			Long posteCandidatureNumber, Long posteCandidatureActifNumber, Long posteCandidatureFileNumber,
			String totalFileSizeFormatted, String maxFileSize, Long nbPages, Double pagesKilo, Long nbRames,
			Long moyNbPages, Long moyPagesGr, Long memberReviewFileNumber, String totalMemberReviewFileSizeFormatted, 
			Long posteAPourvoirFileNumber, String totalposteAPourvoirFileSizeFormatted) {
		super();
		this.posteNumber = posteNumber;
		this.userNumber = userNumber;
		this.adminNumber = adminNumber;
		this.supermanagerNumber = supermanagerNumber;
		this.managerNumber = managerNumber;
		this.membreNumber = membreNumber;
		this.candidatNumber = candidatNumber;
		this.userActifNumber = userActifNumber;
		this.candidatActifNumber = candidatActifNumber;
		this.posteCandidatureNumber = posteCandidatureNumber;
		this.posteCandidatureActifNumber = posteCandidatureActifNumber;
		this.posteCandidatureFileNumber = posteCandidatureFileNumber;
		this.totalFileSizeFormatted = totalFileSizeFormatted;
		this.maxFileSize = maxFileSize;
		this.nbPages = nbPages;
		this.pagesKilo = pagesKilo;
		this.nbRames = nbRames;
		this.moyNbPages = moyNbPages;
		this.moyPagesGr = moyPagesGr;
		this.memberReviewFileNumber = memberReviewFileNumber;
		this.totalMemberReviewFileSizeFormatted = totalMemberReviewFileSizeFormatted;
		this.posteAPourvoirFileNumber = posteAPourvoirFileNumber;
		this.totalposteAPourvoirFileSizeFormatted = totalposteAPourvoirFileSizeFormatted;
	}
	
	public Long getPosteNumber() {
		return posteNumber;
	}
	public Long getUserNumber() {
		return userNumber;
	}
	public Long getAdminNumber() {
		return adminNumber;
	}
	public Long getSupermanagerNumber() {
		return supermanagerNumber;
	}
	public Long getManagerNumber() {
		return managerNumber;
	}
	public Long getMembreNumber() {
		return membreNumber;
	}
	public Long getCandidatNumber() {
		return candidatNumber;
	}
	public Long getUserActifNumber() {
		return userActifNumber;
	}
	public Long getCandidatActifNumber() {
		return candidatActifNumber;
	}
	public Long getPosteCandidatureNumber() {
		return posteCandidatureNumber;
	}
	public Long getPosteCandidatureActifNumber() {
		return posteCandidatureActifNumber;
	}
	public Long getPosteCandidatureFileNumber() {
		return posteCandidatureFileNumber;
	}
	public String getTotalFileSizeFormatted() {
		return totalFileSizeFormatted;
	}
	public String getMaxFileSize() {
		return maxFileSize;
	}
	public Long getNbPages() {
		return nbPages;
	}
	public Double getPagesKilo() {
		return pagesKilo;
	}
	public Long getNbRames() {
		return nbRames;
	}
	public Long getMoyNbPages() {
		return moyNbPages;
	}
	public Long getMoyPagesGr() {
		return moyPagesGr;
	}

	public Long getMemberReviewFileNumber() {
		return memberReviewFileNumber;
	}

	public String getTotalMemberReviewFileSizeFormatted() {
		return totalMemberReviewFileSizeFormatted;
	}

	public Long getPosteAPourvoirFileNumber() {
		return posteAPourvoirFileNumber;
	}

	public String getTotalposteAPourvoirFileSizeFormatted() {
		return totalposteAPourvoirFileSizeFormatted;
	}

	public String toText() {
		return "Nombre de postes : " + posteNumber + "\n\n"
				+ "Comptes créés : " + userNumber + "\n\n"
				+ "Compts activés : " + userActifNumber + "\n\n"
				+ "Admins : " + adminNumber + "\n\n"
				+ "Super Managers : " + supermanagerNumber + "\n\n"
				+ "Managers : " + managerNumber + "\n\n"
				+ "Membres : "+ membreNumber + "\n\n"
				+ "Candidats uniques : " + candidatNumber + "\n\n"
				+ "Cadnidats actifs : " + candidatActifNumber + "\n\n"
				+ "Nombre de candidatures : " + posteCandidatureNumber + "\n\n"
				+ "Nombre de candidatures actives (au moins une modification) : " + posteCandidatureActifNumber + "\n\n"
				+ "Fichiers de candidatures déposés : " + posteCandidatureFileNumber + "\n\n"
				+ "Volume total des candidatures : " + totalFileSizeFormatted + "\n\n"
				+ "Taille Max Fichier de candidature : " + maxFileSize + "\n\n"
				+ "Nbre total de pages (fichiers PDF uniquement et pris en compte seulement si le parsing s'est bien déroulé) : " + nbPages + " [~" + pagesKilo + " kg - ~" + nbRames + " rames]\n\n"
				+ "Nbre de pages en moyenne par candidature (fichiers PDF uniquement et pris en compte seulement si le parsing s'est bien déroulé) : " + "~" + moyNbPages + " [~" + moyPagesGr + " g]" + "\n\n"
				+ "Rapports de commission déposés : " + memberReviewFileNumber + "\n\n"
				+ "Volume total des rapports de commission : " + totalMemberReviewFileSizeFormatted + "\n\n" 
				+ "Fichiers internes des comités de sélection : " + posteAPourvoirFileNumber + "\n\n"
				+ "Volume total des fichiers internes des comités de sélection : " + totalposteAPourvoirFileSizeFormatted + "\n\n";
	}
	
	
	
}
