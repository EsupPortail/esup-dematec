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
package fr.univrouen.poste.web.candidat;

import fr.univrouen.poste.dao.*;
import fr.univrouen.poste.domain.*;
import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import fr.univrouen.poste.domain.TemplateFile.TemplateFileType;
import fr.univrouen.poste.services.*;
import fr.univrouen.poste.utils.PdfService;
import fr.univrouen.poste.web.searchcriteria.PosteCandidatureSearchCriteria;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("postecandidatures")
@Controller
@Transactional
public class MyPosteCandidatureController {

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	LogService logService;

	@Resource
	ReturnReceiptService returnReceiptService;

	@Resource
	ManagerReviewLegendColorService managerReviewLegendColorService;

	@Resource
	ZipService zipService;
	
    @Resource
    EmailService emailService;
    
    @Resource
    PdfService pdfService;
    
    @Resource
    TemplateService templateService;
    
    @Resource
    CsvService csvService;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	AppliConfigFileTypeDao appliConfigFileTypeDao;

	@Resource
	MemberReviewFileDao memberReviewFileDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	PosteCandidatureFileDao posteCandidatureFileDao;

	@Resource
	PosteCandidatureTagDao posteCandidatureTagDao;

	@Resource
	TemplateFileDao templateFileDao;

	@Resource
	UserDao userDao;

    @Resource
    BigFileDao bigFileDao;

    @Resource
    ManagerReviewDao managerReviewDao;
    @Autowired
    private AppliConfigService appliConfigService;

	@ModelAttribute("currentUser")
	public User getCurrentUser() {
		String emailAddress = SecurityContextHolder.getContext().getAuthentication().getName();
		return userDao.findUserByEmailAddress(emailAddress);
	}

	@ModelAttribute("command")
	public PosteCandidatureSearchCriteria getSearchCriteria() {
		return new PosteCandidatureSearchCriteria();
	}
	
	@RequestMapping(value = "/{id}/{idFile}")
	@PreAuthorize("hasPermission(#id, 'view')")
	public void downloadCandidatureFile(@PathVariable Long id, @PathVariable Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
			PosteCandidatureFile postecandidatureFile = posteCandidatureFileDao.findPosteCandidatureFile(idFile);
			if (postecandidatureFile == null || !postecandidature.getCandidatureFiles().contains(postecandidatureFile)) {
				logger.warn("Access denied: user {} attempted to access file {} not belonging to candidature {}", request.getRemoteUser(), idFile, id);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ce fichier n'appartient pas à cette candidature");
				return;
			}
			String filename = postecandidatureFile.getFilename();
			Long size = postecandidatureFile.getFileSize();
			String contentType = postecandidatureFile.getContentType();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.setContentLength(size.intValue());
			IOUtils.copy(postecandidatureFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
	
			LocalDateTime currentTime = LocalDateTime.now();
	
			logService.logActionFile(LogService.DOWNLOAD_ACTION, postecandidature, postecandidatureFile, request, currentTime);
		} catch(IOException ioe) {
	        String ip = request.getRemoteAddr();	
			logger.warn("Download IOException, that can be just because the client [" + ip +
					"] canceled the download process : " + ioe.getCause());
		}
	}

	
	@RequestMapping(value = "/{id}", params = {"export"})
	@PreAuthorize("hasPermission(#id, 'review')")
	public String exportCandidatureFiles(@PathVariable Long id, @RequestParam String export, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			
			LocalDateTime currentTime = LocalDateTime.now();
			DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
			String currentTimeFmt = currentTime.format(dateFmt);

			PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
			String fileName = postecandidature.getPoste().getNumEmploi() + "-" + postecandidature.getEmail() + "-" + currentTimeFmt + "." + export;
			DematFileDummy dematFile = new DematFileDummy(fileName, "-");
			
			if("zip".equals(export)) {						
				List<PosteCandidature> postecandidatures = Arrays.asList(postecandidature);
	    		String contentType = "application/zip";
	    		response.setContentType(contentType);
	    		response.setHeader("Content-Disposition","attachment; filename=\"" + fileName +"\"");
	    		zipService.writeZip(postecandidatures, response.getOutputStream());
			} else if("pdf".equals(export)) {
				List<InputStream> pdfFiles = new ArrayList<InputStream>();
				for(PosteCandidatureFile posteCandidatureFile: postecandidature.getCandidatureFiles()) {
					pdfFiles.add(posteCandidatureFile.getBigFile().getBinaryFile().getBinaryStream());
				}
				String contentType = "text/pdf";
				response.setContentType(contentType);
	    		response.setHeader("Content-Disposition","attachment; filename=\"" + fileName +"\"");
				pdfService.mergePdfs(pdfFiles, fileName, response.getOutputStream());	
			} else {
				return "redirect:/postecandidatures/" + id.toString();
			}
	
			logService.logActionFile(LogService.DOWNLOAD_ACTION, postecandidature, dematFile, request, currentTime);
		} catch(Exception e) {
			logger.info("PostCandidature " + id + " can't be exported as " + export, e);
			return "redirect:/postecandidatures/" + id.toString() + "?exportFailed=" + export;
		}
		return null;
	}

	@RequestMapping(value = "/{id}/reviewFile/{idFile}")
	@PreAuthorize("hasPermission(#id, 'review')")
	public void downloadReviewFile(@PathVariable Long id, @PathVariable Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
			MemberReviewFile memberReviewFile = memberReviewFileDao.findMemberReviewFile(idFile);
			if (memberReviewFile == null || !postecandidature.getMemberReviewFiles().contains(memberReviewFile)) {
				logger.warn("Access denied: user {} attempted to access review file {} not belonging to candidature {}", request.getRemoteUser(), idFile, id);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ce fichier n'appartient pas à cette candidature");
				return;
			}
			// byte[] file = postecandidatureFile.getBigFile().getBinaryFile();
			String filename = memberReviewFile.getFilename();
			Long size = memberReviewFile.getFileSize();
			String contentType = memberReviewFile.getContentType();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.setContentLength(size.intValue());
			IOUtils.copy(memberReviewFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
	
			LocalDateTime currentTime = LocalDateTime.now();
			//postecandidature.setModification(currentTime);
	
			logService.logActionFile(LogService.DOWNLOAD_REVIEW_ACTION, postecandidature, memberReviewFile, request, currentTime);
		} catch(IOException ioe) {
	        String ip = request.getRemoteAddr();	
			logger.warn("Download IOException, that can be just because the client [" + ip +
					"] canceled the download process : " + ioe.getCause());
		}
	}
	
	@RequestMapping(value = "/{id}/templateReviewFile/{idFile}")
	@PreAuthorize("hasPermission(#id, 'review')")
	public void downloadTemplateReviewFile(@PathVariable Long id, @PathVariable Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
			
			TemplateFile templateFile = templateFileDao.findTemplateFile(idFile);
			
			String filename = postecandidature.getPoste().getNumEmploi() + 
					"-" + 
					postecandidature.getNumCandidat() + 
					"-" +
					templateFile.getFilename();

			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			
			templateService.generateTemplateFile(templateFile, postecandidature, response.getOutputStream());
			
		} catch(IOException ioe) {
	        String ip = request.getRemoteAddr();	
			logger.warn("Download IOException, that can be just because the client [" + ip +
					"] canceled the download process : " + ioe.getCause());
		}
	}
	
	@RequestMapping(value = "/{id}/delFile/{idFile}")
	@PreAuthorize("hasPermission(#id, 'manage') and hasPermission(#idFile, 'delFile')")
	public String deleteCandidatureFile(@PathVariable Long id, @PathVariable Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException {
		PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
		PosteCandidatureFile postecandidatureFile = posteCandidatureFileDao.findPosteCandidatureFile(idFile);
		if (postecandidatureFile == null || !postecandidature.getCandidatureFiles().contains(postecandidatureFile)) {
			logger.warn("Access denied: user {} attempted to delete file {} not belonging to candidature {}", request.getRemoteUser(), idFile, id);
			return "redirect:/postecandidatures/" + id.toString();
		}
		postecandidature.getCandidatureFiles().remove(postecandidatureFile);

		LocalDateTime currentTime = LocalDateTime.now();
		postecandidature.setModification(currentTime);

		logService.logActionFile(LogService.DELETE_ACTION, postecandidature, postecandidatureFile, request, currentTime);
		return "redirect:/postecandidatures/" + id.toString();
	}

	@RequestMapping(value = "/{id}/delMemberReviewFile/{idFile}")
	@PreAuthorize("hasPermission(#id, 'review') and hasPermission(#idFile, 'delMemberReviewFile')")
	public String delMemberReviewFile(@PathVariable Long id, @PathVariable Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException {
		PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
		MemberReviewFile memberReviewFile = memberReviewFileDao.findMemberReviewFile(idFile);
		if (memberReviewFile == null || !postecandidature.getMemberReviewFiles().contains(memberReviewFile)) {
			logger.warn("Access denied: user {} attempted to delete review file {} not belonging to candidature {}", request.getRemoteUser(), idFile, id);
			return "redirect:/postecandidatures/" + id.toString();
		}
		postecandidature.getMemberReviewFiles().remove(memberReviewFile);
		
		LocalDateTime currentTime = LocalDateTime.now();
		// postecandidature.setModification(currentTime);
		
		logService.logActionFile(LogService.DELETE_REVIEW_ACTION, postecandidature, memberReviewFile, request, currentTime);
		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/addFile", method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasPermission(#id, 'manage')")
	public String addFile(@PathVariable Long id, @Valid PosteCandidatureFile posteCandidatureFile, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {
		if (bindingResult.hasErrors()) {
			logger.warn("Errors on addFile method : {}", bindingResult.getAllErrors());
			return "redirect:/postecandidatures/" + id.toString();
		}

		// get PosteCandidature from id
		PosteCandidature posteCandidature = posteCandidatureDao.findPosteCandidature(id);

		// upload file
		MultipartFile file = posteCandidatureFile.getFile();
		
		// sometimes file is null here, but I don't know how to reproduce this issue ... maybe that can occur only with some specifics browsers ?
		if(file != null) {
			String filename = file.getOriginalFilename();
			
			boolean filenameAlreadyUsed = false;
			for(PosteCandidatureFile pcFile : posteCandidature.getCandidatureFiles()) {
				if(pcFile.getFilename().equals(filename)) {
					filenameAlreadyUsed = true;
					break;
				}
			}		
			
			if(filenameAlreadyUsed) {
				redirectAttributes.addFlashAttribute("filename_already_used", filename);
				logger.warn("Upload Restriction sur '" + filename + "' un fichier de même nom existe déjà pour une candidature de " + posteCandidature.getCandidat().getEmailAddress());
			} else {
				
				Long fileSize = file.getSize();
				
				if(fileSize != 0) {
					String contentType = file.getContentType();
					// cf https://github.com/EsupPortail/esup-dematec/issues/8 - workaround pour éviter mimetype erroné comme application/text-plain:formatted
					contentType = contentType.replaceAll(":.*", "");
					
					logger.info("Try to upload file '" + filename + "' with size=" + fileSize + " and contentType=" + contentType);
					
					Long maxFileMoSize = posteCandidatureFile.getFileType().getCandidatureFileMoSizeMax();
					Long maxFileSize = maxFileMoSize*1024*1024;
					String mimeTypeRegexp = posteCandidatureFile.getFileType().getCandidatureContentTypeRestrictionRegexp();
					String filenameRegexp = posteCandidatureFile.getFileType().getCandidatureFilenameRestrictionRegexp();
					
					boolean sizeRestriction = maxFileSize>0 && fileSize > maxFileSize;
					boolean contentTypeRestriction = !contentType.matches(mimeTypeRegexp);
					boolean filenameRestriction = !filename.matches(filenameRegexp);
					
					if(sizeRestriction || contentTypeRestriction || filenameRestriction) {
						String restriction = sizeRestriction ? "SizeRestriction" : "";
						restriction = contentTypeRestriction || filenameRestriction ? restriction + "ContentTypeRestriction" : restriction;
						redirectAttributes.addFlashAttribute("upload_restricion_size_contentype", restriction);
						logger.info("addFile - upload restriction sur " + filename + "' avec taille=" + fileSize + " et contentType=" + contentType + " pour une candidature de " + posteCandidature.getCandidat().getEmailAddress());
					} else {			
						InputStream inputStream = file.getInputStream();
						//byte[] bytes = IOUtils.toByteArray(inputStream);

					    PosteCandidatureFile newFile = new PosteCandidatureFile();
					    newFile.setFileType(posteCandidatureFile.getFileType());
					    newFile.setFilename(filename);
					    newFile.setFileSize(fileSize);
					    newFile.setContentType(contentType);
						logger.info("Upload and set file in DB with filesize = " + fileSize);
						bigFileDao.setBinaryFileStream(newFile.getBigFile(), inputStream, fileSize);
						bigFileDao.saveBigFile(newFile.getBigFile());
				
						LocalDateTime currentTime = LocalDateTime.now();
						newFile.setSendTime(currentTime);

						posteCandidature.getCandidatureFiles().add(newFile);
				
						posteCandidature.setModification(currentTime);
				
						logService.logActionFile(LogService.UPLOAD_ACTION, posteCandidature, newFile, request, currentTime);
						returnReceiptService.logActionFile(LogService.UPLOAD_ACTION, posteCandidature, newFile, request, currentTime);
						
						pdfService.updateNbPages(newFile.getId());
					}
				}
			}
		} else {
			String userId = SecurityContextHolder.getContext().getAuthentication().getName();
			String ip = request.getRemoteAddr();
			String userAgent = request.getHeader("User-Agent");
			logger.warn(userId + "[" + ip + "] tried to add a 'null file' ... his userAgent is : " + userAgent);
		}

		return "redirect:/postecandidatures/" + id.toString();
	}

	
	@RequestMapping(value = "/{id}/addMemberReviewFile", method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasPermission(#id, 'review')")
	public String addMemberReviewFile(@PathVariable Long id, @Valid MemberReviewFile memberReviewFile, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {
		if (bindingResult.hasErrors()) {
			logger.warn("Errors on addMemberReviewFile method : {}", bindingResult.getAllErrors());
			return "redirect:/postecandidatures/" + id.toString();
		}
		redirectAttributes.asMap().clear();

		// get PosteCandidature from id
		PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);

		// upload file
		MultipartFile file = memberReviewFile.getFile();
		// sometimes file is null here, but I don't know how to reproduce this issue ... maybe that can occur only with some specifics browsers ?
		if(file != null) {
			String filename = file.getOriginalFilename();
			Long fileSize = file.getSize();
			
			boolean filenameAlreadyUsed = false;
			for(MemberReviewFile rFile : postecandidature.getMemberReviewFiles()) {
				if(rFile.getFilename().equals(filename)) {
					filenameAlreadyUsed = true;
					break;
				}
			}		
			
			if(filenameAlreadyUsed) {
				redirectAttributes.addFlashAttribute("filename_already_used", filename);
				logger.info("addMemberReviewFile - upload restriction sur '" + filename + "' un fichier de même nom existe déjà pour une candidature de " + postecandidature.getCandidat().getEmailAddress());
			} else {
			
				if(fileSize != 0) {
					String contentType = file.getContentType();
					// cf https://github.com/EsupPortail/esup-dematec/issues/8 - workaround pour éviter mimetype erroné comme application/text-plain:formatted
					contentType = contentType.replaceAll(":.*", "");
					
					InputStream inputStream = file.getInputStream();
					//byte[] bytes = IOUtils.toByteArray(inputStream);

					MemberReviewFile newFile = new MemberReviewFile();
					newFile.setContentType(memberReviewFile.getContentType());
					newFile.setFilename(filename);
					newFile.setFileSize(fileSize);
					newFile.setContentType(contentType);
					logger.info("Upload and set file in DB with filesize = " + fileSize);
					bigFileDao.setBinaryFileStream(newFile.getBigFile(), inputStream, fileSize);
					bigFileDao.saveBigFile(newFile.getBigFile());
			
					LocalDateTime currentTime = LocalDateTime.now();
					newFile.setSendTime(currentTime);
					
					User currentUser = getCurrentUser();
					newFile.setMember(currentUser);
			
					postecandidature.getMemberReviewFiles().add(newFile);
			
					//postecandidature.setModification(currentTime);
			
					logService.logActionFile(LogService.UPLOAD_REVIEW_ACTION, postecandidature, newFile, request, currentTime);
				}
			}
		} else {
			String userId = SecurityContextHolder.getContext().getAuthentication().getName();
			String ip = request.getRemoteAddr();
			String userAgent = request.getHeader("User-Agent");
			logger.warn(userId + "[" + ip + "] tried to add a 'null file' ... his userAgent is : " + userAgent);
		}

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/modify", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String modifyRecevableCandidature(@PathVariable Long id, @RequestParam(required=true) RecevableEnum recevable, RedirectAttributes redirectAttributes) {
		PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
		
		postecandidature.setRecevableEnum(recevable);

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/auditionnable", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String modifyAuditionnableCandidatureFile(@PathVariable Long id, @RequestParam(required=true) Boolean auditionnable, @RequestParam(required=false) String mailCorps, RedirectAttributes redirectAttributes) {
		PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
		
		mailCorps = mailCorps == null ? "" : mailCorps;
				
		if(auditionnable) {
			String mailTo = postecandidature.getEmail();
    	    String mailFrom = appliConfigDao.getAppliConfig().getMailFrom();
    	    String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();
    	    
    	    String mailMessage = appliConfigDao.getAppliConfig().getTexteEnteteMailCandidatAuditionnable() + 
    	    		"\n" +
    	    		mailCorps + 
    	    		"\n" +
    	    		appliConfigDao.getAppliConfig().getTextePiedpageMailCandidatAuditionnable(); 	    
    	    
    	    mailMessage = mailMessage.replaceAll("@@numEmploi@@", postecandidature.getPoste().getNumEmploi());        
    		    		
    		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
		}
		
		for(PosteCandidatureFile candidatureFile : postecandidature.getCandidatureFiles()) {
			candidatureFile.setWriteable(false);
		}
		
		postecandidature.setAuditionnable(auditionnable);

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	
	@RequestMapping(value = "/{id}/laureat", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String modifyLaureatCandidatureFile(@PathVariable Long id, @RequestParam(required=true) Boolean laureat, @RequestParam(required=false) String mailCorps, RedirectAttributes redirectAttributes) {
		PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
		
		mailCorps = mailCorps == null ? "" : mailCorps;
				
		if(laureat) {
			String mailTo = postecandidature.getEmail();
    	    String mailFrom = appliConfigDao.getAppliConfig().getMailFrom();
    	    String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();
    	    
    	    String mailMessage = mailCorps; 	    
    	    
    	    mailMessage = mailMessage.replaceAll("@@numEmploi@@", postecandidature.getPoste().getNumEmploi());        
    		    		
    		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
		}

		postecandidature.setLaureat(laureat);

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/review", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String modifyReviewCandidature(@PathVariable Long id, @RequestParam(required=true) String reviewStatus, RedirectAttributes redirectAttributes) {
		PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
		
		User currentUser = getCurrentUser();
		
		ManagerReview managerReview = postecandidature.getManagerReview();
		if(managerReview == null) {
			managerReview = new ManagerReview();
			managerReview.setManager(currentUser);
			managerReview.setReviewDate(LocalDateTime.now());
			postecandidature.setManagerReview(managerReview);
			managerReviewDao.saveManagerReview(managerReview);
		} else {	
			managerReview.setManager(currentUser);
			managerReview.setReviewDate(LocalDateTime.now());
		}
		if(ReviewStatusTypes.Vue_incomplet.toString().equals(reviewStatus)) {
			managerReview.setReviewStatus(ReviewStatusTypes.Vue_incomplet);
		}
		if(ReviewStatusTypes.Vue.toString().equals(reviewStatus)) {
			managerReview.setReviewStatus(ReviewStatusTypes.Vue);
		}

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	/*
	 * @RequestMapping(method = RequestMethod.POST, produces = "text/html")
	 * public String create(@Valid PosteCandidature posteCandidature,
	 * BindingResult bindingResult, Model uiModel, HttpServletRequest
	 * httpServletRequest) throws IOException { if (bindingResult.hasErrors()) {
	 * logger.warn(bindingResult.getAllErrors()); return
	 * "redirect:/postecandidaturefiles/create"; } uiModel.asMap().clear();
	 * 
	 * // set current user String email =
	 * SecurityContextHolder.getContext().getAuthentication().getName();
	 * 
	 * Candidat targetCandidat =
	 * Candidat.findCandidatsByEmail(email).getSingleResult();
	 * 
	 * posteCandidature.setCandidat(targetCandidat);
	 * 
	 * // set current date Calendar cal = Calendar.getInstance();
	 * posteCandidature.setCreation(cal.getTime());
	 * posteCandidature.setModification(cal.getTime());
	 * 
	 * posteCandidature.persist(); return "redirect:/postecandidatures/" +
	 * posteCandidature.getId().toString(); }
	 */

	@Transactional
	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
	@PreAuthorize("hasPermission(#id, 'view')")
	public String show(@PathVariable Long id, Model uiModel, HttpServletRequest request) {
		PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
		uiModel.addAttribute("postecandidature", postecandidature);
		PosteCandidatureFile posteCandidatureFile = new PosteCandidatureFile();
		posteCandidatureFile.setFileType(appliConfigFileTypeDao.getDefaultFileType());
		uiModel.addAttribute("posteCandidatureFile", posteCandidatureFile);
		uiModel.addAttribute("fileTypes", appliConfigFileTypeDao.findAllAppliConfigFileTypes().getContent());
		uiModel.addAttribute("texteCandidatAideCandidatureDepot", appliConfigDao.getAppliConfig().getTexteCandidatAideCandidatureDepot());
		
		
	    String mailAuditionnableEntete = appliConfigDao.getAppliConfig().getTexteEnteteMailCandidatAuditionnable();
	    String mailAuditionnablePiedPage = appliConfigDao.getAppliConfig().getTextePiedpageMailCandidatAuditionnable();	    
	    mailAuditionnableEntete = mailAuditionnableEntete.replaceAll("@@numEmploi@@", postecandidature.getPoste().getNumEmploi());  
	    mailAuditionnablePiedPage = mailAuditionnablePiedPage.replaceAll("@@numEmploi@@", postecandidature.getPoste().getNumEmploi());  
	    uiModel.addAttribute("mailAuditionnableEntete", mailAuditionnableEntete);
	    uiModel.addAttribute("mailAuditionnablePiedPage", mailAuditionnablePiedPage);
	    
		uiModel.addAttribute("memberReviewFile", new MemberReviewFile());
		uiModel.addAttribute("supprReview", appliConfigDao.getAppliConfig().getMembreSupprReviewFile());
		
		// Pour phase auditionnable, on ne compte que les fichiers supprimables (writeable).
		int nbFiles = 0;
		for(PosteCandidatureFile f : postecandidature.getCandidatureFiles()) {
			if(f.getWriteable()) {
				nbFiles++;
			}
		}
		
		List<AppliConfigFileType> fileTypes = appliConfigFileTypeDao.findAllAppliConfigFileTypes().getContent();
		List<AppliConfigFileType> fileTypesAvailable = new ArrayList<AppliConfigFileType>(); 
		for(AppliConfigFileType fileType: fileTypes) {
			if(fileType.getCandidatureNbFileMax()<0) {
				fileTypesAvailable.add(fileType);
			} else {
				// le nbre max de fichiers permis pour ce type de pièce est dépassé ?
				int nbFile4Type = 0;
				for(PosteCandidatureFile pcFile: postecandidature.getCandidatureFiles()) {
					if(fileType.equals(pcFile.getFileType())) {
						nbFile4Type++;
					}
				}
				if(nbFile4Type<fileType.getCandidatureNbFileMax()) {
					fileTypesAvailable.add(fileType);
				}
			}
		}
		uiModel.addAttribute("fileTypes", fileTypesAvailable);
		
		List<TemplateFile> templateFiles = templateFileDao.findTemplateFilesByTemplateFileType(TemplateFileType.CANDIDATURE, "id", "asc");
		uiModel.addAttribute("templateFiles", templateFiles);
		
		Boolean isPresident = postecandidature.getPoste().getPresidents() != null && postecandidature.getPoste().getPresidents().contains(getCurrentUser());
		uiModel.addAttribute("isPresident", isPresident);

		boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
		boolean isManager = request.isUserInRole("ROLE_MANAGER");
		uiModel.addAttribute("isAdminOrManager", isAdmin ||  isManager);
		
		uiModel.addAttribute("presidentReportersView", appliConfigDao.getAppliConfig().getPresidentReportersView());
		
		uiModel.addAttribute("laureatEnable", appliConfigDao.getAppliConfig().getLaureatEnable());
		uiModel.addAttribute("texteMailCandidatLaureat", appliConfigDao.getAppliConfig().getTexteMailCandidatLaureat());
		
		uiModel.addAttribute("allPosteCandidatureTag", posteCandidatureTagDao.findAllPosteCandidatureTags());
		
		return "postecandidatures/show";
	}
	
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
    public String delete(@PathVariable Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, RedirectAttributes redirectAttributes) {
    	PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
		posteCandidatureDao.deletePosteCandidature(postecandidature);
        redirectAttributes.addFlashAttribute("page", (page == null) ? "1" : page.toString());
        redirectAttributes.addFlashAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/postecandidatures";
    }

	@RequestMapping(produces = "text/html")
	public String list(@ModelAttribute("command") PosteCandidatureSearchCriteria searchCriteria,
			@PageableDefault(size = 10) Pageable pageable,
			@RequestParam(value = "zip", required = false, defaultValue = "off") Boolean zip,
			@RequestParam(value = "mails", required = false, defaultValue = "off") Boolean mails,
			@RequestParam(value = "csv", required = false, defaultValue = "off") Boolean csv,
			@RequestParam(value = "find", required = false) String find,
			HttpServletResponse response, HttpServletRequest request,
			Model uiModel) throws IOException, SQLException {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String emailAddress = auth.getName();
		User user = userDao.findUserByEmailAddress(emailAddress);
		
		boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
		boolean isManager = request.isUserInRole("ROLE_MANAGER");
		boolean isMembre = request.isUserInRole("ROLE_MEMBRE");
		boolean isCandidat = request.isUserInRole("ROLE_CANDIDAT");

		// Handle exports for admin/manager in finder mode - these need all results without pagination
		if (isAdmin || isManager) {
			if(zip) {
				List<PosteCandidature> postecandidatures = posteCandidatureDao.findAllPosteCandidatures(searchCriteria);
				String contentType = "application/zip";
				String baseName = "demat.zip";
				response.setContentType(contentType);
				response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
				zipService.writeZip(postecandidatures, response.getOutputStream());
				return null;
			} else if(searchCriteria.getTemplateFile() != null) {
				List<PosteCandidature> postecandidatures = posteCandidatureDao.findAllPosteCandidatures(searchCriteria);
				String filename = searchCriteria.getTemplateFile().getFilename();
				response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
				templateService.generateTemplateFile(searchCriteria.getTemplateFile(), postecandidatures, response.getOutputStream());
				return null;
			} else if(mails) {
				List<PosteCandidature> postecandidatures = posteCandidatureDao.findAllPosteCandidatures(searchCriteria);
				Set<String> mailAdresses = new HashSet<String>();
				for(PosteCandidature pc: postecandidatures) {
					mailAdresses.add(pc.getEmail());
				}

				List<String> mailAdressesSorted = new ArrayList<String>(mailAdresses);
				Collections.sort(mailAdressesSorted);
				StringBuffer mailAdressesString = new StringBuffer();
				for(String email: mailAdressesSorted) {
					mailAdressesString.append(email).append("\r\n");
				}

				String contentType = "text/plain";
				String baseName = "emails.txt";
				InputStream inputStream = new ByteArrayInputStream(mailAdressesString.toString().getBytes(StandardCharsets.UTF_8));

				response.setContentType(contentType);
				response.setCharacterEncoding("utf-8");
				response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
				FileCopyUtils.copy(inputStream, response.getOutputStream());

				return null;
			} else if(csv) {
				List<PosteCandidature> posteCandidatures = posteCandidatureDao.findAllPosteCandidatures(searchCriteria);

				String contentType = "text/csv";
				String baseName = "candidatures.csv";

				response.setContentType(contentType);
				response.setCharacterEncoding("utf-8");
				response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
				csvService.csvWrite(response.getWriter(), posteCandidatures);

				return null;
			}
		}

		Page<PosteCandidature> postecandidatures = null;
    	// pagination only for admin / manager users ...
    	if (isAdmin || isManager) {
			postecandidatures = posteCandidatureDao.findPosteCandidatures(searchCriteria, pageable);

			long nbResultsTotal = postecandidatures.getTotalElements();
			uiModel.addAttribute("nbResultsTotal", nbResultsTotal);
			uiModel.addAttribute("maxPages", postecandidatures.getTotalPages());

			uiModel.addAttribute("posteapourvoirs", posteAPourvoirDao.findAllPosteAPourvoirNumEplois());
			uiModel.addAttribute("candidats", userDao.findAllCandidatsIds());
			uiModel.addAttribute("reviewStatusList", Arrays.asList(ReviewStatusTypes.values()));
			
		    String mailAuditionnableEntete = appliConfigDao.getAppliConfig().getTexteEnteteMailCandidatAuditionnable();
		    String mailAuditionnablePiedPage = appliConfigDao.getAppliConfig().getTextePiedpageMailCandidatAuditionnable();	    
		    uiModel.addAttribute("mailAuditionnableEntete", mailAuditionnableEntete);
		    uiModel.addAttribute("mailAuditionnablePiedPage", mailAuditionnablePiedPage);
		    
			uiModel.addAttribute("laureatEnable", appliConfigDao.getAppliConfig().getLaureatEnable());
			uiModel.addAttribute("texteMailCandidatLaureat", appliConfigDao.getAppliConfig().getTexteMailCandidatLaureat());
			
			List<TemplateFile> templateFiles = templateFileDao.findTemplateFilesByTemplateFileType(TemplateFileType.MULTI_CANDIDATURES);
			uiModel.addAttribute("templateFiles", templateFiles);
			
			uiModel.addAttribute("allPosteCandidatureTag", posteCandidatureTagDao.findAllPosteCandidatureTags());

			uiModel.addAttribute("filter", searchCriteria);
		} else if (isCandidat) {
			
			if(!appliConfigDao.getAppliConfig().getCandidatCanSignup()) {
				
				postecandidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(user);
			
				// restrictions si phase auditionnable
		        LocalDateTime currentTime = LocalDateTime.now();
				if(appliConfigDao.getAppliConfig().getDateEndCandidat() != null && currentTime.isAfter(appliConfigDao.getAppliConfig().getDateEndCandidat()) &&
					appliConfigDao.getAppliConfig().getDateEndCandidatActif() != null && currentTime.isAfter(appliConfigDao.getAppliConfig().getDateEndCandidatActif())) {
					for(PosteCandidature postecandidature: posteCandidatureDao.findPosteCandidaturesByCandidat(user)) {
						if(!postecandidature.getAuditionnable() || postecandidature.getPoste().getDateEndCandidatAuditionnable() != null && currentTime.isAfter(postecandidature.getPoste().getDateEndCandidatAuditionnable())) {
							postecandidatures.getContent().remove(postecandidature);
						}
					}
				}
			
			} else {				
				postecandidatures = posteCandidatureDao.findPosteCandidaturesByCandidatAndByDateEndCandidatGreaterThanAndNoAuditionnableOrByDateEndCandidatAuditionnableGreaterThanAndAuditionnable(user, LocalDateTime.now(), pageable);
			}
			
		} else if (isMembre) {
			Set<PosteAPourvoir> membresPostes = new HashSet<PosteAPourvoir>(user.getPostes());
			List<String> numPostes = searchCriteria.getNumEmploiPostes();
			if(numPostes != null && !numPostes.isEmpty()) {
				membresPostes = membresPostes.stream().filter(p->numPostes.contains(p.getNumEmploi())).collect(Collectors.toSet());
			} 
			if(membresPostes.isEmpty()) {
				membresPostes = new HashSet<PosteAPourvoir>(user.getPostes());
			}
			postecandidatures = posteCandidatureDao.findPosteCandidaturesRecevableByPostes(membresPostes, searchCriteria.getAuditionnable(), pageable);
			if(zip) {
				String contentType = "application/zip";
				LocalDateTime currentTime = LocalDateTime.now();
				DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
				String currentTimeFmt = currentTime.format(dateFmt);
	    		String baseName = "demat-" + currentTimeFmt + ".zip";
	    		response.setContentType(contentType);
	    		response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
	    		zipService.writeZip(postecandidatures.getContent(), response.getOutputStream());
	    		logService.logActionFile(LogService.DOWNLOAD_ACTION, postecandidatures.getContent(), request, currentTime);
	    		return null;
			}
			
			for(PosteCandidature pc : postecandidatures) {
				if(pc.getReporters() != null && pc.getReporters().contains(user)) {
					pc.setReporterTag(true);
				}
			}

			List<PosteAPourvoir> membresPostes2Display = new ArrayList<PosteAPourvoir>(user.getPostes());
			
			Collections.sort(membresPostes2Display, new Comparator<PosteAPourvoir>(){
				@Override
				public int compare(PosteAPourvoir p1, PosteAPourvoir p2) {
					return p1.getNumEmploi().compareTo(p2.getNumEmploi());
				}} );
			
			uiModel.addAttribute("membresPostes", membresPostes2Display);
			uiModel.addAttribute("filter", searchCriteria);
		}
		
		uiModel.addAttribute("postecandidatures", postecandidatures);

		uiModel.addAttribute("zip", Boolean.FALSE);
		
		uiModel.addAttribute("texteMembreAideCandidatures", appliConfigDao.getAppliConfig().getTexteMembreAideCandidatures());
		uiModel.addAttribute("texteCandidatAideCandidatures", appliConfigDao.getAppliConfig().getTexteCandidatAideCandidatures());
		
		uiModel.addAttribute("legendColors", managerReviewLegendColorService.getLegendColors());
		uiModel.addAttribute("legendColorMap", managerReviewLegendColorService.getLegendColorsMap());
		uiModel.addAttribute("reporterTagColor", appliConfigService.getCacheColorReporterTag());

		addDateTimeFormatPatterns(uiModel);
		return "postecandidatures/list";
	}

	
    @RequestMapping(value = "/{id}/updateManagerComment", method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String updateManagerComment(@PathVariable Long id, @RequestParam String comment) {
    	PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
    	postecandidature.setManagerComment4Members(comment);
		posteCandidatureDao.savePosteCandidature(postecandidature);
        return "redirect:/postecandidatures/" + id;
    }
    
    @RequestMapping(value = "/{id}/addReporter", method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'manageReporters')")
    public String addReporter(@PathVariable Long id, @RequestParam Long userId, RedirectAttributes redirectAttributes) {
    	PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
    	User user = userDao.findUser(userId);
    	if (user == null || postecandidature.getPoste().getMembres() == null
    			|| !postecandidature.getPoste().getMembres().contains(user)) {
    		logger.warn("Access denied: user {} attempted to add user {} as reporter on candidature {} but that user is not a member of the poste", SecurityContextHolder.getContext().getAuthentication().getName(), userId, id);
    		return "redirect:/postecandidatures/" + id;
    	}
    	postecandidature.getReporters().add(user);
		posteCandidatureDao.savePosteCandidature(postecandidature);
        return "redirect:/postecandidatures/" + id;
    }
    
    @RequestMapping(value = "/{id}/delReporter", method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'manageReporters')")
    public String delReporter(@PathVariable Long id, @RequestParam Long userId) {
    	PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
    	User user = userDao.findUser(userId);
    	if (user == null || postecandidature.getPoste().getMembres() == null
    			|| !postecandidature.getPoste().getMembres().contains(user)) {
    		logger.warn("Access denied: user {} attempted to remove user {} as reporter on candidature {} but that user is not a member of the poste", SecurityContextHolder.getContext().getAuthentication().getName(), userId, id);
    		return "redirect:/postecandidatures/" + id;
    	}
    	postecandidature.getReporters().remove(user);
    	posteCandidatureDao.savePosteCandidature(postecandidature);
        return "redirect:/postecandidatures/" + id;
    }
    
    @RequestMapping(value = "/{id}/updateTags", method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String updateTags(@PathVariable Long id, @Valid PosteCandidatureTagForm posteCandidatureTagForm) {
    	PosteCandidature postecandidature = posteCandidatureDao.findPosteCandidature(id);
    	postecandidature.setTags(posteCandidatureTagForm.getTags());
        return "redirect:/postecandidatures/" + id;
    }
    
    

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("posteCandidature_creation_date_format", "dd/MM/yyyy HH:mm");
        uiModel.addAttribute("posteCandidature_modification_date_format", "dd/MM/yyyy HH:mm");
    }
}

