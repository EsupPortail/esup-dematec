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
package fr.univrouen.poste.web;

import fr.univrouen.poste.dao.*;
import fr.univrouen.poste.domain.*;
import fr.univrouen.poste.services.AppliConfigService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

@Configurable
/**
 * A central place to register application converters and formatters. 
 */
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	@Resource
	AppliConfigDao appliConfigDao;

    @Resource
	AppliConfigService appliConfigService;

    @Resource
	CommissionEntryDao commissionEntryDao;

    @Resource
	GalaxieEntryDao galaxieEntryDao;

    @Resource
	PosteCandidatureDao posteCandidatureDao;

    @Resource
    GalaxieExcelDao galaxieExcelDao;

    @Resource
    GalaxieMappingDao galaxieMappingDao;

    @Resource
    AppliConfigFileTypeDao appliConfigFileTypeDao;

    @Resource
    LogAuthDao logAuthDao;

    @Resource
    CommissionExcelDao commissionExcelDao;

    @Resource
    TemplateFileDao templateFileDao;

    @Resource
    UserDao userDao;

    @Resource
    PosteAPourvoirDao posteAPourvoirDao;

    @Resource
    PosteCandidatureTagDao posteCandidatureTagDao;

    @Resource
    PosteCandidatureTagValueDao posteCandidatureTagValueDao;

    @Resource
    LogImportCommissionDao logImportCommissionDao;

    @Resource
    LogImportGalaxieDao logImportGalaxieDao;

    @Resource
    LogPosteFileDao logPosteFileDao;

    @Resource
    LogMailDao logMailDao;

    @Resource
    LogFileDao logFileDao;

	protected void installFormatters(FormatterRegistry registry) {
		// not used
	}

	public Converter<PosteAPourvoir, String> getPosteAPourvoirToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.PosteAPourvoir, java.lang.String>() {
            public String convert(PosteAPourvoir posteAPourvoir) {
                return posteAPourvoir.getNumEmploi();
            }
        };
    }

	public Converter<PosteCandidature, String> getPosteCandidatureToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.PosteCandidature, java.lang.String>() {
            public String convert(PosteCandidature posteCandidature) {
                return posteCandidature.getCandidat().getEmailAddress() + " [" + posteCandidature.getPoste().getNumEmploi() + "]";
            }
        };
    }

	public Converter<User, String> getUserToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.User, java.lang.String>() {
            public String convert(User user) {
                return user.getEmailAddress();
            }
        };
    }

    public Converter<PosteCandidatureFile, String> getPosteCandidatureFileToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.PosteCandidatureFile, java.lang.String>() {
            public String convert(PosteCandidatureFile posteCandidatureFile) {
                return posteCandidatureFile.getFilename();
            }
        };
    }


	public Converter<AppliConfig, String> getAppliConfigToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.AppliConfig, java.lang.String>() {
            public String convert(AppliConfig appliConfig) {
                return appliConfig.getTitre() + ' ' + appliConfig.getImageUrl() + ' ' + appliConfig.getPiedPage() + ' ' + appliConfig.getMailFrom();
            }
        };
    }

	public Converter<Long, AppliConfig> getIdToAppliConfigConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.AppliConfig>() {
            public fr.univrouen.poste.domain.AppliConfig convert(java.lang.Long id) {
                return appliConfigDao.findAppliConfig(id);
            }
        };
    }

	public Converter<String, AppliConfig> getStringToAppliConfigConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.AppliConfig>() {
            public fr.univrouen.poste.domain.AppliConfig convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), AppliConfig.class);
            }
        };
    }

	public Converter<AppliConfigFileType, String> getAppliConfigFileTypeToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.AppliConfigFileType, java.lang.String>() {
            public String convert(AppliConfigFileType appliConfigFileType) {
                return String.valueOf(appliConfigFileType.getListIndex()) + ' ' + appliConfigFileType.getTypeTitle() + ' ' + appliConfigFileType.getTypeDescription() + ' ' + appliConfigFileType.getCandidatureFileMoSizeMax();
            }
        };
    }

	public Converter<Long, AppliConfigFileType> getIdToAppliConfigFileTypeConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.AppliConfigFileType>() {
            public fr.univrouen.poste.domain.AppliConfigFileType convert(java.lang.Long id) {
                return appliConfigFileTypeDao.findAppliConfigFileType(id);
            }
        };
    }

	public Converter<String, AppliConfigFileType> getStringToAppliConfigFileTypeConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.AppliConfigFileType>() {
            public fr.univrouen.poste.domain.AppliConfigFileType convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), AppliConfigFileType.class);
            }
        };
    }

	public Converter<CommissionEntry, String> getCommissionEntryToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.CommissionEntry, java.lang.String>() {
            public String convert(CommissionEntry commissionEntry) {
                return commissionEntry.getNumPoste() + ' ' + commissionEntry.getEmail() + ' ' + commissionEntry.getNom() + ' ' + commissionEntry.getPrenom();
            }
        };
    }

	public Converter<Long, CommissionEntry> getIdToCommissionEntryConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.CommissionEntry>() {
            public fr.univrouen.poste.domain.CommissionEntry convert(java.lang.Long id) {
                return commissionEntryDao.findCommissionEntry(id);
            }
        };
    }

	public Converter<String, CommissionEntry> getStringToCommissionEntryConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.CommissionEntry>() {
            public fr.univrouen.poste.domain.CommissionEntry convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), CommissionEntry.class);
            }
        };
    }

	public Converter<CommissionExcel, String> getCommissionExcelToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.CommissionExcel, java.lang.String>() {
            public String convert(CommissionExcel commissionExcel) {
                return commissionExcel.getFilename() + ' ' + commissionExcel.getFile() + ' ' + commissionExcel.getCreation();
            }
        };
    }

	public Converter<Long, CommissionExcel> getIdToCommissionExcelConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.CommissionExcel>() {
            public fr.univrouen.poste.domain.CommissionExcel convert(java.lang.Long id) {
                return commissionExcelDao.findCommissionExcel(id);
            }
        };
    }

	public Converter<String, CommissionExcel> getStringToCommissionExcelConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.CommissionExcel>() {
            public fr.univrouen.poste.domain.CommissionExcel convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), CommissionExcel.class);
            }
        };
    }

	public Converter<GalaxieEntry, String> getGalaxieEntryToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.GalaxieEntry, java.lang.String>() {
            public String convert(GalaxieEntry galaxieEntry) {
                return galaxieEntry.getNumEmploi() + ' ' + galaxieEntry.getNumCandidat() + ' ' + galaxieEntry.getCivilite() + ' ' + galaxieEntry.getNom();
            }
        };
    }

	public Converter<Long, GalaxieEntry> getIdToGalaxieEntryConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.GalaxieEntry>() {
            public fr.univrouen.poste.domain.GalaxieEntry convert(java.lang.Long id) {
                return galaxieEntryDao.findGalaxieEntry(id);
            }
        };
    }

	public Converter<String, GalaxieEntry> getStringToGalaxieEntryConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.GalaxieEntry>() {
            public fr.univrouen.poste.domain.GalaxieEntry convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), GalaxieEntry.class);
            }
        };
    }

	public Converter<GalaxieExcel, String> getGalaxieExcelToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.GalaxieExcel, java.lang.String>() {
            public String convert(GalaxieExcel galaxieExcel) {
                return galaxieExcel.getFilename() + ' ' + galaxieExcel.getFile() + ' ' + galaxieExcel.getCreation();
            }
        };
    }

	public Converter<Long, GalaxieExcel> getIdToGalaxieExcelConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.GalaxieExcel>() {
            public fr.univrouen.poste.domain.GalaxieExcel convert(java.lang.Long id) {
                return galaxieExcelDao.findGalaxieExcel(id);
            }
        };
    }

	public Converter<String, GalaxieExcel> getStringToGalaxieExcelConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.GalaxieExcel>() {
            public fr.univrouen.poste.domain.GalaxieExcel convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), GalaxieExcel.class);
            }
        };
    }

	public Converter<GalaxieMapping, String> getGalaxieMappingToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.GalaxieMapping, java.lang.String>() {
            public String convert(GalaxieMapping galaxieMapping) {
                return galaxieMapping.getId_numemploi() + ' ' + galaxieMapping.getId_numCandidat() + ' ' + galaxieMapping.getId_civilite() + ' ' + galaxieMapping.getId_nom();
            }
        };
    }

	public Converter<Long, GalaxieMapping> getIdToGalaxieMappingConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.GalaxieMapping>() {
            public fr.univrouen.poste.domain.GalaxieMapping convert(java.lang.Long id) {
                return galaxieMappingDao.findGalaxieMapping(id);
            }
        };
    }

	public Converter<String, GalaxieMapping> getStringToGalaxieMappingConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.GalaxieMapping>() {
            public fr.univrouen.poste.domain.GalaxieMapping convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), GalaxieMapping.class);
            }
        };
    }

	public Converter<LogAuth, String> getLogAuthToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.LogAuth, java.lang.String>() {
            public String convert(LogAuth logAuth) {
                return String.valueOf(logAuth.getActionDate()) + ' ' + logAuth.getUserId() + ' ' + logAuth.getIp() + ' ' + logAuth.getAction();
            }
        };
    }

	public Converter<Long, LogAuth> getIdToLogAuthConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.LogAuth>() {
            public fr.univrouen.poste.domain.LogAuth convert(java.lang.Long id) {
                return logAuthDao.findLogAuth(id);
            }
        };
    }

	public Converter<String, LogAuth> getStringToLogAuthConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.LogAuth>() {
            public fr.univrouen.poste.domain.LogAuth convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), LogAuth.class);
            }
        };
    }

	public Converter<LogFile, String> getLogFileToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.LogFile, java.lang.String>() {
            public String convert(LogFile logFile) {
                return String.valueOf(logFile.getActionDate()) + ' ' + logFile.getUserId() + ' ' + logFile.getNumEmploi() + ' ' + logFile.getNumCandidat();
            }
        };
    }

	public Converter<Long, LogFile> getIdToLogFileConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.LogFile>() {
            public fr.univrouen.poste.domain.LogFile convert(java.lang.Long id) {
                return logFileDao.findLogFile(id);
            }
        };
    }

	public Converter<String, LogFile> getStringToLogFileConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.LogFile>() {
            public fr.univrouen.poste.domain.LogFile convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), LogFile.class);
            }
        };
    }

	public Converter<LogImportCommission, String> getLogImportCommissionToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.LogImportCommission, java.lang.String>() {
            public String convert(LogImportCommission logImportCommission) {
                return String.valueOf(logImportCommission.getActionDate()) + ' ' + logImportCommission.getMessage() + ' ' + logImportCommission.getStatus();
            }
        };
    }

	public Converter<Long, LogImportCommission> getIdToLogImportCommissionConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.LogImportCommission>() {
            public fr.univrouen.poste.domain.LogImportCommission convert(java.lang.Long id) {
                return logImportCommissionDao.findLogImportCommission(id);
            }
        };
    }

	public Converter<String, LogImportCommission> getStringToLogImportCommissionConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.LogImportCommission>() {
            public fr.univrouen.poste.domain.LogImportCommission convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), LogImportCommission.class);
            }
        };
    }

	public Converter<LogImportGalaxie, String> getLogImportGalaxieToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.LogImportGalaxie, java.lang.String>() {
            public String convert(LogImportGalaxie logImportGalaxie) {
                return String.valueOf(logImportGalaxie.getActionDate()) + ' ' + logImportGalaxie.getMessage() + ' ' + logImportGalaxie.getStatus();
            }
        };
    }

	public Converter<Long, LogImportGalaxie> getIdToLogImportGalaxieConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.LogImportGalaxie>() {
            public fr.univrouen.poste.domain.LogImportGalaxie convert(java.lang.Long id) {
                return logImportGalaxieDao.findLogImportGalaxie(id);
            }
        };
    }

	public Converter<String, LogImportGalaxie> getStringToLogImportGalaxieConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.LogImportGalaxie>() {
            public fr.univrouen.poste.domain.LogImportGalaxie convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), LogImportGalaxie.class);
            }
        };
    }

	public Converter<LogMail, String> getLogMailToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.LogMail, java.lang.String>() {
            public String convert(LogMail logMail) {
                return String.valueOf(logMail.getActionDate()) + ' ' + logMail.getMailTo() + ' ' + logMail.getMessage() + ' ' + logMail.getStatus();
            }
        };
    }

	public Converter<Long, LogMail> getIdToLogMailConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.LogMail>() {
            public fr.univrouen.poste.domain.LogMail convert(java.lang.Long id) {
                return logMailDao.findLogMail(id);
            }
        };
    }

	public Converter<String, LogMail> getStringToLogMailConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.LogMail>() {
            public fr.univrouen.poste.domain.LogMail convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), LogMail.class);
            }
        };
    }

	public Converter<LogPosteFile, String> getLogPosteFileToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.LogPosteFile, java.lang.String>() {
            public String convert(LogPosteFile logPosteFile) {
                return String.valueOf(logPosteFile.getActionDate()) + ' ' + logPosteFile.getNumEmploi() + ' ' + logPosteFile.getEmail() + ' ' + logPosteFile.getIp();
            }
        };
    }

	public Converter<Long, LogPosteFile> getIdToLogPosteFileConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.LogPosteFile>() {
            public fr.univrouen.poste.domain.LogPosteFile convert(java.lang.Long id) {
                return logPosteFileDao.findLogPosteFile(id);
            }
        };
    }

	public Converter<String, LogPosteFile> getStringToLogPosteFileConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.LogPosteFile>() {
            public fr.univrouen.poste.domain.LogPosteFile convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), LogPosteFile.class);
            }
        };
    }

	public Converter<Long, PosteAPourvoir> getIdToPosteAPourvoirConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.PosteAPourvoir>() {
            public fr.univrouen.poste.domain.PosteAPourvoir convert(java.lang.Long id) {
                return posteAPourvoirDao.findPosteAPourvoir(id);
            }
        };
    }

	public Converter<String, PosteAPourvoir> getStringToPosteAPourvoirConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.PosteAPourvoir>() {
            public fr.univrouen.poste.domain.PosteAPourvoir convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PosteAPourvoir.class);
            }
        };
    }

	public Converter<Long, PosteCandidature> getIdToPosteCandidatureConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.PosteCandidature>() {
            public fr.univrouen.poste.domain.PosteCandidature convert(java.lang.Long id) {
                return posteCandidatureDao != null ? posteCandidatureDao.findPosteCandidature(id) : posteCandidatureDao.findPosteCandidature(id);
            }
        };
    }

	public Converter<String, PosteCandidature> getStringToPosteCandidatureConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.PosteCandidature>() {
            public fr.univrouen.poste.domain.PosteCandidature convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PosteCandidature.class);
            }
        };
    }

	public Converter<PosteCandidatureTag, String> getPosteCandidatureTagToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.PosteCandidatureTag, java.lang.String>() {
            public String convert(PosteCandidatureTag posteCandidatureTag) {
                return posteCandidatureTag.getName();
            }
        };
    }

	public Converter<Long, PosteCandidatureTag> getIdToPosteCandidatureTagConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.PosteCandidatureTag>() {
            public fr.univrouen.poste.domain.PosteCandidatureTag convert(java.lang.Long id) {
                return posteCandidatureTagDao.findPosteCandidatureTag(id);
            }
        };
    }

	public Converter<String, PosteCandidatureTag> getStringToPosteCandidatureTagConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.PosteCandidatureTag>() {
            public fr.univrouen.poste.domain.PosteCandidatureTag convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PosteCandidatureTag.class);
            }
        };
    }

	public Converter<PosteCandidatureTagValue, String> getPosteCandidatureTagValueToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.PosteCandidatureTagValue, java.lang.String>() {
            public String convert(PosteCandidatureTagValue posteCandidatureTagValue) {
                return posteCandidatureTagValue.getValue();
            }
        };
    }

	public Converter<Long, PosteCandidatureTagValue> getIdToPosteCandidatureTagValueConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.PosteCandidatureTagValue>() {
            public fr.univrouen.poste.domain.PosteCandidatureTagValue convert(java.lang.Long id) {
                return posteCandidatureTagValueDao.findPosteCandidatureTagValue(id);
            }
        };
    }

	public Converter<String, PosteCandidatureTagValue> getStringToPosteCandidatureTagValueConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.PosteCandidatureTagValue>() {
            public fr.univrouen.poste.domain.PosteCandidatureTagValue convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PosteCandidatureTagValue.class);
            }
        };
    }

	public Converter<TemplateFile, String> getTemplateFileToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.TemplateFile, java.lang.String>() {
            public String convert(TemplateFile templateFile) {
                return templateFile.getFilename() + ' ' + templateFile.getFile() + ' ' + templateFile.getSendTime() + ' ' + templateFile.getTemplateFileType();
            }
        };
    }

	public Converter<Long, TemplateFile> getIdToTemplateFileConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.TemplateFile>() {
            public fr.univrouen.poste.domain.TemplateFile convert(java.lang.Long id) {
                return templateFileDao.findTemplateFile(id);
            }
        };
    }

	public Converter<String, TemplateFile> getStringToTemplateFileConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.TemplateFile>() {
            public fr.univrouen.poste.domain.TemplateFile convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), TemplateFile.class);
            }
        };
    }

	public Converter<Long, User> getIdToUserConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, fr.univrouen.poste.domain.User>() {
            public fr.univrouen.poste.domain.User convert(java.lang.Long id) {
                return userDao.findUser(id);
            }
        };
    }

	public Converter<String, User> getStringToUserConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, fr.univrouen.poste.domain.User>() {
            public fr.univrouen.poste.domain.User convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), User.class);
            }
        };
    }

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getAppliConfigToStringConverter());
        registry.addConverter(getIdToAppliConfigConverter());
        registry.addConverter(getStringToAppliConfigConverter());
        registry.addConverter(getAppliConfigFileTypeToStringConverter());
        registry.addConverter(getIdToAppliConfigFileTypeConverter());
        registry.addConverter(getStringToAppliConfigFileTypeConverter());
        registry.addConverter(getCommissionEntryToStringConverter());
        registry.addConverter(getIdToCommissionEntryConverter());
        registry.addConverter(getStringToCommissionEntryConverter());
        registry.addConverter(getCommissionExcelToStringConverter());
        registry.addConverter(getIdToCommissionExcelConverter());
        registry.addConverter(getStringToCommissionExcelConverter());
        registry.addConverter(getGalaxieEntryToStringConverter());
        registry.addConverter(getIdToGalaxieEntryConverter());
        registry.addConverter(getStringToGalaxieEntryConverter());
        registry.addConverter(getGalaxieExcelToStringConverter());
        registry.addConverter(getIdToGalaxieExcelConverter());
        registry.addConverter(getStringToGalaxieExcelConverter());
        registry.addConverter(getGalaxieMappingToStringConverter());
        registry.addConverter(getIdToGalaxieMappingConverter());
        registry.addConverter(getStringToGalaxieMappingConverter());
        registry.addConverter(getLogAuthToStringConverter());
        registry.addConverter(getIdToLogAuthConverter());
        registry.addConverter(getStringToLogAuthConverter());
        registry.addConverter(getLogFileToStringConverter());
        registry.addConverter(getIdToLogFileConverter());
        registry.addConverter(getStringToLogFileConverter());
        registry.addConverter(getLogImportCommissionToStringConverter());
        registry.addConverter(getIdToLogImportCommissionConverter());
        registry.addConverter(getStringToLogImportCommissionConverter());
        registry.addConverter(getLogImportGalaxieToStringConverter());
        registry.addConverter(getIdToLogImportGalaxieConverter());
        registry.addConverter(getStringToLogImportGalaxieConverter());
        registry.addConverter(getLogMailToStringConverter());
        registry.addConverter(getIdToLogMailConverter());
        registry.addConverter(getStringToLogMailConverter());
        registry.addConverter(getLogPosteFileToStringConverter());
        registry.addConverter(getIdToLogPosteFileConverter());
        registry.addConverter(getStringToLogPosteFileConverter());
        registry.addConverter(getPosteAPourvoirToStringConverter());
        registry.addConverter(getIdToPosteAPourvoirConverter());
        registry.addConverter(getStringToPosteAPourvoirConverter());
        registry.addConverter(getPosteCandidatureToStringConverter());
        registry.addConverter(getIdToPosteCandidatureConverter());
        registry.addConverter(getStringToPosteCandidatureConverter());
        registry.addConverter(getPosteCandidatureTagToStringConverter());
        registry.addConverter(getIdToPosteCandidatureTagConverter());
        registry.addConverter(getStringToPosteCandidatureTagConverter());
        registry.addConverter(getPosteCandidatureTagValueToStringConverter());
        registry.addConverter(getIdToPosteCandidatureTagValueConverter());
        registry.addConverter(getStringToPosteCandidatureTagValueConverter());
        registry.addConverter(getTemplateFileToStringConverter());
        registry.addConverter(getIdToTemplateFileConverter());
        registry.addConverter(getStringToTemplateFileConverter());
        registry.addConverter(getUserToStringConverter());
        registry.addConverter(getIdToUserConverter());
        registry.addConverter(getStringToUserConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
