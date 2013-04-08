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
package fr.univrouen.poste.web.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;

@RequestMapping("/admin")
@Controller
public class AdminController {

	@RequestMapping
	public String stats(Model uiModel) throws IOException {


		Long posteNumber = PosteAPourvoir.countPosteAPourvoirs();
		Long userNumber = User.countUsers();
		Long adminNumber = User.countAdmins();
		Long supermanagerNumber = User.countSupermanagers();
		Long managerNumber = User.countManagers();
		Long membreNumber = User.countMembres();
		Long candidatNumber = User.countCandidats();
		Long userActifNumber = User.countActifCUsers();
		Long candidatActifNumber = User.countActifCandidats();
		Long posteCandidatureNumber = PosteCandidature.countPosteCandidatures();
		Long posteCandidatureActifNumber = PosteCandidature.countPosteActifCandidatures();
		Long posteCandidatureFileNumber = PosteCandidatureFile.countPosteCandidatureFiles();

		long totalFileSize = 0;
		for (PosteCandidatureFile posteCandidatureFile : PosteCandidatureFile.findAllPosteCandidatureFiles())
			totalFileSize += posteCandidatureFile.getFileSize();
		String totalFileSizeFormatted = PosteCandidatureFile.readableFileSize(totalFileSize);

		String maxFileSize = PosteCandidatureFile.getMaxFileSize();
		
		uiModel.addAttribute("posteNumber", posteNumber);
		uiModel.addAttribute("userNumber", userNumber);
		uiModel.addAttribute("candidatNumber", candidatNumber);
		uiModel.addAttribute("adminNumber", adminNumber);
		uiModel.addAttribute("supermanagerNumber", supermanagerNumber);
		uiModel.addAttribute("managerNumber", managerNumber);
		uiModel.addAttribute("membreNumber", membreNumber);
		uiModel.addAttribute("userActifNumber", userActifNumber);
		uiModel.addAttribute("candidatActifNumber", candidatActifNumber);
		uiModel.addAttribute("posteCandidatureNumber", posteCandidatureNumber);
		uiModel.addAttribute("posteCandidatureActifNumber", posteCandidatureActifNumber);		
		uiModel.addAttribute("posteCandidatureFileNumber", posteCandidatureFileNumber);		
		uiModel.addAttribute("totalFileSizeFormatted", totalFileSizeFormatted);
		uiModel.addAttribute("maxFileSize", maxFileSize);
		
		return "admin";
	}

	@RequestMapping("/zip")
	@Transactional
	public void getZip(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		File tmpFile = File.createTempFile("demat-zip.", ".tmp");
		// we call tmpFile.deleteOnExit so that ths tmp file will be deleted
		// when jvm stops ...
		tmpFile.deleteOnExit();

		FileOutputStream output = new FileOutputStream(tmpFile);
		ZipOutputStream out = new ZipOutputStream(output);

		for (PosteCandidature posteCandidature : PosteCandidature.findAllPosteCandidatures()) {
			String folderName = posteCandidature.getPoste().getNumEmploi().concat("/").concat(posteCandidature.getCandidat().getNumCandidat().concat("/"));
			for (PosteCandidatureFile posteCandidatureFile : posteCandidature.getCandidatureFiles()) {
				String fileName = posteCandidatureFile.getId().toString().concat("-").concat(posteCandidatureFile.getFilename());
				String folderFileName = folderName.concat(fileName);
				out.putNextEntry(new ZipEntry(folderFileName));
				out.write(IOUtils.toByteArray(posteCandidatureFile.getBigFile().getBinaryFile().getBinaryStream()));
				out.closeEntry();
			}
		}
		out.close();
		output.close();

		String contentType = "application/zip";
		int size = (int) tmpFile.length();
		String baseName = "demat.zip";
		InputStream inputStream = new FileInputStream(tmpFile);
		
		response.setContentType(contentType);
		response.setContentLength(size);
		//response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
		FileCopyUtils.copy(inputStream, response.getOutputStream());

		tmpFile.delete();
	}

}
