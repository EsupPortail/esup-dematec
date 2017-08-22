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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RequestMapping;

import flexjson.JSONSerializer;
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
		
		List<PosteCandidature> postecandidatures = PosteCandidature.findAllPosteCandidatures();

		String contentType = "application/zip";
		String baseName = "demat.zip";
		
		response.setContentType(contentType);
		response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
		zipService.writeZip(postecandidatures, response.getOutputStream());
	}

	
	@RequestMapping(value="/chart")
	@Transactional
	public String chart(Model uiModel) {
	
		List<List<Object>> countUploadLogFilesByDate = statService.countUploadLogFilesBydate();
		List<List<Object>> countSuccessLogAuthsByDate = statService.countSuccessLogAuthsByDate();
		List<List<Object>> sumPosteCandidatureFileSizeByDate = statService.sumPosteCandidatureFileSizeByDate();
		List<List<Object>> sumMemberReviewFileSizeByDate = statService.sumMemberReviewFileSizeByDate();
		List<List<Object>> sumPosteAPourvoirFileSizeByDate = statService.sumPosteAPourvoirFileSizeByDate();
		
		JSONSerializer serializer = new JSONSerializer();
		
		String uploadStatsLabels = serializer.deepSerialize(countUploadLogFilesByDate.get(0));
		String uploadStatsValues = serializer.deepSerialize(countUploadLogFilesByDate.get(1));			
		uiModel.addAttribute("uploadStatsLabels", uploadStatsLabels);
		uiModel.addAttribute("uploadStatsValues", uploadStatsValues);
		
		String authStatsLabels = serializer.deepSerialize(countSuccessLogAuthsByDate.get(0));
		String authStatsValues = serializer.deepSerialize(countSuccessLogAuthsByDate.get(1));			
		uiModel.addAttribute("authStatsLabels", authStatsLabels);
		uiModel.addAttribute("authStatsValues", authStatsValues);
	
		String sumFilesSizeStatsLabels = serializer.deepSerialize(sumPosteCandidatureFileSizeByDate.get(0));
		String sumFilesSizeStatsValues = serializer.deepSerialize(sumPosteCandidatureFileSizeByDate.get(1));			
		uiModel.addAttribute("sumFilesSizeStatsLabels", sumFilesSizeStatsLabels);
		uiModel.addAttribute("sumFilesSizeStatsValues", sumFilesSizeStatsValues);
		
		String sumMemberReviewFilesSizeStatsLabels = serializer.deepSerialize(sumMemberReviewFileSizeByDate.get(0));
		String sumMemberReviewFilesSizeStatsValues = serializer.deepSerialize(sumMemberReviewFileSizeByDate.get(1));			
		uiModel.addAttribute("sumMemberReviewFilesSizeStatsLabels", sumMemberReviewFilesSizeStatsLabels);
		uiModel.addAttribute("sumMemberReviewFilesSizeStatsValues", sumMemberReviewFilesSizeStatsValues);
		
		String sumPosteAPourvoirFilesSizeStatsLabels = serializer.deepSerialize(sumPosteAPourvoirFileSizeByDate.get(0));
		String sumPosteAPourvoirFilesSizeStatsValues = serializer.deepSerialize(sumPosteAPourvoirFileSizeByDate.get(1));			
		uiModel.addAttribute("sumPosteAPourvoirFilesSizeStatsLabels", sumPosteAPourvoirFilesSizeStatsLabels);
		uiModel.addAttribute("sumPosteAPourvoirFilesSizeStatsValues", sumPosteAPourvoirFilesSizeStatsValues);
		
	    return "admin/chart";
	}  
}
