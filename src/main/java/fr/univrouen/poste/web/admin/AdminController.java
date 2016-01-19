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
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.services.StatBean;
import fr.univrouen.poste.services.StatService;
import fr.univrouen.poste.services.ZipService;

@RequestMapping("/admin")
@Controller
@Transactional
public class AdminController {
	
	private final Logger log = Logger.getLogger(getClass());
	
	@Resource
	ZipService zipService;
	
	@Resource 
	StatService statService;
	
	
	@RequestMapping
	public String stats(Model uiModel) {

		StatBean stat = statService.stats();
		uiModel.addAttribute("stat", stat);

		return "admin";
	}
	

	@RequestMapping("/zip")
	@Transactional
	public void getZip(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		
		File tmpFile = zipService.getZip(PosteCandidature.findAllPosteCandidatures());

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
