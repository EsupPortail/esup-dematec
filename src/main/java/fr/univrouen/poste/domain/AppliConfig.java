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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
	
	public static enum MailReturnReceiptModeTypes {NEVER, EACH_UPLOAD};

	private static String cacheTitre;
	
	private static String cacheImageUrl;
	
	private static String cachePiedPage;

	private static String cacheMailFrom;

	private static String cacheMailSubject;
	
	private static String cacheTexteMailActivation;
	
	private static String cacheTexteMailNewCandidatures;
	
	private static String cacheMailSubjectMembre;
	
	private static String cacheTexteMailActivationMembre;

	private static String cacheTexteMailPasswordOublie;

	private static String cacheTextePremierePageAnonyme;

	private static String cacheTexteMembreAideCandidatures;

	private static String cacheTextePremierePageCandidat;

	private static String cacheTextePremierePageMembre;

	private static String cacheTexteCandidatAideCandidatures;

	private static String cacheTexteCandidatAideCandidatureDepot;

	private static String cacheTexteMailCandidatReturnReceipt;
	
	private static String cacheTexteEnteteMailCandidatAuditionnable;
	
	private static String cacheTextePiedpageMailCandidatAuditionnable;
	
	private static Date cacheDateEndCandidat;
	
	private static Date cacheDateEndCandidatActif;
	
	private static Date cacheDateEndMembre;
	
	private static MailReturnReceiptModeTypes cacheMailReturnReceiptModeType;
	
	private static String cacheColorCandidatureNonVue;
	
	private static String cacheColorCandidatureVue;
	
	private static String cacheColorCandidatureVueModifieDepuis;
	
	private static String cacheColorCandidatureVueIncomplet;
	
	private static String cacheColorCandidatureVueIncompletModifieDepuis;
	
	private static Boolean cacheMembreSupprReviewFile;
	
	private static Long cacheCandidatureFileMoSizeMax; 
	
	private static Long cacheCandidatureNbFileMax; 
	
	private static String cacheCandidatureContentTypeRestrictionRegexp; 
	
	private static Boolean cacheCandidatureRecevableDefault;
	
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
    
    @Column
    private Boolean membreSupprReviewFile;
    
    // Candidat
    
    @Column(columnDefinition="TEXT")
    private String textePremierePageCandidat;

    @Column(columnDefinition="TEXT")
    private String textePremierePageMembre;
    
    @Column(columnDefinition="TEXT")
    private String texteCandidatAideCandidatures;
    
    @Column(columnDefinition="TEXT")
    private String texteCandidatAideCandidatureDepot;

    @Column
    @Enumerated(EnumType.STRING)
    private MailReturnReceiptModeTypes mailReturnReceiptModeType;
    
    @Column(columnDefinition="TEXT")
	private String texteMailNewCandidatures;
	
    @Column(columnDefinition="TEXT")
    private String texteMailCandidatReturnReceipt;
    
    @Column(columnDefinition="TEXT")
    private String texteEnteteMailCandidatAuditionnable;
    
    @Column(columnDefinition="TEXT")
    private String textePiedpageMailCandidatAuditionnable;
    
    @Column(columnDefinition="TEXT")
    private String colorCandidatureNonVue;
    
    @Column(columnDefinition="TEXT")
    private String colorCandidatureVue;
    
    @Column(columnDefinition="TEXT")
    private String colorCandidatureVueIncomplet;
    
    @Column(columnDefinition="TEXT")
    private String colorCandidatureVueModifieDepuis;
    
    @Column(columnDefinition="TEXT")
    private String colorCandidatureVueIncompletModifieDepuis;
    
    @Column
	private Long candidatureFileMoSizeMax; 
	
    @Column
	private Long candidatureNbFileMax; 
	
    @Column(columnDefinition="TEXT")
	private String candidatureContentTypeRestrictionRegexp; 
    
    @Column
    private Boolean candidatureRecevableDefault;
    
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
	
	public void setMailReturnReceiptModeType(MailReturnReceiptModeTypes mailReturnReceiptModeType) {
    	this.mailReturnReceiptModeType = mailReturnReceiptModeType;
    	cacheMailReturnReceiptModeType = mailReturnReceiptModeType;
    }
	
	public void setTexteMailCandidatReturnReceipt(String texteMailCandidatReturnReceipt) {
    	this.texteMailCandidatReturnReceipt = texteMailCandidatReturnReceipt;
    	cacheTexteMailCandidatReturnReceipt = texteMailCandidatReturnReceipt;
    }
	
	public void setTexteEnteteMailCandidatAuditionnable(String texteEnteteMailCandidatAuditionnable) {
    	this.texteEnteteMailCandidatAuditionnable = texteEnteteMailCandidatAuditionnable;
    	cacheTexteEnteteMailCandidatAuditionnable= texteEnteteMailCandidatAuditionnable;
    }
	
	public void setTextePiedpageMailCandidatAuditionnable(String textePiedpageMailCandidatAuditionnable) {
    	this.textePiedpageMailCandidatAuditionnable = textePiedpageMailCandidatAuditionnable;
    	cacheTextePiedpageMailCandidatAuditionnable= textePiedpageMailCandidatAuditionnable;
    }

	public void setColorCandidatureNonVue(String colorCandidatureNonVue) {
		this.colorCandidatureNonVue = colorCandidatureNonVue;
		cacheColorCandidatureNonVue = colorCandidatureNonVue;
	}

	public void setColorCandidatureVue(String colorCandidatureVue) {
		this.colorCandidatureVue = colorCandidatureVue;
		cacheColorCandidatureVue = colorCandidatureVue;
	}

	public void setColorCandidatureVueIncomplet(String colorCandidatureVueIncomplet) {
		this.colorCandidatureVueIncomplet = colorCandidatureVueIncomplet;
		cacheColorCandidatureVueIncomplet = colorCandidatureVueIncomplet;
	}

	public void setColorCandidatureVueModifieDepuis(
			String colorCandidatureVueModifieDepuis) {
		this.colorCandidatureVueModifieDepuis = colorCandidatureVueModifieDepuis;
		cacheColorCandidatureVueModifieDepuis = colorCandidatureVueModifieDepuis;
	}

	public void setColorCandidatureVueIncompletModifieDepuis(
			String colorCandidatureVueIncompletModifieDepuis) {
		this.colorCandidatureVueIncompletModifieDepuis = colorCandidatureVueIncompletModifieDepuis;
		cacheColorCandidatureVueIncompletModifieDepuis = colorCandidatureVueIncompletModifieDepuis;
	}

	public void setMembreSupprReviewFile(Boolean membreSupprReviewFile) {
		this.membreSupprReviewFile = membreSupprReviewFile;
		cacheMembreSupprReviewFile = membreSupprReviewFile;
	}
	
	

	public void setCandidatureFileMoSizeMax(Long candidatureFileMoSizeMax) {
		this.candidatureFileMoSizeMax = candidatureFileMoSizeMax;
		cacheCandidatureFileMoSizeMax = candidatureFileMoSizeMax;
	}

	public void setCandidatureNbFileMax(Long candidatureNbFileMax) {
		this.candidatureNbFileMax = candidatureNbFileMax;
		cacheCandidatureNbFileMax = candidatureNbFileMax;
	}

	public void setCandidatureContentTypeRestrictionRegexp(
			String candidatureContentTypeRestrictionRegexp) {
		this.candidatureContentTypeRestrictionRegexp = candidatureContentTypeRestrictionRegexp;
		cacheCandidatureContentTypeRestrictionRegexp = candidatureContentTypeRestrictionRegexp;
	}

	public void setCandidatureRecevableDefault(Boolean candidatureRecevableDefault) {
		this.candidatureRecevableDefault = candidatureRecevableDefault;
		cacheCandidatureRecevableDefault = candidatureRecevableDefault;
	}

	public static String getCacheTitre() {
		if(cacheTitre == null) {
			List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
			if(!configs.isEmpty()) {
				cacheTitre = configs.get(0).getTitre();		
			}
			if(cacheTitre == null) {
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
			} 
			if(cacheImageUrl == null) {
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
			} 
			if(cachePiedPage == null) {
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
			}
			if(cacheMailFrom == null) {
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
			} 
			if(cacheMailSubject == null) {
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
			}
			if(cacheTexteMailActivation == null) {
				cacheTexteMailActivation = "";
			}
		}
		return cacheTexteMailActivation;
	}
	
	public static String getCacheTexteMailNewCandidatures() {
		if(cacheTexteMailNewCandidatures == null) {
			List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
			if(!configs.isEmpty()) {
				cacheTexteMailNewCandidatures = configs.get(0).getTexteMailNewCandidatures();	
			}
			if(cacheTexteMailNewCandidatures == null) {
				cacheTexteMailNewCandidatures = "";
			}
		}
		return cacheTexteMailNewCandidatures;
	}

	public static String getCacheMailSubjectMembre() {
		if(cacheMailSubjectMembre == null) {
			List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
			if(!configs.isEmpty()) {
				cacheMailSubjectMembre = configs.get(0).getMailSubjectMembre();	
			} 
			if(cacheMailSubjectMembre == null) {	
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
    		} 
    		if(cacheTexteMailActivationMembre == null) {
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
    		} 
    		if(cacheTexteMailPasswordOublie == null) {
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
    		} 
    		if(cacheTextePremierePageAnonyme == null) {
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
    		} 
    		if(cacheTexteMembreAideCandidatures == null) {
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
    		}
    		if(cacheTextePremierePageCandidat == null) {
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
    		} 
    		if(cacheTextePremierePageMembre == null) {
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
    		} 
    		if(cacheTexteCandidatAideCandidatures == null) {
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
    		} 
    		if(cacheTexteCandidatAideCandidatureDepot == null) {
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
			if (cacheDateEndCandidat == null) {
				// initialize to this currentTime + 5 years
				Calendar c = Calendar.getInstance();
				c.roll(Calendar.YEAR, 5);
				cacheDateEndCandidat = c.getTime();
			}
		}
		return cacheDateEndCandidat;
	}

	public static Date getCacheDateEndCandidatActif() {
		if (cacheDateEndCandidatActif == null) {
			List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();
			if (!configs.isEmpty()) {
				cacheDateEndCandidatActif = configs.get(0).getDateEndCandidatActif();
			}
			if (cacheDateEndCandidatActif == null) {
				// initialize to this currentTime + 5 years
				Calendar c = Calendar.getInstance();
				c.roll(Calendar.YEAR, 5);
				cacheDateEndCandidatActif = c.getTime();
			}
		}
		return cacheDateEndCandidatActif;
	}

	public static Date getCacheDateEndMembre() {
		if (cacheDateEndMembre == null) {
			List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();
			if (!configs.isEmpty()) {
				cacheDateEndMembre = configs.get(0).getDateEndMembre();
			}
			if (cacheDateEndMembre == null) {
				// initialize to this currentTime + 5 years
				Calendar c = Calendar.getInstance();
				c.roll(Calendar.YEAR, 5);
				cacheDateEndMembre = c.getTime();
			}
		}
		return cacheDateEndMembre;
	}
	
	public static MailReturnReceiptModeTypes getCacheMailReturnReceiptModeType() {
    	if(cacheMailReturnReceiptModeType == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheMailReturnReceiptModeType = configs.get(0).getMailReturnReceiptModeType();
    		} 
    		if(cacheMailReturnReceiptModeType == null) {
    			cacheMailReturnReceiptModeType = MailReturnReceiptModeTypes.NEVER;
    		}
    	}
    	return cacheMailReturnReceiptModeType;
    }

	public static String getCacheTexteMailCandidatReturnReceipt() {
    	if(cacheTexteMailCandidatReturnReceipt == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTexteMailCandidatReturnReceipt = configs.get(0).getTexteMailCandidatReturnReceipt();		
    		} 
    		if(cacheTexteMailCandidatReturnReceipt == null) {
    			cacheTexteMailCandidatReturnReceipt = "";
    		}
    	}
    	return cacheTexteMailCandidatReturnReceipt;
    }
	
	public static String getCacheTexteEnteteMailCandidatAuditionnable() {
    	if(cacheTexteEnteteMailCandidatAuditionnable == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTexteEnteteMailCandidatAuditionnable = configs.get(0).getTexteEnteteMailCandidatAuditionnable();		
    		} 
    		if(cacheTexteEnteteMailCandidatAuditionnable == null) {
    			cacheTexteEnteteMailCandidatAuditionnable = "";
    		}
    	}
    	return cacheTexteEnteteMailCandidatAuditionnable;
    }
	
	public static String getCacheTextePiedpageMailCandidatAuditionnable() {
    	if(cacheTextePiedpageMailCandidatAuditionnable == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheTextePiedpageMailCandidatAuditionnable = configs.get(0).getTextePiedpageMailCandidatAuditionnable();		
    		} 
    		if(cacheTextePiedpageMailCandidatAuditionnable == null) {
    			cacheTextePiedpageMailCandidatAuditionnable = "";
    		}
    	}
    	return cacheTextePiedpageMailCandidatAuditionnable;
    }
	
	public static String getCacheColorCandidatureNonVue() {
    	if(cacheColorCandidatureNonVue == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheColorCandidatureNonVue = configs.get(0).getColorCandidatureNonVue();		
    		} 
    		if(cacheColorCandidatureNonVue == null) {
    			cacheColorCandidatureNonVue = "#FFFFFF";
    		}
    	}
    	return cacheColorCandidatureNonVue;
    }
	
	public static String getCacheColorCandidatureVue() {
    	if(cacheColorCandidatureVue == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheColorCandidatureVue = configs.get(0).getColorCandidatureVue();		
    		} 
    		if(cacheColorCandidatureVue == null) {
    			cacheColorCandidatureVue = "#FFFFFF";
    		}
    	}
    	return cacheColorCandidatureVue;
    }
	
	
	public static String getCacheColorCandidatureVueIncomplet() {
    	if(cacheColorCandidatureVueIncomplet == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheColorCandidatureVueIncomplet = configs.get(0).getColorCandidatureVueIncomplet();		
    		} 
    		if(cacheColorCandidatureVueIncomplet == null) {
    			cacheColorCandidatureVueIncomplet = "#FFFFFF";
    		}
    	}
    	return cacheColorCandidatureVueIncomplet;
    }
	
	public static String getCacheColorCandidatureVueModifieDepuis() {
    	if(cacheColorCandidatureVueModifieDepuis == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheColorCandidatureVueModifieDepuis = configs.get(0).getColorCandidatureVueModifieDepuis();		
    		} 
    		if(cacheColorCandidatureVueModifieDepuis == null) {
    			cacheColorCandidatureVueModifieDepuis = "#FFFFFF";
    		}
    	}
    	return cacheColorCandidatureVueModifieDepuis;
    }
	
	
	public static String getCacheColorCandidatureVueIncompletModifieDepuis() {
    	if(cacheColorCandidatureVueIncompletModifieDepuis == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheColorCandidatureVueIncompletModifieDepuis = configs.get(0).getColorCandidatureVueIncompletModifieDepuis();		
    		} 
    		if(cacheColorCandidatureVueIncompletModifieDepuis == null) {
    			cacheColorCandidatureVueIncompletModifieDepuis = "#FFFFFF";
    		}
    	}
    	return cacheColorCandidatureVueIncompletModifieDepuis;
    }

	public static Boolean getCacheMembreSupprReviewFile() {
		if(cacheMembreSupprReviewFile == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheMembreSupprReviewFile = configs.get(0).getMembreSupprReviewFile();		
    		}
    		if(cacheMembreSupprReviewFile == null) {
    			cacheMembreSupprReviewFile = false;
    		}
    	}
    	return cacheMembreSupprReviewFile;
	}

	
	public static Long getCacheCandidatureFileMoSizeMax() {
    	if(cacheCandidatureFileMoSizeMax == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheCandidatureFileMoSizeMax = configs.get(0).getCandidatureFileMoSizeMax();		
    		} 
    		if(cacheCandidatureFileMoSizeMax == null) {
    			cacheCandidatureFileMoSizeMax = new Long(-1);
    		}
    	}
    	return cacheCandidatureFileMoSizeMax;
    }
	
	
	public static Long getCacheCandidatureNbFileMax() {
    	if(cacheCandidatureNbFileMax == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheCandidatureNbFileMax = configs.get(0).getCandidatureNbFileMax();		
    		} 
    		if(cacheCandidatureNbFileMax == null) {
    			cacheCandidatureNbFileMax = new Long(-1);
    		}
    	}
    	return cacheCandidatureNbFileMax;
    }
	
	public static String getCacheCandidatureContentTypeRestrictionRegexp() {
    	if(cacheCandidatureContentTypeRestrictionRegexp == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheCandidatureContentTypeRestrictionRegexp = configs.get(0).getCandidatureContentTypeRestrictionRegexp();		
    		} 
    		if(cacheCandidatureContentTypeRestrictionRegexp == null || cacheCandidatureContentTypeRestrictionRegexp.isEmpty()) {
    			cacheCandidatureContentTypeRestrictionRegexp = ".*";
    		}
    	}
    	return cacheCandidatureContentTypeRestrictionRegexp;
    }

	public static Boolean getCacheCandidatureRecevableDefault() {
		if(cacheCandidatureRecevableDefault == null) {
    		List<AppliConfig> configs = AppliConfig.findAllAppliConfigs();  		
    		if(!configs.isEmpty()) {
    			cacheCandidatureRecevableDefault = configs.get(0).getCandidatureRecevableDefault();		
    		}
    		if(cacheCandidatureRecevableDefault == null) {
    			cacheCandidatureRecevableDefault = true;
    		}
    	}
    	return cacheCandidatureRecevableDefault;
	}
	
}
