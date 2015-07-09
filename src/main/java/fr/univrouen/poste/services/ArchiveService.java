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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import fr.univrouen.poste.domain.MemberReviewFile;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;

@Service
public class ArchiveService {
	
	private final Logger logger = Logger.getLogger(getClass());

	
	@Transactional(readOnly=true)
	public void archive(String destFolder) throws IOException, SQLException {
		
		List<PosteCandidature> posteCandidatures = PosteCandidature.findAllPosteCandidatures();
		
		File destFolderFile = new File(destFolder);
		if(destFolderFile.mkdir()) {
	
			final String[] header = new String[] { "id", "filename", "sendDate", "owner" };
			final CellProcessor[] processors = getProcessors();
			
			for (PosteCandidature posteCandidature : posteCandidatures) {
				String folderName = destFolder.concat("/");
				folderName = folderName.concat(posteCandidature.getPoste().getNumEmploi()).concat("/");	
				
				File folder = new File(folderName);
				folder.mkdir();
				
				folderName = folderName.concat(posteCandidature.getRecevable() ? "Recevable" : "Non_Recevable").concat("/");	
				folder = new File(folderName);
				folder.mkdir();
				
				if(posteCandidature.getAuditionnable() != null) {
					folderName = folderName.concat(posteCandidature.getAuditionnable() ? "Auditionnable" : "Non_Auditionnable").concat("/");	
					folder = new File(folderName);
					folder.mkdir();			
				}
				
				folderName = folderName.concat(posteCandidature.getCandidat().getNom()).concat("-");	
				folderName = folderName.concat(posteCandidature.getCandidat().getPrenom()).concat("-");	
				folderName = folderName.concat(posteCandidature.getCandidat().getNumCandidat()).concat("/");
				
				folder = new File(folderName);
				folder.mkdir();
				
				ICsvBeanWriter beanWriter =  new CsvBeanWriter(new FileWriter(folderName.concat("metadata.csv")), CsvPreference.STANDARD_PREFERENCE);
				beanWriter.writeHeader(header);
				for (PosteCandidatureFile posteCandidatureFile : posteCandidature.getCandidatureFiles()) {
					String fileName = posteCandidatureFile.getId().toString().concat("-").concat(posteCandidatureFile.getFilename());
					String folderFileName = folderName.concat(fileName);
					File file = new File(folderFileName);
					file.createNewFile();
					
					OutputStream outputStream = new FileOutputStream(file);
					InputStream inputStream = posteCandidatureFile.getBigFile().getBinaryFile().getBinaryStream();					
					IOUtils.copyLarge(inputStream, outputStream);
					
					ArchiveMetadataFileBean archiveMetadataFileBean = new ArchiveMetadataFileBean(fileName, posteCandidatureFile.getFilename(), posteCandidatureFile.getSendTime(), posteCandidature.getCandidat().getEmailAddress());
					beanWriter.write(archiveMetadataFileBean, header, processors);
				}
				beanWriter.close();
				
				if(!posteCandidature.getMemberReviewFiles().isEmpty()) {
					folderName = folderName.concat("Rapports_commission").concat("/");	
					folder = new File(folderName);
					folder.mkdir();
					
					beanWriter =  new CsvBeanWriter(new FileWriter(folderName.concat("metadata.csv")), CsvPreference.STANDARD_PREFERENCE);
					beanWriter.writeHeader(header);
					for (MemberReviewFile memberReviewFile : posteCandidature.getMemberReviewFiles()) {
						String fileName = memberReviewFile.getId().toString().concat("-").concat(memberReviewFile.getFilename());
						String folderFileName = folderName.concat(fileName);
						File file = new File(folderFileName);
						file.createNewFile();
						
						OutputStream outputStream = new FileOutputStream(file);
						InputStream inputStream = memberReviewFile.getBigFile().getBinaryFile().getBinaryStream();					
						IOUtils.copyLarge(inputStream, outputStream);
						
						ArchiveMetadataFileBean archiveMetadataFileBean = new ArchiveMetadataFileBean(fileName, memberReviewFile.getFilename(), memberReviewFile.getSendTime(), memberReviewFile.getMember().getEmailAddress());
						beanWriter.write(archiveMetadataFileBean, header, processors);
					}
					beanWriter.close();
				}
				
			}
		
		} else {
			logger.error("Le répertoire " + destFolder + " n'a pas pu être créé. Vérifiez qu'il n'existe pas déjà, que l'application a bien les droits de le créer, etc." );
		}
	}
	
	
	public class ArchiveMetadataFileBean {
		
		String id;
		
		String filename;
		
		Date sendDate;
		
		String owner;

		public ArchiveMetadataFileBean(String id, String filename,
				Date sendDate, String owner) {
			super();
			this.id = id;
			this.filename = filename;
			this.sendDate = sendDate;
			this.owner = owner;
		}

		public String getId() {
			return id;
		}

		public String getFilename() {
			return filename;
		}

		public Date getSendDate() {
			return sendDate;
		}

		public String getOwner() {
			return owner;
		}
		
	}

	private static CellProcessor[] getProcessors() {

		final CellProcessor[] processors = new CellProcessor[] { 
				new UniqueHashCode(), // id (must be unique)
				new NotNull(), // filename
				new FmtDate("dd/MM/yyyy - HH:mm"), // sendTime
				new NotNull(), // owner
		};

		return processors;
	}
	
}
