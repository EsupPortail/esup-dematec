package fr.univrouen.poste.domain;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;

@RooJavaBean
@RooJpaActiveRecord
public class GalaxieMapping {

    private String id_numemploi;
    
    private String id_numCandidat;
    
    private String id_civilite;
    
    private String id_nom;
    
    private String id_prenom;
    
    private String id_email;
    
    private String id_localisation;
    
    private String id_profil;
    
    private String id_etat_dossier;
    

    private static String cache_id_numemploi;
    
    private static String cache_id_numCandidat;
    
    private static String cache_id_civilite;
    
    private static String cache_id_nom;
    
    private static String cache_id_prenom;
    
    private static String cache_id_email;
    
    private static String cache_id_localisation;
    
    private static String cache_id_profil;
    
    private static String cache_id_etat_dossier;

    
	public void setId_numemploi(String id_numemploi) {
		this.id_numemploi = id_numemploi;
		this.cache_id_numemploi = id_numemploi;
	}

	public void setId_numCandidat(String id_numCandidat) {
		this.id_numCandidat = id_numCandidat;
		this.cache_id_numCandidat = id_numCandidat;
	}

	public void setId_civilite(String id_civilite) {
		this.id_civilite = id_civilite;
		this.cache_id_civilite = id_civilite;
	}

	public void setId_nom(String id_nom) {
		this.id_nom = id_nom;
		this.cache_id_nom = id_nom;
	}

	public void setId_prenom(String id_prenom) {
		this.id_prenom = id_prenom;
		this.cache_id_prenom = id_prenom;
	}

	public void setId_email(String id_email) {
		this.id_email = id_email;
		this.cache_id_email = id_email;
	}

	public void setId_localisation(String id_localisation) {
		this.id_localisation = id_localisation;
		this.cache_id_localisation = id_localisation;
	}

	public void setId_profil(String id_profil) {
		this.id_profil = id_profil;
		this.cache_id_profil = id_profil;
	}

	public void setId_etat_dossier(String id_etat_dossier) {
		this.id_etat_dossier = id_etat_dossier;
		this.cache_id_etat_dossier = id_etat_dossier;
	}

	
	public static String getCache_id_numemploi() {
		if(cache_id_numemploi == null) {
			cache_id_numemploi = GalaxieMapping.findAllGalaxieMappings().get(0).getId_numemploi();
		}
		return cache_id_numemploi;
	}

	public static String getCache_id_numCandidat() {
		if(cache_id_numCandidat == null) {
			cache_id_numCandidat = GalaxieMapping.findAllGalaxieMappings().get(0).getId_numCandidat();
		}
		return cache_id_numCandidat;
	}

	public static String getCache_id_civilite() {
		if(cache_id_civilite == null) {
			cache_id_civilite = GalaxieMapping.findAllGalaxieMappings().get(0).getId_civilite();
		}
		return cache_id_civilite;
	}

	public static String getCache_id_nom() {
		if(cache_id_nom == null) {
			cache_id_nom = GalaxieMapping.findAllGalaxieMappings().get(0).getId_nom();
		}
		return cache_id_nom;
	}

	public static String getCache_id_prenom() {
		if(cache_id_prenom == null) {
			cache_id_prenom = GalaxieMapping.findAllGalaxieMappings().get(0).getId_prenom();
		}
		return cache_id_prenom;
	}

	public static String getCache_id_email() {
		if(cache_id_email == null) {
			cache_id_email = GalaxieMapping.findAllGalaxieMappings().get(0).getId_email();
		}
		return cache_id_email;
	}

	public static String getCache_id_localisation() {
		if(cache_id_localisation == null) {
			cache_id_localisation = GalaxieMapping.findAllGalaxieMappings().get(0).getId_localisation();
		}
		return cache_id_localisation;
	}

	public static String getCache_id_profil() {
		if(cache_id_profil == null) {
			cache_id_profil = GalaxieMapping.findAllGalaxieMappings().get(0).getId_profil();
		}
		return cache_id_profil;
	}

	public static String getCache_id_etat_dossier() {
		if(cache_id_etat_dossier == null) {
			cache_id_etat_dossier = GalaxieMapping.findAllGalaxieMappings().get(0).getId_etat_dossier();
		}
		return cache_id_etat_dossier;
	}

    
}
