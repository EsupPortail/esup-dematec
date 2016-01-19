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
	
	public StatBean(Long posteNumber, Long userNumber, Long adminNumber, Long supermanagerNumber, Long managerNumber,
			Long membreNumber, Long candidatNumber, Long userActifNumber, Long candidatActifNumber,
			Long posteCandidatureNumber, Long posteCandidatureActifNumber, Long posteCandidatureFileNumber,
			String totalFileSizeFormatted, String maxFileSize, Long nbPages, Double pagesKilo, Long nbRames,
			Long moyNbPages, Long moyPagesGr) {
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
	
}
