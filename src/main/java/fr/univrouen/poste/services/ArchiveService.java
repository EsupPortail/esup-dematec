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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
			for (PosteCandidature posteCandidature : posteCandidatures) {
				String folderName = destFolder;
				folderName = folderName.concat(posteCandidature.getPoste().getNumEmploi().concat("/"));	
				folderName = folderName.concat(posteCandidature.getCandidat().getNom().concat("-"));	
				folderName = folderName.concat(posteCandidature.getCandidat().getPrenom().concat("-"));	
				folderName = folderName.concat(posteCandidature.getCandidat().getNumCandidat().concat("/"));
				
				File folder = new File(folderName);
				folder.mkdir();

				for (PosteCandidatureFile posteCandidatureFile : posteCandidature.getCandidatureFiles()) {
					String fileName = posteCandidatureFile.getId().toString().concat("-").concat(posteCandidatureFile.getFilename());
					String folderFileName = folderName.concat(fileName);
					File file = new File(folderFileName);
					
					OutputStream outputStream = new FileOutputStream(file);
					InputStream inputStream = posteCandidatureFile.getBigFile().getBinaryFile().getBinaryStream();					
					IOUtils.copyLarge(inputStream, outputStream);
				}
			}
		
		}
	}
	
}
