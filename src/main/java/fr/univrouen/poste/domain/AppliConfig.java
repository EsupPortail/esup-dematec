/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class AppliConfig {

	private static String cacheTitre;
	
	private static String cacheImageUrl;
	
	private static String cachePiedPage;

	private static String cacheMailFrom;

	private static String cacheMailSubject;
	
	private static String cacheTexteMailActivation;
	
	private static String cacheMailSubjectMembre;
	
	private static String cacheTexteMailActivationMembre;

	private static String cacheTexteMailPasswordOublie;

	private static String cacheTextePremierePageAnonyme;

	private static String cacheTexteMembreAideCandidatures;

	private static String cacheTextePremierePageCandidat;

	private static String cacheTextePremierePageMembre;

	private static String cacheTexteCandidatAideCandidatures;

	private static String cacheTexteCandidatAideCandidatureDepot;
		
	private static Date cacheDateEndCandidat;
	
	private static Date cacheDateEndCandidatActif;

	private static Date cacheDateEndMembre;
	
	
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateEndCandidat;
    
	
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateEndCandidatActif;


    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateEndMembre;

    
    //General
    @Column(columnDefinition="TEXT")
    private String titre;

    @Column(columnDefinition="TEXT")
    private String imageUrl;
    
    @Column(columnDefinition="TEXT")
    private String piedPage;
    
    @Column(columnDefinition="TEXT")
    private String mailFrom;

    @Column(columnDefinition="TEXT")
    private String mailSubject;

    @Column(columnDefinition="TEXT")
    private String texteMailActivation;
    
    @Column(columnDefinition="TEXT")
    private String texteMailPasswordOublie;
    
    
    // Anonyme
    
    @Column(columnDefinition="TEXT")
    private String textePremierePageAnonyme;


    // Membre
    
    @Column(columnDefinition="TEXT")
    private String texteMembreAideCandidatures;
    
    @Column(columnDefinition="TEXT")
    private String mailSubjectMembre;

    @Column(columnDefinition="TEXT")
    private String texteMailActivationMembre;
    
    
    // Candidat
    
    @Column(columnDefinition="TEXT")
    private String textePremierePageCandidat;

    @Column(columnDefinition="TEXT")
    private String textePremierePageMembre;
    
    @Column(columnDefinition="TEXT")
    private String texteCandidatAideCandidatures;
    
    @Column(columnDefinition="TEXT")
    private String texteCandidatAideCandidatureDepot;

    
    
    public void setTitre(String titre) {
    	this.titre = titre;
    	cacheTitre = titre;
    }
 
    public void setImageUrl(String imageUrl) {
    	this.imageUrl = imageUrl;
    	cacheImageUrl = imageUrl;
    }

	public void setPiedPage(String piedPage) {
    	this.piedPage = piedPage;
    	cachePiedPage = piedPage;
    }
    
    public void setMailFrom(String mailFrom) {
    	this.mailFrom = mailFrom;
    	cacheMailFrom = mailFrom;
    }

	public void setMailSubject(String mailSubject) {
    	this.mailSubject = mailSubject;
    	cacheMailSubject = mailSubject;
    }

	public void setTexteMailActivation(String texteMailActivation) {
    	this.texteMailActivation = texteMailActivation;
    	cacheTexteMailActivation = texteMailActivation;
    }


	public void setMailSubjectMembre(String mailSubjectMembre) {
    	this.mailSubjectMembre = mailSubjectMembre;
    	cacheMailSubjectMembre = mailSubjectMembre;
    }

	public void setTexteMailActivationMembre(String texteMailActivationMembre) {
    	this.texteMailActivationMembre = texteMailActivationMembre;
    	cacheTexteMailActivationMembre = texteMailActivationMembre;
    }
	
	public void setTexteMailPasswordOublie(String texteMailPasswordOublie) {
    	this.texteMailPasswordOublie = texteMailPasswordOublie;
    	cacheTexteMailPasswordOublie = texteMailPasswordOublie;
    }

	public void setTextePremierePageAnonyme(String textePremierePageAnonyme) {
    	this.textePremierePageAnonyme = textePremierePageAnonyme;
    	cacheTextePremierePageAnonyme = textePremierePageAnonyme;
    }

	public void setTexteMembreAideCandidatures(String texteMembreAideCandidatures) {
    	this.texteMembreAideCandidatures = texteMembreAideCandidatures;
    	cacheTexteMembreAideCandidatures = texteMembreAideCandidatures;
    }

	public void setTextePremierePageCandidat(String textePremierePageCandidat) {
    	this.textePremierePageCandidat = textePremierePageCandidat;
    	cacheTextePremierePageCandidat = textePremierePageCandidat;
    }

	public void setTextePremierePageMembre(String textePremierePageMembre) {
    	this.textePremierePageMembre = textePremierePageMembre;
    	cacheTextePremierePageMembre = textePremierePageMembre;
    }

	public void setTexteCandidatAideCandidatures(String texteCandidatAideCandidatures) {
    	this.texteCandidatAideCandidatures = texteCandidatAideCandidatures;
    	cacheTexteCandidatAideCandidatures = texteCandidatAideCandidatures;
    }

	public void setTexteCandidatAideCandidatureDepot(String texteCandidatAideCandidatureDepot) {
    	this.texteCandidatAideCandidatureDepot = texteCandidatAideCandidatureDepot;
    	cacheTexteCandidatAideCandidatureDepot = texteCandidatAideCandidatureDepot;
    }

	public void setDateEndCandidat(Date dateEndCandidat) {
    	this.dateEndCandidat = dateEndCandidat;
    	cacheDateEndCandidat = dateEndCandidat;
    }

	public void setDateEndCandidatActif(Date dateEndCandidatActif) {
    	this.dateEndCandidatActif = dateEndCandidatActif;
    	cacheDateEndCandidatActif = dateEndCandidatActif;
    }

	public void setDateEndMembre(Date dateEndMembre) {
    	this.dateEndMembre = dateEndMembre;
    	cacheDateEndMembre = dateEndMembre;
    }

	public static String getCacheTitre() {
    	if(cacheTitre == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTitre = configs.get(0).getTitre();		
    		} else {
    			cacheTitre = "";
    		}
    	}
		return cacheTitre;
    }
	
	public static String getCacheImageUrl() {
    	if(cacheImageUrl == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheImageUrl = configs.get(0).getImageUrl();		
    		} else {
    			cacheImageUrl = "";
    		}
    	}
		return cacheImageUrl;
    }
	
	public static String getCachePiedPage() {
    	if(cachePiedPage == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cachePiedPage = configs.get(0).getPiedPage();
    		} else {
    			cachePiedPage = "";
    		}
    	}
		return cachePiedPage;
    }
		
	public static String getCacheMailFrom() {
    	if(cacheMailFrom == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheMailFrom = configs.get(0).getMailFrom();	
    		} else {
    			cacheMailFrom = "";
    		}
    	}
		return cacheMailFrom;
    }
	
	public static String getCacheMailSubject() {
    	if(cacheMailSubject == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheMailSubject = configs.get(0).getMailSubject();	
    		} else {
    			cacheMailSubject = "";
    		}
    	}
		return cacheMailSubject;
    }

	public static String getCacheTexteMailActivation() {
    	if(cacheTexteMailActivation == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTexteMailActivation = configs.get(0).getTexteMailActivation();	
    		} else {
    			cacheTexteMailActivation = "";
    		}
    	}
    	return cacheTexteMailActivation;
    }
	
	public static String getCacheMailSubjectMembre() {
    	if(cacheMailSubjectMembre == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheMailSubjectMembre = configs.get(0).getMailSubjectMembre();	
    		} else {
    			cacheMailSubjectMembre = "";
    		}
    	}
		return cacheMailSubjectMembre;
    }

	public static String getCacheTexteMailActivationMembre() {
    	if(cacheTexteMailActivationMembre == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTexteMailActivationMembre = configs.get(0).getTexteMailActivationMembre();	
    		} else {
    			cacheTexteMailActivationMembre = "";
    		}
    	}
    	return cacheTexteMailActivationMembre;
    }

	public static String getCacheTexteMailPasswordOublie() {
    	if(cacheTexteMailPasswordOublie == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTexteMailPasswordOublie = configs.get(0).getTexteMailPasswordOublie();		
    		} else {
    			cacheTexteMailPasswordOublie = "";
    		}
    	}
    	return cacheTexteMailPasswordOublie;
    }

	public static String getCacheTextePremierePageAnonyme() {
    	if(cacheTextePremierePageAnonyme == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTextePremierePageAnonyme = configs.get(0).getTextePremierePageAnonyme();		
    		} else {
    			cacheTextePremierePageAnonyme = "";
    		}
    	}
    	return cacheTextePremierePageAnonyme;
    }

	public static String getCacheTexteMembreAideCandidatures() {
    	if(cacheTexteMembreAideCandidatures == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTexteMembreAideCandidatures = configs.get(0).getTexteMembreAideCandidatures();		
    		} else {
    			cacheTexteMembreAideCandidatures = "";
    		}
    	}
    	return cacheTexteMembreAideCandidatures;
    }

	public static String getCacheTextePremierePageCandidat() {
    	if(cacheTextePremierePageCandidat == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTextePremierePageCandidat = configs.get(0).getTextePremierePageCandidat();	
    		} else {
    			cacheTextePremierePageCandidat = "";
    		}
    	}
    	return cacheTextePremierePageCandidat;
    }

	public static String getCacheTextePremierePageMembre() {
    	if(cacheTextePremierePageMembre == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTextePremierePageMembre = configs.get(0).getTextePremierePageMembre();
    		} else {
    			cacheTextePremierePageMembre = "";
    		}
    	}
    	return cacheTextePremierePageMembre;
    }

	public static String getCacheTexteCandidatAideCandidatures() {
    	if(cacheTexteCandidatAideCandidatures == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTexteCandidatAideCandidatures = configs.get(0).getTexteCandidatAideCandidatures();
    		} else {
    			cacheTexteCandidatAideCandidatures = "";
    		}
    	}
    	return cacheTexteCandidatAideCandidatures;
    }

	public static String getCacheTexteCandidatAideCandidatureDepot() {
    	if(cacheTexteCandidatAideCandidatureDepot == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTexteCandidatAideCandidatureDepot = configs.get(0).getTexteCandidatAideCandidatureDepot();
    		} else {
    			cacheTexteCandidatAideCandidatureDepot = "";
    		}
    	}
    	return cacheTexteCandidatAideCandidatureDepot;
    }
	

	public static Date getCacheDateEndCandidat() {
		if (cacheDateEndCandidat == null) {
			List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();
			if (!configs.isEmpty()) {
				cacheDateEndCandidat = configs.get(0).getDateEndCandidat();
			}
		}
		if (cacheDateEndCandidat == null) {
			// initialize to this currentTime + 5 years
			Calendar c = Calendar.getInstance();
			c.roll(Calendar.YEAR, 5);
			cacheDateEndCandidat = c.getTime();
		}
		return cacheDateEndCandidat;
    }
	
	public static Date getCacheDateEndCandidatActif() {
		if (cacheDateEndCandidatActif == null) {
			List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();
			if (!configs.isEmpty()) {
				cacheDateEndCandidatActif = configs.get(0).getDateEndCandidatActif();
			}
		}
		if (cacheDateEndCandidatActif == null) {
			// initialize to this currentTime + 5 years
			Calendar c = Calendar.getInstance();
			c.roll(Calendar.YEAR, 5);
			cacheDateEndCandidatActif = c.getTime();
		}
		return cacheDateEndCandidatActif;
    }
	
	public static Date getCacheDateEndMembre() {
		if (cacheDateEndMembre == null) {
			List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();
			if (!configs.isEmpty()) {
				cacheDateEndMembre = configs.get(0).getDateEndMembre();
			}
		}
		if (cacheDateEndMembre == null) {
			// initialize to this currentTime + 5 years
			Calendar c = Calendar.getInstance();
			c.roll(Calendar.YEAR, 5);
			cacheDateEndMembre = c.getTime();
		}
		return cacheDateEndMembre;
	}
	
}
