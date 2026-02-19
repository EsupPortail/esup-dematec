package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.AppliConfig.MailReturnReceiptModeTypes;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class AppliConfigService {

    @Autowired
    AppliConfigDao appliConfigDao;

    // Auto-injection pour éviter le problème de self-invocation avec @Cacheable
    @Autowired
    @Lazy
    private AppliConfigService self;

    private static final String DEFAULT_STRING = "";
    private static final String DEFAULT_COLOR = "#FFFFFF";
    private static final Boolean DEFAULT_BOOLEAN = false;

    /**
     * Récupère la configuration de l'application (mise en cache)
     */
    @Cacheable(value = "appliConfig", unless = "#result == null")
    public AppliConfig getConfig() {
        return appliConfigDao.getAppliConfig();
    }

    /**
     * Invalide le cache de configuration
     */
    @CacheEvict(value = "appliConfig", allEntries = true)
    public void clearCache() {
        // Le cache est automatiquement vidé par l'annotation @CacheEvict
    }

    /**
     * Retourne une valeur par défaut si la valeur est null
     */
    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Retourne une date par défaut (+5 ans) si la date est null
     */
    private Date getDefaultDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 5);
        return c.getTime();
    }

    public String getCacheTitre() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTitre(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheImageUrl() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getImageUrl(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCachePiedPage() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getPiedPage(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheMailFrom() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getMailFrom(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheMailSubject() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getMailSubject(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteMailActivation() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteMailActivation(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteMailNewCandidatures() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteMailNewCandidatures(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheMailSubjectMembre() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getMailSubjectMembre(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteMailActivationMembre() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteMailActivationMembre(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteMailNewCommissions() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteMailNewCommissions(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteMailPasswordOublie() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteMailPasswordOublie(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTextePremierePageAnonyme() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTextePremierePageAnonyme(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteMembreAideCandidatures() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteMembreAideCandidatures(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTextePremierePageCandidat() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTextePremierePageCandidat(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTextePremierePageMembre() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTextePremierePageMembre(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteCandidatAideCandidatures() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteCandidatAideCandidatures(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteCandidatAideCandidatureDepot() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteCandidatAideCandidatureDepot(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public Date getCacheDateEndCandidat() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getDateEndCandidat(), getDefaultDate()) : getDefaultDate();
    }

    public Date getCacheDateEndCandidatActif() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getDateEndCandidatActif(), getDefaultDate()) : getDefaultDate();
    }

    public Date getCacheDateEndMembre() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getDateEndMembre(), getDefaultDate()) : getDefaultDate();
    }

    public MailReturnReceiptModeTypes getCacheMailReturnReceiptModeType() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getMailReturnReceiptModeType(), MailReturnReceiptModeTypes.NEVER) : MailReturnReceiptModeTypes.NEVER;
    }

    public String getCacheTexteMailCandidatReturnReceipt() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteMailCandidatReturnReceipt(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTexteEnteteMailCandidatAuditionnable() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteEnteteMailCandidatAuditionnable(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheTextePiedpageMailCandidatAuditionnable() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTextePiedpageMailCandidatAuditionnable(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public String getCacheColorCandidatureNonVue() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getColorCandidatureNonVue(), DEFAULT_COLOR) : DEFAULT_COLOR;
    }

    public String getCacheColorCandidatureVue() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getColorCandidatureVue(), DEFAULT_COLOR) : DEFAULT_COLOR;
    }

    public String getCacheColorCandidatureVueIncomplet() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getColorCandidatureVueIncomplet(), DEFAULT_COLOR) : DEFAULT_COLOR;
    }

    public String getCacheColorCandidatureVueModifieDepuis() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getColorCandidatureVueModifieDepuis(), DEFAULT_COLOR) : DEFAULT_COLOR;
    }

    public String getCacheColorCandidatureVueIncompletModifieDepuis() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getColorCandidatureVueIncompletModifieDepuis(), DEFAULT_COLOR) : DEFAULT_COLOR;
    }

    public Boolean getCacheMembreSupprReviewFile() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getMembreSupprReviewFile(), DEFAULT_BOOLEAN) : DEFAULT_BOOLEAN;
    }

    public RecevableEnum getCacheCandidatureRecevableEnumDefault() {
        AppliConfig config = self.getConfig();
        return config != null ? config.getCandidatureRecevableEnumDefault() : null;
    }

    public Boolean getCacheCandidatCanSignup() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getCandidatCanSignup(), DEFAULT_BOOLEAN) : DEFAULT_BOOLEAN;
    }

    public String getCacheColorReporterTag() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getColorReporterTag(), DEFAULT_COLOR) : DEFAULT_COLOR;
    }

    public Boolean getCachePostesMenu4Members() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getPostesMenu4Members(), DEFAULT_BOOLEAN) : DEFAULT_BOOLEAN;
    }

    public Boolean getCachePresidentReportersView() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getPresidentReportersView(), DEFAULT_BOOLEAN) : DEFAULT_BOOLEAN;
    }

    public String getCacheTextePostesMenu4Members() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTextePostesMenu4Members(), DEFAULT_STRING) : DEFAULT_STRING;
    }

    public Boolean getCacheLaureatEnable() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getLaureatEnable(), DEFAULT_BOOLEAN) : DEFAULT_BOOLEAN;
    }

    public String getCacheTexteMailCandidatLaureat() {
        AppliConfig config = self.getConfig();
        return config != null ? getOrDefault(config.getTexteMailCandidatLaureat(), DEFAULT_STRING) : DEFAULT_STRING;
    }
}

