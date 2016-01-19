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
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import fr.univrouen.poste.domain.PosteCandidature;

@Service
public class CsvService {
	
	private final Logger log = Logger.getLogger(getClass());

	
	@Transactional(readOnly=true)
	public void csvWrite(Writer writer, List<PosteCandidature> posteCandidatures) throws IOException {

		log.info("Generate CSV for " + posteCandidatures.size() + " posteCandidatures");
		
		final String[] header = new String[] { "poste", "nom", "email", "prenom", "galaxie", "recevable", "auditionnable", "vue", "creation", "modification", "gestionnaire", "dateGestion"};
		final CellProcessor[] processors = getProcessors();
		
		ICsvBeanWriter beanWriter =  new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);
		beanWriter.writeHeader(header);
		
		for (PosteCandidature posteCandidature : posteCandidatures) {
			CsvPosteCandidatureMetadataFileBean csvMetadataFileBean = new CsvPosteCandidatureMetadataFileBean(posteCandidature);
			beanWriter.write(csvMetadataFileBean, header, processors);
		}
		beanWriter.close();
		
		log.info("Generate CSV OK");
	}
	
	
	public class CsvPosteCandidatureMetadataFileBean {
		
		PosteCandidature posteCandidature;

		public CsvPosteCandidatureMetadataFileBean(PosteCandidature posteCandidature) {
			super();
			this.posteCandidature = posteCandidature;
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
			return posteCandidature.getRecevable() ? "true" : "false";
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
	}

	private static CellProcessor[] getProcessors() {

		final CellProcessor[] processors = new CellProcessor[] {
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
		};

		return processors;
	}
	
}
