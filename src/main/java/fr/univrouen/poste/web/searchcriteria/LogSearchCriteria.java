package fr.univrouen.poste.web.searchcriteria;


public class LogSearchCriteria {

	String status = "";
	
	String userId = "";
	
	String nom = "";
	
	String action = "";

	String email = "";

	String message = "";

	String numEmploi = "";

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

	public String getAction() {
        return this.action;
    }

	public void setAction(String action) {
        this.action = action;
    }

	public String getEmail() {
        return this.email;
    }

	public void setEmail(String email) {
        this.email = email;
    }

	public String getMessage() {
        return this.message;
    }

	public void setMessage(String message) {
        this.message = message;
    }

	public String getNumEmploi() {
        return this.numEmploi;
    }

	public void setNumEmploi(String numEmploi) {
        this.numEmploi = numEmploi;
    }
}
