// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.AppliConfig;
import java.util.Date;

privileged aspect AppliConfig_Roo_JavaBean {
    
    public Date AppliConfig.getDateEndCandidat() {
        return this.dateEndCandidat;
    }
    
    public Date AppliConfig.getDateEndCandidatActif() {
        return this.dateEndCandidatActif;
    }
    
    public Date AppliConfig.getDateEndMembre() {
        return this.dateEndMembre;
    }
    
    public String AppliConfig.getTitre() {
        return this.titre;
    }
    
    public String AppliConfig.getImageUrl() {
        return this.imageUrl;
    }
    
    public String AppliConfig.getPiedPage() {
        return this.piedPage;
    }
    
    public String AppliConfig.getMailFrom() {
        return this.mailFrom;
    }
    
    public String AppliConfig.getMailSubject() {
        return this.mailSubject;
    }
    
    public String AppliConfig.getTexteMailActivation() {
        return this.texteMailActivation;
    }
    
    public String AppliConfig.getTexteMailPasswordOublie() {
        return this.texteMailPasswordOublie;
    }
    
    public String AppliConfig.getTextePremierePageAnonyme() {
        return this.textePremierePageAnonyme;
    }
    
    public String AppliConfig.getTexteMembreAideCandidatures() {
        return this.texteMembreAideCandidatures;
    }
    
    public String AppliConfig.getMailSubjectMembre() {
        return this.mailSubjectMembre;
    }
    
    public String AppliConfig.getTexteMailActivationMembre() {
        return this.texteMailActivationMembre;
    }
    
    public Boolean AppliConfig.getMembreSupprReviewFile() {
        return this.membreSupprReviewFile;
    }
    
    public String AppliConfig.getTextePremierePageCandidat() {
        return this.textePremierePageCandidat;
    }
    
    public String AppliConfig.getTextePremierePageMembre() {
        return this.textePremierePageMembre;
    }
    
    public String AppliConfig.getTexteCandidatAideCandidatures() {
        return this.texteCandidatAideCandidatures;
    }
    
    public String AppliConfig.getTexteCandidatAideCandidatureDepot() {
        return this.texteCandidatAideCandidatureDepot;
    }
    
    public MailReturnReceiptModeTypes AppliConfig.getMailReturnReceiptModeType() {
        return this.mailReturnReceiptModeType;
    }
    
    public String AppliConfig.getTexteMailCandidatReturnReceipt() {
        return this.texteMailCandidatReturnReceipt;
    }
    
    public String AppliConfig.getTexteEnteteMailCandidatAuditionnable() {
        return this.texteEnteteMailCandidatAuditionnable;
    }
    
    public String AppliConfig.getTextePiedpageMailCandidatAuditionnable() {
        return this.textePiedpageMailCandidatAuditionnable;
    }
    
    public String AppliConfig.getColorCandidatureNonVue() {
        return this.colorCandidatureNonVue;
    }
    
    public String AppliConfig.getColorCandidatureVue() {
        return this.colorCandidatureVue;
    }
    
    public String AppliConfig.getColorCandidatureVueIncomplet() {
        return this.colorCandidatureVueIncomplet;
    }
    
    public String AppliConfig.getColorCandidatureVueModifieDepuis() {
        return this.colorCandidatureVueModifieDepuis;
    }
    
    public String AppliConfig.getColorCandidatureVueIncompletModifieDepuis() {
        return this.colorCandidatureVueIncompletModifieDepuis;
    }
    
    public Long AppliConfig.getCandidatureFileMoSizeMax() {
        return this.candidatureFileMoSizeMax;
    }
    
    public Long AppliConfig.getCandidatureNbFileMax() {
        return this.candidatureNbFileMax;
    }
    
    public String AppliConfig.getCandidatureContentTypeRestrictionRegexp() {
        return this.candidatureContentTypeRestrictionRegexp;
    }
    
}
