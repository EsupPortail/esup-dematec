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

import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.services.StatBean;
import fr.univrouen.poste.services.StatService;
import fr.univrouen.poste.services.ZipService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RequestMapping("/admin")
@Controller
@Transactional
public class AdminController {
	
	final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	ZipService zipService;
	
	@Resource 
	StatService statService;

	@Resource
	PosteCandidatureDao posteCandidatureDao;
	
	
	@RequestMapping
	public String stats(Model uiModel) {

		StatBean stat = statService.stats();
		uiModel.addAttribute("stat", stat);

		return "admin";
	}
	

	@RequestMapping("/zip")
	@Transactional
	public void getZip(HttpServletResponse response) throws IOException, SQLException {
		
		List<PosteCandidature> postecandidatures = posteCandidatureDao.findAllPosteCandidatures();

		String contentType = "application/zip";
		String baseName = "demat.zip";
		
		response.setContentType(contentType);
		response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
		zipService.writeZip(postecandidatures, response.getOutputStream());
	}

	
	@RequestMapping(value="/chart")
	@Transactional
	public String chart(Model uiModel) {
	
		List<List<String>> countUploadLogFilesByDate = statService.countUploadLogFilesBydate();
		List<List<String>> countSuccessLogAuthsByDate = statService.countSuccessLogAuthsByDate();
		List<List<String>> sumPosteCandidatureFileSizeByDate = statService.sumPosteCandidatureFileSizeByDate();
		List<List<String>> sumMemberReviewFileSizeByDate = statService.sumMemberReviewFileSizeByDate();
		List<List<String>> sumPosteAPourvoirFileSizeByDate = statService.sumPosteAPourvoirFileSizeByDate();

		List<String> uploadStatsLabels = countUploadLogFilesByDate.get(0);
		List<String> uploadStatsValues = countUploadLogFilesByDate.get(1);
		uiModel.addAttribute("uploadStatsLabels", uploadStatsLabels);
		uiModel.addAttribute("uploadStatsValues", uploadStatsValues);


		List<String> authStatsLabels = countSuccessLogAuthsByDate.get(0);
		List<String> authStatsValues = countSuccessLogAuthsByDate.get(1);
		uiModel.addAttribute("authStatsLabels", authStatsLabels);
		uiModel.addAttribute("authStatsValues", authStatsValues);

		List<String> sumFilesSizeStatsLabels = sumPosteCandidatureFileSizeByDate.get(0);
		List<String> sumFilesSizeStatsValues = sumPosteCandidatureFileSizeByDate.get(1);
		uiModel.addAttribute("sumFilesSizeStatsLabels", sumFilesSizeStatsLabels);
		uiModel.addAttribute("sumFilesSizeStatsValues", sumFilesSizeStatsValues);

		List<String> sumMemberReviewFilesSizeStatsLabels = sumMemberReviewFileSizeByDate.get(0);
		List<String> sumMemberReviewFilesSizeStatsValues = sumMemberReviewFileSizeByDate.get(1);
		uiModel.addAttribute("sumMemberReviewFilesSizeStatsLabels", sumMemberReviewFilesSizeStatsLabels);
		uiModel.addAttribute("sumMemberReviewFilesSizeStatsValues", sumMemberReviewFilesSizeStatsValues);

		List<String> sumPosteAPourvoirFilesSizeStatsLabels = sumPosteAPourvoirFileSizeByDate.get(0);
		List<String> sumPosteAPourvoirFilesSizeStatsValues = sumPosteAPourvoirFileSizeByDate.get(1);
		uiModel.addAttribute("sumPosteAPourvoirFilesSizeStatsLabels", sumPosteAPourvoirFilesSizeStatsLabels);
		uiModel.addAttribute("sumPosteAPourvoirFilesSizeStatsValues", sumPosteAPourvoirFilesSizeStatsValues);
		
	    return "admin/chart";
	}  
}
