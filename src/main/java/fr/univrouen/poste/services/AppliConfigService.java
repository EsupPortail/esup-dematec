package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.AppliConfig.MailReturnReceiptModeTypes;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class AppliConfigService {

    @Autowired
    AppliConfigDao appliConfigDao;

    String cacheTitre;
    String cacheImageUrl;
    String cachePiedPage;
    String cacheMailFrom;
    String cacheMailSubject;
    String cacheTexteMailActivation;
    String cacheTexteMailNewCandidatures;
    String cacheMailSubjectMembre;
    String cacheTexteMailActivationMembre;
    String cacheTexteMailNewCommissions;
    String cacheTexteMailPasswordOublie;
    String cacheTextePremierePageAnonyme;
    String cacheTexteMembreAideCandidatures;
    String cacheTextePremierePageCandidat;
    String cacheTextePremierePageMembre;
    String cacheTexteCandidatAideCandidatures;
    String cacheTexteCandidatAideCandidatureDepot;
    String cacheTexteMailCandidatReturnReceipt;
    String cacheTexteEnteteMailCandidatAuditionnable;
    String cacheTextePiedpageMailCandidatAuditionnable;
    Date cacheDateEndCandidat;
    Date cacheDateEndCandidatActif;
    Date cacheDateEndMembre;
    MailReturnReceiptModeTypes cacheMailReturnReceiptModeType;
    String cacheColorCandidatureNonVue;
    String cacheColorCandidatureVue;
    String cacheColorCandidatureVueModifieDepuis;
    String cacheColorCandidatureVueIncomplet;
    String cacheColorCandidatureVueIncompletModifieDepuis;
    Boolean cacheMembreSupprReviewFile;
    RecevableEnum cacheCandidatureRecevableEnumDefault;
    Boolean cacheCandidatCanSignup;
    String cacheColorReporterTag;
    Boolean cachePostesMenu4Members;
    Boolean cachePresidentReportersView;
    String cacheTextePostesMenu4Members;
    Boolean cacheLaureatEnable;
    String cacheTexteMailCandidatLaureat;

    AppliConfig getConfig() {
        AppliConfig config = appliConfigDao.getAppliConfig();
        return config;
    }

    public String getCacheTitre() {
        if (cacheTitre == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTitre = config.getTitre();
            }
            if (cacheTitre == null) {
                cacheTitre = "";
            }
        }
        return cacheTitre;
    }

    public String getCacheImageUrl() {
        if (cacheImageUrl == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheImageUrl = config.getImageUrl();
            }
            if (cacheImageUrl == null) {
                cacheImageUrl = "";
            }
        }
        return cacheImageUrl;
    }

    public String getCachePiedPage() {
        if (cachePiedPage == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cachePiedPage = config.getPiedPage();
            }
            if (cachePiedPage == null) {
                cachePiedPage = "";
            }
        }
        return cachePiedPage;
    }

    public String getCacheMailFrom() {
        if (cacheMailFrom == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheMailFrom = config.getMailFrom();
            }
            if (cacheMailFrom == null) {
                cacheMailFrom = "";
            }
        }
        return cacheMailFrom;
    }

    public String getCacheMailSubject() {
        if (cacheMailSubject == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheMailSubject = config.getMailSubject();
            }
            if (cacheMailSubject == null) {
                cacheMailSubject = "";
            }
        }
        return cacheMailSubject;
    }

    public String getCacheTexteMailActivation() {
        if (cacheTexteMailActivation == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteMailActivation = config.getTexteMailActivation();
            }
            if (cacheTexteMailActivation == null) {
                cacheTexteMailActivation = "";
            }
        }
        return cacheTexteMailActivation;
    }

    public String getCacheTexteMailNewCandidatures() {
        if (cacheTexteMailNewCandidatures == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteMailNewCandidatures = config.getTexteMailNewCandidatures();
            }
            if (cacheTexteMailNewCandidatures == null) {
                cacheTexteMailNewCandidatures = "";
            }
        }
        return cacheTexteMailNewCandidatures;
    }

    public String getCacheMailSubjectMembre() {
        if (cacheMailSubjectMembre == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheMailSubjectMembre = config.getMailSubjectMembre();
            }
            if (cacheMailSubjectMembre == null) {
                cacheMailSubjectMembre = "";
            }
        }
        return cacheMailSubjectMembre;
    }

    public String getCacheTexteMailActivationMembre() {
        if (cacheTexteMailActivationMembre == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteMailActivationMembre = config.getTexteMailActivationMembre();
            }
            if (cacheTexteMailActivationMembre == null) {
                cacheTexteMailActivationMembre = "";
            }
        }
        return cacheTexteMailActivationMembre;
    }

    public String getCacheTexteMailNewCommissions() {
        if (cacheTexteMailNewCommissions == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteMailNewCommissions = config.getTexteMailNewCommissions();
            }
            if (cacheTexteMailNewCommissions == null) {
                cacheTexteMailNewCommissions = "";
            }
        }
        return cacheTexteMailNewCommissions;
    }

    public String getCacheTexteMailPasswordOublie() {
        if (cacheTexteMailPasswordOublie == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteMailPasswordOublie = config.getTexteMailPasswordOublie();
            }
            if (cacheTexteMailPasswordOublie == null) {
                cacheTexteMailPasswordOublie = "";
            }
        }
        return cacheTexteMailPasswordOublie;
    }

    public String getCacheTextePremierePageAnonyme() {
        if (cacheTextePremierePageAnonyme == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTextePremierePageAnonyme = config.getTextePremierePageAnonyme();
            }
            if (cacheTextePremierePageAnonyme == null) {
                cacheTextePremierePageAnonyme = "";
            }
        }
        return cacheTextePremierePageAnonyme;
    }

    public String getCacheTexteMembreAideCandidatures() {
        if (cacheTexteMembreAideCandidatures == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteMembreAideCandidatures = config.getTexteMembreAideCandidatures();
            }
            if (cacheTexteMembreAideCandidatures == null) {
                cacheTexteMembreAideCandidatures = "";
            }
        }
        return cacheTexteMembreAideCandidatures;
    }

    public String getCacheTextePremierePageCandidat() {
        if (cacheTextePremierePageCandidat == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTextePremierePageCandidat = config.getTextePremierePageCandidat();
            }
            if (cacheTextePremierePageCandidat == null) {
                cacheTextePremierePageCandidat = "";
            }
        }
        return cacheTextePremierePageCandidat;
    }

    public String getCacheTextePremierePageMembre() {
        if (cacheTextePremierePageMembre == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTextePremierePageMembre = config.getTextePremierePageMembre();
            }
            if (cacheTextePremierePageMembre == null) {
                cacheTextePremierePageMembre = "";
            }
        }
        return cacheTextePremierePageMembre;
    }

    public String getCacheTexteCandidatAideCandidatures() {
        if (cacheTexteCandidatAideCandidatures == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteCandidatAideCandidatures = config.getTexteCandidatAideCandidatures();
            }
            if (cacheTexteCandidatAideCandidatures == null) {
                cacheTexteCandidatAideCandidatures = "";
            }
        }
        return cacheTexteCandidatAideCandidatures;
    }

    public String getCacheTexteCandidatAideCandidatureDepot() {
        if (cacheTexteCandidatAideCandidatureDepot == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteCandidatAideCandidatureDepot = config.getTexteCandidatAideCandidatureDepot();
            }
            if (cacheTexteCandidatAideCandidatureDepot == null) {
                cacheTexteCandidatAideCandidatureDepot = "";
            }
        }
        return cacheTexteCandidatAideCandidatureDepot;
    }

    public Date getCacheDateEndCandidat() {
        if (cacheDateEndCandidat == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheDateEndCandidat = config.getDateEndCandidat();
            }
            if (cacheDateEndCandidat == null) {
                Calendar c = Calendar.getInstance();
                c.roll(Calendar.YEAR, 5);
                cacheDateEndCandidat = c.getTime();
            }
        }
        return cacheDateEndCandidat;
    }

    public Date getCacheDateEndCandidatActif() {
        if (cacheDateEndCandidatActif == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheDateEndCandidatActif = config.getDateEndCandidatActif();
            }
            if (cacheDateEndCandidatActif == null) {
                Calendar c = Calendar.getInstance();
                c.roll(Calendar.YEAR, 5);
                cacheDateEndCandidatActif = c.getTime();
            }
        }
        return cacheDateEndCandidatActif;
    }

    public Date getCacheDateEndMembre() {
        if (cacheDateEndMembre == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheDateEndMembre = config.getDateEndMembre();
            }
            if (cacheDateEndMembre == null) {
                Calendar c = Calendar.getInstance();
                c.roll(Calendar.YEAR, 5);
                cacheDateEndMembre = c.getTime();
            }
        }
        return cacheDateEndMembre;
    }

    public MailReturnReceiptModeTypes getCacheMailReturnReceiptModeType() {
        if (cacheMailReturnReceiptModeType == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheMailReturnReceiptModeType = config.getMailReturnReceiptModeType();
            }
            if (cacheMailReturnReceiptModeType == null) {
                cacheMailReturnReceiptModeType = MailReturnReceiptModeTypes.NEVER;
            }
        }
        return cacheMailReturnReceiptModeType;
    }

    public String getCacheTexteMailCandidatReturnReceipt() {
        if (cacheTexteMailCandidatReturnReceipt == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteMailCandidatReturnReceipt = config.getTexteMailCandidatReturnReceipt();
            }
            if (cacheTexteMailCandidatReturnReceipt == null) {
                cacheTexteMailCandidatReturnReceipt = "";
            }
        }
        return cacheTexteMailCandidatReturnReceipt;
    }

    public String getCacheTexteEnteteMailCandidatAuditionnable() {
        if (cacheTexteEnteteMailCandidatAuditionnable == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteEnteteMailCandidatAuditionnable = config.getTexteEnteteMailCandidatAuditionnable();
            }
            if (cacheTexteEnteteMailCandidatAuditionnable == null) {
                cacheTexteEnteteMailCandidatAuditionnable = "";
            }
        }
        return cacheTexteEnteteMailCandidatAuditionnable;
    }

    public String getCacheTextePiedpageMailCandidatAuditionnable() {
        if (cacheTextePiedpageMailCandidatAuditionnable == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTextePiedpageMailCandidatAuditionnable = config.getTextePiedpageMailCandidatAuditionnable();
            }
            if (cacheTextePiedpageMailCandidatAuditionnable == null) {
                cacheTextePiedpageMailCandidatAuditionnable = "";
            }
        }
        return cacheTextePiedpageMailCandidatAuditionnable;
    }

    public String getCacheColorCandidatureNonVue() {
        if (cacheColorCandidatureNonVue == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheColorCandidatureNonVue = config.getColorCandidatureNonVue();
            }
            if (cacheColorCandidatureNonVue == null) {
                cacheColorCandidatureNonVue = "#FFFFFF";
            }
        }
        return cacheColorCandidatureNonVue;
    }

    public String getCacheColorCandidatureVue() {
        if (cacheColorCandidatureVue == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheColorCandidatureVue = config.getColorCandidatureVue();
            }
            if (cacheColorCandidatureVue == null) {
                cacheColorCandidatureVue = "#FFFFFF";
            }
        }
        return cacheColorCandidatureVue;
    }

    public String getCacheColorCandidatureVueIncomplet() {
        if (cacheColorCandidatureVueIncomplet == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheColorCandidatureVueIncomplet = config.getColorCandidatureVueIncomplet();
            }
            if (cacheColorCandidatureVueIncomplet == null) {
                cacheColorCandidatureVueIncomplet = "#FFFFFF";
            }
        }
        return cacheColorCandidatureVueIncomplet;
    }

    public String getCacheColorCandidatureVueModifieDepuis() {
        if (cacheColorCandidatureVueModifieDepuis == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheColorCandidatureVueModifieDepuis = config.getColorCandidatureVueModifieDepuis();
            }
            if (cacheColorCandidatureVueModifieDepuis == null) {
                cacheColorCandidatureVueModifieDepuis = "#FFFFFF";
            }
        }
        return cacheColorCandidatureVueModifieDepuis;
    }

    public String getCacheColorCandidatureVueIncompletModifieDepuis() {
        if (cacheColorCandidatureVueIncompletModifieDepuis == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheColorCandidatureVueIncompletModifieDepuis = config.getColorCandidatureVueIncompletModifieDepuis();
            }
            if (cacheColorCandidatureVueIncompletModifieDepuis == null) {
                cacheColorCandidatureVueIncompletModifieDepuis = "#FFFFFF";
            }
        }
        return cacheColorCandidatureVueIncompletModifieDepuis;
    }

    public Boolean getCacheMembreSupprReviewFile() {
        if (cacheMembreSupprReviewFile == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheMembreSupprReviewFile = config.getMembreSupprReviewFile();
            }
            if (cacheMembreSupprReviewFile == null) {
                cacheMembreSupprReviewFile = false;
            }
        }
        return cacheMembreSupprReviewFile;
    }

    public RecevableEnum getCacheCandidatureRecevableEnumDefault() {
        if (cacheCandidatureRecevableEnumDefault == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheCandidatureRecevableEnumDefault = config.getCandidatureRecevableEnumDefault();
            }
        }
        return cacheCandidatureRecevableEnumDefault;
    }

    public Boolean getCacheCandidatCanSignup() {
        if (cacheCandidatCanSignup == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheCandidatCanSignup = config.getCandidatCanSignup();
            }
            if (cacheCandidatCanSignup == null) {
                cacheCandidatCanSignup = false;
            }
        }
        return cacheCandidatCanSignup;
    }

    public String getCacheColorReporterTag() {
        if (cacheColorReporterTag == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheColorReporterTag = config.getColorReporterTag();
            }
            if (cacheColorReporterTag == null) {
                cacheColorReporterTag = "#FFFFFF";
            }
        }
        return cacheColorReporterTag;
    }

    public Boolean getCachePostesMenu4Members() {
        if (cachePostesMenu4Members == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cachePostesMenu4Members = config.getPostesMenu4Members();
            }
            if (cachePostesMenu4Members == null) {
                cachePostesMenu4Members = false;
            }
        }
        return cachePostesMenu4Members;
    }

    public Boolean getCachePresidentReportersView() {
        if (cachePresidentReportersView == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cachePresidentReportersView = config.getPresidentReportersView();
            }
            if (cachePresidentReportersView == null) {
                cachePresidentReportersView = false;
            }
        }
        return cachePresidentReportersView;
    }

    public String getCacheTextePostesMenu4Members() {
        if (cacheTextePostesMenu4Members == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTextePostesMenu4Members = config.getTextePostesMenu4Members();
            }
            if (cacheTextePostesMenu4Members == null) {
                cacheTextePostesMenu4Members = "";
            }
        }
        return cacheTextePostesMenu4Members;
    }

    public Boolean getCacheLaureatEnable() {
        if (cacheLaureatEnable == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheLaureatEnable = config.getLaureatEnable();
            }
            if (cacheLaureatEnable == null) {
                cacheLaureatEnable = false;
            }
        }
        return cacheLaureatEnable;
    }

    public String getCacheTexteMailCandidatLaureat() {
        if (cacheTexteMailCandidatLaureat == null) {
            AppliConfig config = getConfig();
            if (config != null) {
                cacheTexteMailCandidatLaureat = config.getTexteMailCandidatLaureat();
            }
            if (cacheTexteMailCandidatLaureat == null) {
                cacheTexteMailCandidatLaureat = "";
            }
        }
        return cacheTexteMailCandidatLaureat;
    }

    public void clearCache() {
        cacheTitre = null;
        cacheImageUrl = null;
        cachePiedPage = null;
        cacheMailFrom = null;
        cacheMailSubject = null;
        cacheTexteMailActivation = null;
        cacheTexteMailNewCandidatures = null;
        cacheMailSubjectMembre = null;
        cacheTexteMailActivationMembre = null;
        cacheTexteMailNewCommissions = null;
        cacheTexteMailPasswordOublie = null;
        cacheTextePremierePageAnonyme = null;
        cacheTexteMembreAideCandidatures = null;
        cacheTextePremierePageCandidat = null;
        cacheTextePremierePageMembre = null;
        cacheTexteCandidatAideCandidatures = null;
        cacheTexteCandidatAideCandidatureDepot = null;
        cacheTexteMailCandidatReturnReceipt = null;
        cacheTexteEnteteMailCandidatAuditionnable = null;
        cacheTextePiedpageMailCandidatAuditionnable = null;
        cacheDateEndCandidat = null;
        cacheDateEndCandidatActif = null;
        cacheDateEndMembre = null;
        cacheMailReturnReceiptModeType = null;
        cacheColorCandidatureNonVue = null;
        cacheColorCandidatureVue = null;
        cacheColorCandidatureVueModifieDepuis = null;
        cacheColorCandidatureVueIncomplet = null;
        cacheColorCandidatureVueIncompletModifieDepuis = null;
        cacheMembreSupprReviewFile = null;
        cacheCandidatureRecevableEnumDefault = null;
        cacheCandidatCanSignup = null;
        cacheColorReporterTag = null;
        cachePostesMenu4Members = null;
        cachePresidentReportersView = null;
        cacheTextePostesMenu4Members = null;
        cacheLaureatEnable = null;
        cacheTexteMailCandidatLaureat = null;
    }
}

