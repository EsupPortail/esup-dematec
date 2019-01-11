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
package fr.univrouen.poste.services;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tiles.jsp.taglib.GetAsStringTag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureTag;

@Service
public class CsvService {
	
	private final Logger log = Logger.getLogger(getClass());

	
	@Transactional(readOnly=true)
	public void csvWrite(Writer writer, List<PosteCandidature> posteCandidatures) throws IOException {

		log.info("Generate CSV for " + posteCandidatures.size() + " posteCandidatures");
		
		List<String> header = new ArrayList<String>(Arrays.asList(new String[] { "poste", "nom", "email", "prenom", "galaxie", "recevable", "auditionnable", "vue", "creation", "modification", "gestionnaire", "dateGestion"}));
		List<String> fieldMapping = new ArrayList<String>(header);
		int i = 0;
		for(PosteCandidatureTag tag : PosteCandidatureTag.findAllPosteCandidatureTags()) {
			header.add(tag.getName());
			fieldMapping.add(String.format("tagValues[%d]", i));
			i++;
		}
		
		List<CellProcessor> processors = getProcessors();
		
		ICsvDozerBeanWriter beanWriter =  new CsvDozerBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);
		beanWriter.writeHeader(header.toArray(new String[header.size()]));
		beanWriter.configureBeanMapping(CsvPosteCandidatureMetadataFileBean.class, fieldMapping.toArray(new String[fieldMapping.size()]));
		
		for (PosteCandidature posteCandidature : posteCandidatures) {
			CsvPosteCandidatureMetadataFileBean csvMetadataFileBean = new CsvPosteCandidatureMetadataFileBean(posteCandidature);
			beanWriter.write(csvMetadataFileBean, processors.toArray(new CellProcessor[processors.size()]));
		}
		beanWriter.close();
		
		log.info("Generate CSV OK");
	}
	
	
	public class CsvPosteCandidatureMetadataFileBean {
		
		PosteCandidature posteCandidature;
		
		List<String> tagValues = new ArrayList<String>();

		public CsvPosteCandidatureMetadataFileBean(PosteCandidature posteCandidature) {
			super();
			this.posteCandidature = posteCandidature;
			tagValues = getTagValues();
		}
		
		public String getPoste() {
			return posteCandidature.getPoste().getNumEmploi();
		}

		public String getNom() {
			return posteCandidature.getNom();
		}

		public String getEmail() {
			return posteCandidature.getEmail();
		}

		public String getPrenom() {
			return posteCandidature.getPrenom();
		}
		
		public String getGalaxie() {
			return posteCandidature.getNumCandidat();
		}
		
		public String getRecevable() {
			return posteCandidature.getRecevableEnum().name();
		}
		
		public String getAuditionnable() {
			return posteCandidature.getAuditionnable() ? "true" : "false";
		}
		
		public String getVue() {
			return posteCandidature.getManagerReviewState();
		}
		
		public Date getCreation() {
			return posteCandidature.getCreation();
		}
		
		public Date getModification() {
			return posteCandidature.getModification();
		}
		
		public String getGestionnaire() {
			try {
				return posteCandidature.getManagerReview().getManager().getEmailAddress();
			} catch(NullPointerException npe) {
				return "";
			}
		}
		
		public Date getDateGestion() {
			try {
				return posteCandidature.getManagerReview().getReviewDate();
			} catch(NullPointerException npe) {
				return null;
			}
		}
		
		public List<String> getTagValues() {
			List<String> tagValues = new ArrayList<String>();
			for(PosteCandidatureTag tag : PosteCandidatureTag.findAllPosteCandidatureTags()) {
				String tagValue = "";
				if(posteCandidature.getTags() != null && posteCandidature.getTags().get(tag) != null) {
					tagValue = posteCandidature.getTags().get(tag).getValue();
				}
				tagValues.add(tagValue);
			}
			return tagValues;
		}
		
		
	}

	private static List<CellProcessor> getProcessors() {

		List<CellProcessor> processors = new ArrayList<CellProcessor>(Arrays.asList(new CellProcessor[] {
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				new Optional(new FmtDate("dd/MM/yyyy - HH:mm")),
				new Optional(new FmtDate("dd/MM/yyyy - HH:mm")),
				null,
				new Optional(new FmtDate("dd/MM/yyyy - HH:mm"))
		}));
		
		for(PosteCandidatureTag tag : PosteCandidatureTag.findAllPosteCandidatureTags()) {
			processors.add(null);
		}
		
		return processors;
	}
	
}
