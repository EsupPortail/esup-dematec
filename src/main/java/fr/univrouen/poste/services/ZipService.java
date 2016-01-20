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
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;

@Service
public class ZipService {
	
	private final Logger logger = Logger.getLogger(getClass());


	public void writeZip(List<PosteCandidature> posteCandidatures, OutputStream destStream) throws IOException, SQLException {

		ZipOutputStream out = new ZipOutputStream(destStream);

		for (PosteCandidature posteCandidature : posteCandidatures) {
			String folderName = posteCandidature.getPoste().getNumEmploi().concat("/");	
			folderName = folderName.concat(posteCandidature.getCandidat().getNom().concat("-"));	
			folderName = folderName.concat(posteCandidature.getCandidat().getPrenom().concat("-"));	
			folderName = folderName.concat(posteCandidature.getCandidat().getNumCandidat().concat("/"));
			for (PosteCandidatureFile posteCandidatureFile : posteCandidature.getCandidatureFiles()) {
				String fileName = posteCandidatureFile.getId().toString().concat("-").concat(posteCandidatureFile.getFilename());
				String folderFileName = folderName.concat(fileName);
				out.putNextEntry(new ZipEntry(folderFileName));
				out.write(IOUtils.toByteArray(posteCandidatureFile.getBigFile().getBinaryFile().getBinaryStream()));
				out.closeEntry();
			}
		}
		out.close();
	}
	
}
