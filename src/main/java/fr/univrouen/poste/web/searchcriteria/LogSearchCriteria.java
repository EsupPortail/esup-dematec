package fr.univrouen.poste.web.searchcriteria;


public class LogSearchCriteria {

	String status = "";
	
	String userId = "";
	
	String nom = "";
	

	public String getStatus() {
        return this.status;
    }

	public void setStatus(String status) {
        this.status = status;
    }

	public String getUserId() {
        return this.userId;
    }

	public void setUserId(String userId) {
        this.userId = userId;
    }

	public String getNom() {
        return this.nom;
    }

	public void setNom(String nom) {
        this.nom = nom;
    }
}
