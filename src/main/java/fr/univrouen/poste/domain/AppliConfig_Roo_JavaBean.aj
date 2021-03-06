// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
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
    
    public String AppliConfig.getTexteMailNewCommissions() {
        return this.texteMailNewCommissions;
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
    
    public String AppliConfig.getTexteMailNewCandidatures() {
        return this.texteMailNewCandidatures;
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
    
    public RecevableEnum AppliConfig.getCandidatureRecevableEnumDefault() {
        return this.candidatureRecevableEnumDefault;
    }
    
    public Boolean AppliConfig.getCandidatCanSignup() {
        return this.candidatCanSignup;
    }
    
    public String AppliConfig.getColorReporterTag() {
        return this.colorReporterTag;
    }
    
    public void AppliConfig.setColorReporterTag(String colorReporterTag) {
        this.colorReporterTag = colorReporterTag;
    }
    
    public Boolean AppliConfig.getPostesMenu4Members() {
        return this.postesMenu4Members;
    }
    
    public Boolean AppliConfig.getPresidentReportersView() {
        return this.presidentReportersView;
    }
    
    public String AppliConfig.getTextePostesMenu4Members() {
        return this.textePostesMenu4Members;
    }
    
    public Boolean AppliConfig.getLaureatEnable() {
        return this.laureatEnable;
    }
    
    public String AppliConfig.getTexteMailCandidatLaureat() {
        return this.texteMailCandidatLaureat;
    }
    
}
