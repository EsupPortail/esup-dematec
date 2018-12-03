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

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;

/**
 * A central place to register application converters and formatters. 
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	protected void installFormatters(FormatterRegistry registry) {
		// not used
	}
	
	public Converter<PosteAPourvoir, String> getPosteAPourvoirToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.PosteAPourvoir, java.lang.String>() {
            public String convert(PosteAPourvoir posteAPourvoir) {
                return new StringBuilder().append(posteAPourvoir.getNumEmploi()).toString();
            }
        };
    }

	public Converter<PosteCandidature, String> getPosteCandidatureToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.PosteCandidature, java.lang.String>() {
            public String convert(PosteCandidature posteCandidature) {
                return new StringBuilder().append(posteCandidature.getCandidat().getEmailAddress()).append(" [").append(posteCandidature.getPoste().getNumEmploi()).append("]").toString();
            }
        };
    }

	public Converter<User, String> getUserToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<fr.univrouen.poste.domain.User, java.lang.String>() {
            public String convert(User user) {
                return new StringBuilder().append(user.getEmailAddress()).toString();
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

}
