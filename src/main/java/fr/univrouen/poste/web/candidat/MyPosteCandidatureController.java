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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.domain.DematFileDummy;
import fr.univrouen.poste.domain.ManagerReview;
import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import fr.univrouen.poste.domain.ManagerReviewLegendColor;
import fr.univrouen.poste.domain.MemberReviewFile;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.TemplateFile;
import fr.univrouen.poste.domain.TemplateFile.TemplateFileType;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.provider.DatabaseAuthenticationProvider;
import fr.univrouen.poste.services.CsvService;
import fr.univrouen.poste.services.EmailService;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.services.ReturnReceiptService;
import fr.univrouen.poste.services.TemplateService;
import fr.univrouen.poste.services.ZipService;
import fr.univrouen.poste.utils.PdfService;
import fr.univrouen.poste.web.searchcriteria.PosteCandidatureSearchCriteria;

@RequestMapping("postecandidatures")
@Controller
@RooWebScaffold(path = "postecandidatures", formBackingObject = PosteCandidature.class, create = false, update = false, delete=false)
@Transactional
public class MyPosteCandidatureController {

	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	DatabaseAuthenticationProvider databaseAuthenticationProvider;

	@Autowired
	LogService logService;

	@Autowired
	ReturnReceiptService returnReceiptService;
	
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


	@ModelAttribute("currentUser")
	public User getCurrentUser() {
		String emailAddress = SecurityContextHolder.getContext().getAuthentication().getName();
		User currentUser = User.findUsersByEmailAddress(emailAddress, null, null).getSingleResult();
		return currentUser;
	}

	@ModelAttribute("command")
	public PosteCandidatureSearchCriteria getSearchCriteria() {
		return new PosteCandidatureSearchCriteria();
	}
	
	@RequestMapping(value = "/{id}/{idFile}")
	@PreAuthorize("hasPermission(#id, 'view')")
	public void downloadCandidatureFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
			PosteCandidatureFile postecandidatureFile = PosteCandidatureFile.findPosteCandidatureFile(idFile);
			String filename = postecandidatureFile.getFilename();
			Long size = postecandidatureFile.getFileSize();
			String contentType = postecandidatureFile.getContentType();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.setContentLength(size.intValue());
			IOUtils.copy(postecandidatureFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
	
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
	
			logService.logActionFile(LogService.DOWNLOAD_ACTION, postecandidature, postecandidatureFile, request, currentTime);
		} catch(IOException ioe) {
	        String ip = request.getRemoteAddr();	
			logger.warn("Download IOException, that can be just because the client [" + ip +
					"] canceled the download process : " + ioe.getCause());
		}
	}

	
	@RequestMapping(value = "/{id}", params = {"export"})
	@PreAuthorize("hasPermission(#id, 'review')")
	public String exportCandidatureFiles(@PathVariable("id") Long id, @RequestParam(required=true) String export, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
			SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String currentTimeFmt = dateFmt.format(currentTime);
			
			PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
			String fileName = postecandidature.getPoste().getNumEmploi() + "-" + postecandidature.getEmail() + "-" + currentTimeFmt + "." + export;
			DematFileDummy dematFile = new DematFileDummy(fileName, "-");
			
			if("zip".equals(export)) {						
				List<PosteCandidature> postecandidatures = Arrays.asList(new PosteCandidature[] {postecandidature});
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
	public void downloadReviewFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
			MemberReviewFile memberReviewFile = MemberReviewFile.findMemberReviewFile(idFile);
			// byte[] file = postecandidatureFile.getBigFile().getBinaryFile();
			String filename = memberReviewFile.getFilename();
			Long size = memberReviewFile.getFileSize();
			String contentType = memberReviewFile.getContentType();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.setContentLength(size.intValue());
			IOUtils.copy(memberReviewFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
	
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
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
	public void downloadTemplateReviewFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
			
			TemplateFile templateFile = TemplateFile.findTemplateFile(idFile);
			
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
	public String deleteCandidatureFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		PosteCandidatureFile postecandidatureFile = PosteCandidatureFile.findPosteCandidatureFile(idFile);
		postecandidature.getCandidatureFiles().remove(postecandidatureFile);

		Calendar cal = Calendar.getInstance();
		Date currentTime = cal.getTime();
		postecandidature.setModification(currentTime);

		logService.logActionFile(LogService.DELETE_ACTION, postecandidature, postecandidatureFile, request, currentTime);
		return "redirect:/postecandidatures/" + id.toString();
	}

	@RequestMapping(value = "/{id}/delMemberReviewFile/{idFile}")
	@PreAuthorize("hasPermission(#id, 'review') and hasPermission(#idFile, 'delMemberReviewFile')")
	public String delMemberReviewFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		MemberReviewFile memberReviewFile = MemberReviewFile.findMemberReviewFile(idFile);
		postecandidature.getMemberReviewFiles().remove(memberReviewFile);
		
		Calendar cal = Calendar.getInstance();
		Date currentTime = cal.getTime();
		// postecandidature.setModification(currentTime);
		
		logService.logActionFile(LogService.DELETE_REVIEW_ACTION, postecandidature, memberReviewFile, request, currentTime);
		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/addFile", method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasPermission(#id, 'manage')")
	public String addFile(@PathVariable("id") Long id, @Valid PosteCandidatureFile posteCandidatureFile, BindingResult bindingResult, Model uiModel, HttpServletRequest request) throws IOException {
		if (bindingResult.hasErrors()) {
			logger.warn(bindingResult.getAllErrors());
			return "redirect:/postecandidatures/" + id.toString();
		}
		uiModel.asMap().clear();

		// get PosteCandidature from id
		PosteCandidature posteCandidature = PosteCandidature.findPosteCandidature(id);

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
				uiModel.addAttribute("filename_already_used", filename);
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
						uiModel.addAttribute("upload_restricion_size_contentype", restriction);
						logger.info("addFile - upload restriction sur " + filename + "' avec taille=" + fileSize + " et contentType=" + contentType + " pour une candidature de " + posteCandidature.getCandidat().getEmailAddress());
					} else {			
						InputStream inputStream = file.getInputStream();
						//byte[] bytes = IOUtils.toByteArray(inputStream);
				
						posteCandidatureFile.setFilename(filename);
						posteCandidatureFile.setFileSize(fileSize);
						posteCandidatureFile.setContentType(contentType);
						logger.info("Upload and set file in DB with filesize = " + fileSize);
						posteCandidatureFile.getBigFile().setBinaryFileStream(inputStream, fileSize);
						posteCandidatureFile.getBigFile().persist();
				
						Calendar cal = Calendar.getInstance();
						Date currentTime = cal.getTime();
						posteCandidatureFile.setSendTime(currentTime);
				
						posteCandidature.getCandidatureFiles().add(posteCandidatureFile);
				
						posteCandidature.setModification(currentTime);
				
						posteCandidature.persist();
				
						logService.logActionFile(LogService.UPLOAD_ACTION, posteCandidature, posteCandidatureFile, request, currentTime);
						returnReceiptService.logActionFile(LogService.UPLOAD_ACTION, posteCandidature, posteCandidatureFile, request, currentTime);
						
						pdfService.updateNbPages(posteCandidatureFile.getId());
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
	public String addMemberReviewFile(@PathVariable("id") Long id, @Valid MemberReviewFile memberReviewFile, BindingResult bindingResult, Model uiModel, HttpServletRequest request) throws IOException {
		if (bindingResult.hasErrors()) {
			logger.warn(bindingResult.getAllErrors());
			return "redirect:/postecandidatures/" + id.toString();
		}
		uiModel.asMap().clear();

		// get PosteCandidature from id
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);

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
				uiModel.addAttribute("filename_already_used", filename);
				logger.info("addMemberReviewFile - upload restriction sur '" + filename + "' un fichier de même nom existe déjà pour une candidature de " + postecandidature.getCandidat().getEmailAddress());
			} else {
			
				if(fileSize != 0) {
					String contentType = file.getContentType();
					// cf https://github.com/EsupPortail/esup-dematec/issues/8 - workaround pour éviter mimetype erroné comme application/text-plain:formatted
					contentType = contentType.replaceAll(":.*", "");
					
					InputStream inputStream = file.getInputStream();
					//byte[] bytes = IOUtils.toByteArray(inputStream);
			
					memberReviewFile.setFilename(filename);
					memberReviewFile.setFileSize(fileSize);
					memberReviewFile.setContentType(contentType);
					logger.info("Upload and set file in DB with filesize = " + fileSize);
					memberReviewFile.getBigFile().setBinaryFileStream(inputStream, fileSize);
					memberReviewFile.getBigFile().persist();
			
					Calendar cal = Calendar.getInstance();
					Date currentTime = cal.getTime();
					memberReviewFile.setSendTime(currentTime);
					
					User currentUser = getCurrentUser();
					memberReviewFile.setMember(currentUser);
			
					postecandidature.getMemberReviewFiles().add(memberReviewFile);
			
					//postecandidature.setModification(currentTime);
			
					postecandidature.persist();
			
					logService.logActionFile(LogService.UPLOAD_REVIEW_ACTION, postecandidature, memberReviewFile, request, currentTime);
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
	public String modifyRecevableCandidature(@PathVariable("id") Long id, @RequestParam(required=true) RecevableEnum recevable) {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		
		postecandidature.setRecevableEnum(recevable);

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/auditionnable", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String modifyAuditionnableCandidatureFile(@PathVariable("id") Long id, @RequestParam(required=true) Boolean auditionnable, @RequestParam(required=false) String mailCorps) {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		
		mailCorps = mailCorps == null ? "" : mailCorps;
				
		if(auditionnable) {
			String mailTo = postecandidature.getEmail();
    	    String mailFrom = AppliConfig.getCacheMailFrom();
    	    String mailSubject = AppliConfig.getCacheMailSubject();
    	    
    	    String mailMessage = AppliConfig.getCacheTexteEnteteMailCandidatAuditionnable() + 
    	    		"\n" +
    	    		mailCorps + 
    	    		"\n" +
    	    		AppliConfig.getCacheTextePiedpageMailCandidatAuditionnable(); 	    
    	    
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
	public String modifyLaureatCandidatureFile(@PathVariable("id") Long id, @RequestParam(required=true) Boolean laureat, @RequestParam(required=false) String mailCorps) {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		
		mailCorps = mailCorps == null ? "" : mailCorps;
				
		if(laureat) {
			String mailTo = postecandidature.getEmail();
    	    String mailFrom = AppliConfig.getCacheMailFrom();
    	    String mailSubject = AppliConfig.getCacheMailSubject();
    	    
    	    String mailMessage = mailCorps; 	    
    	    
    	    mailMessage = mailMessage.replaceAll("@@numEmploi@@", postecandidature.getPoste().getNumEmploi());        
    		    		
    		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
		}

		postecandidature.setLaureat(laureat);

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/review", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String modifyReviewCandidature(@PathVariable("id") Long id, @RequestParam(required=true) String reviewStatus) {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		
		User currentUser = getCurrentUser();
		
		ManagerReview managerReview = postecandidature.getManagerReview();
		if(managerReview == null) {
			managerReview = new ManagerReview();
			managerReview.setManager(currentUser);
			managerReview.setReviewDate(new Date());
			postecandidature.setManagerReview(managerReview);
			managerReview.persist();
		} else {	
			managerReview.setManager(currentUser);
			managerReview.setReviewDate(new Date());
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
	@RequestMapping(value = "/{id}", produces = "text/html")
	@PreAuthorize("hasPermission(#id, 'view')")
	public String show(@PathVariable("id") Long id, Model uiModel) {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		uiModel.addAttribute("postecandidature", postecandidature);
		PosteCandidatureFile posteCandidatureFile = new PosteCandidatureFile();
		posteCandidatureFile.setFileType(AppliConfigFileType.getDefaultFileType());
		uiModel.addAttribute("posteCandidatureFile", posteCandidatureFile);
		uiModel.addAttribute("fileTypes", AppliConfigFileType.findAllAppliConfigFileTypes("listIndex, id", "asc"));
		uiModel.addAttribute("texteCandidatAideCandidatureDepot", AppliConfig.getCacheTexteCandidatAideCandidatureDepot());
		
		
	    String mailAuditionnableEntete = AppliConfig.getCacheTexteEnteteMailCandidatAuditionnable();
	    String mailAuditionnablePiedPage = AppliConfig.getCacheTextePiedpageMailCandidatAuditionnable();	    
	    mailAuditionnableEntete = mailAuditionnableEntete.replaceAll("@@numEmploi@@", postecandidature.getPoste().getNumEmploi());  
	    mailAuditionnablePiedPage = mailAuditionnablePiedPage.replaceAll("@@numEmploi@@", postecandidature.getPoste().getNumEmploi());  
	    uiModel.addAttribute("mailAuditionnableEntete", mailAuditionnableEntete);
	    uiModel.addAttribute("mailAuditionnablePiedPage", mailAuditionnablePiedPage);
	    
		uiModel.addAttribute("memberReviewFile", new MemberReviewFile());
		uiModel.addAttribute("supprReview", AppliConfig.getCacheMembreSupprReviewFile());
		
		// Pour phase auditionnable, on ne compte que les fichiers supprimables (writeable).
		int nbFiles = 0;
		for(PosteCandidatureFile f : postecandidature.getCandidatureFiles()) {
			if(f.getWriteable()) {
				nbFiles++;
			}
		}
		
		List<AppliConfigFileType> fileTypes = AppliConfigFileType.findAllAppliConfigFileTypes("listIndex, id", "asc");
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
		
		List<TemplateFile> templateFiles = TemplateFile.findTemplateFilesByTemplateFileType(TemplateFileType.CANDIDATURE, "id", "asc").getResultList();
		uiModel.addAttribute("templateFiles", templateFiles);
		
		Boolean isPresident = postecandidature.getPoste().getPresidents() != null && postecandidature.getPoste().getPresidents().contains(getCurrentUser());
		uiModel.addAttribute("isPresident", isPresident);
		
		uiModel.addAttribute("presidentReportersView", AppliConfig.getCachePresidentReportersView());
		
		uiModel.addAttribute("laureatEnable", AppliConfig.getCacheLaureatEnable());
		uiModel.addAttribute("texteMailCandidatLaureat", AppliConfig.getCacheTexteMailCandidatLaureat());
		
		return "postecandidatures/show";
	}
	
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
    	postecandidature.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/postecandidatures";
    }

	@RequestMapping(produces = "text/html")
	public String list(@ModelAttribute("command") PosteCandidatureSearchCriteria searchCriteria, BindingResult bindResult,
			@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, 
			@RequestParam(value = "zip", required = false, defaultValue = "off") Boolean zip, HttpServletResponse response, HttpServletRequest request, 
			Model uiModel) throws IOException, SQLException {

		// uiModel.addAttribute("users", User.findUserEntries(firstResult,
		// sizeNo));
		
		List<PosteCandidature> postecandidatures = null;

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		String emailAddress = auth.getName();
		User user = User.findUsersByEmailAddress(emailAddress, null, null).getSingleResult();
		
		boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
		boolean isManager = request.isUserInRole("ROLE_MANAGER");
		boolean isSuperManager = isManager || request.isUserInRole("ROLE_SUPER_MANAGER");
		boolean isMembre = request.isUserInRole("ROLE_MEMBRE");
		boolean isCandidat = request.isUserInRole("ROLE_CANDIDAT");

		uiModel.addAttribute("sortFieldName", sortFieldName);
		uiModel.addAttribute("sortOrder", sortOrder);
		
    	if(sortFieldName == null) 
            sortFieldName = "o.poste.numEmploi,o.candidat.nom";   
		if("poste".equals(sortFieldName))
    		sortFieldName = "poste.numEmploi";
    	if("nom".equals(sortFieldName))
    		sortFieldName = "candidat.nom";
    	if("email".equals(sortFieldName))
    		sortFieldName = "candidat.emailAddress";
		if("numCandidat".equals(sortFieldName))
			sortFieldName = "candidat.numCandidat";
    	if("managerReviewState".equals(sortFieldName))
    		sortFieldName = "managerReview.reviewStatus";
    	if("galaxieEntryEtatDossier".equals(sortFieldName))
    		sortFieldName = "galaxieEntry.etatDossier";
    		
    	// pagination only for admin / manager users ...
    	if (isAdmin || isManager) {
    		
			if (page != null || size != null) {
				int sizeNo = size == null ? 10 : size.intValue();
				int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
				long nbResultsTotal = PosteCandidature.countPosteCandidatures();
				uiModel.addAttribute("nbResultsTotal", nbResultsTotal);
				float nrOfPages = (float) nbResultsTotal / sizeNo;
				uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
				postecandidatures = PosteCandidature.findPosteCandidatureEntries(firstResult, sizeNo, sortFieldName, sortOrder);
			} else {
				postecandidatures = PosteCandidature.findAllPosteCandidatures(sortFieldName, sortOrder);
				uiModel.addAttribute("nbResultsTotal", postecandidatures.size());
			}
			
			uiModel.addAttribute("posteapourvoirs", PosteAPourvoir.findAllPosteAPourvoirNumEplois());
			uiModel.addAttribute("candidats", User.findAllCandidatsIds());
			uiModel.addAttribute("reviewStatusList", Arrays.asList(ReviewStatusTypes.values()));
			
		    String mailAuditionnableEntete = AppliConfig.getCacheTexteEnteteMailCandidatAuditionnable();
		    String mailAuditionnablePiedPage = AppliConfig.getCacheTextePiedpageMailCandidatAuditionnable();	    
		    uiModel.addAttribute("mailAuditionnableEntete", mailAuditionnableEntete);
		    uiModel.addAttribute("mailAuditionnablePiedPage", mailAuditionnablePiedPage);
		    
			uiModel.addAttribute("laureatEnable", AppliConfig.getCacheLaureatEnable());
			uiModel.addAttribute("texteMailCandidatLaureat", AppliConfig.getCacheTexteMailCandidatLaureat());
			
			List<TemplateFile> templateFiles = TemplateFile.findTemplateFilesByTemplateFileType(TemplateFileType.MULTI_CANDIDATURES).getResultList();
			uiModel.addAttribute("templateFiles", templateFiles);
		}

		else if (isCandidat) {
			
			if(!AppliConfig.getCacheCandidatCanSignup()) {
				
				postecandidatures = new ArrayList<PosteCandidature>(PosteCandidature.findPosteCandidaturesByCandidat(user, null, null).getResultList());
			
				// restrictions si phase auditionnable
		        Date currentTime = new Date();     
				if(currentTime.compareTo(AppliConfig.getCacheDateEndCandidat()) > 0 && 
					currentTime.compareTo(AppliConfig.getCacheDateEndCandidatActif()) > 0) { 
					for(PosteCandidature postecandidature: PosteCandidature.findPosteCandidaturesByCandidat(user, null, null).getResultList()) {
						if(!postecandidature.getAuditionnable() || postecandidature.getPoste().getDateEndCandidatAuditionnable() != null && currentTime.compareTo(postecandidature.getPoste().getDateEndCandidatAuditionnable()) > 0) {
							postecandidatures.remove(postecandidature);
						}
					}
				}
			
			} else {				
				postecandidatures = new ArrayList<PosteCandidature>(PosteCandidature.findPosteCandidaturesByCandidatAndByDateEndCandidatGreaterThanAndNoAuditionnableOrByDateEndCandidatAuditionnableGreaterThanAndAuditionnable(user, new Date()).getResultList());					
			}
			
		}

		else if (isMembre) {
			Set<PosteAPourvoir> membresPostes = new HashSet<PosteAPourvoir>(user.getPostes());
			List<PosteAPourvoir> postes = searchCriteria.getPostes();
			if(postes != null && !postes.isEmpty()) {
				membresPostes.retainAll(postes);
	    		uiModel.addAttribute("finderview", true);
	    		uiModel.addAttribute("command", searchCriteria);
			} 
			if(membresPostes.isEmpty()) {
				membresPostes = new HashSet<PosteAPourvoir>(user.getPostes());
			}
			postecandidatures = PosteCandidature.findPosteCandidaturesRecevableByPostes(membresPostes, searchCriteria.getAuditionnable(), sortFieldName, sortOrder).getResultList();		
			if(zip) {
	    		String contentType = "application/zip";
	    		Calendar cal = Calendar.getInstance();
	    		Date currentTime = cal.getTime();
				SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				String currentTimeFmt = dateFmt.format(currentTime);
	    		String baseName = "demat-" + currentTimeFmt + ".zip";
	    		response.setContentType(contentType);
	    		response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
	    		zipService.writeZip(postecandidatures, response.getOutputStream());
	    		logService.logActionFile(LogService.DOWNLOAD_ACTION, postecandidatures, request, currentTime);
	    		return null;
			}
			
			for(PosteCandidature pc : postecandidatures) {
				if(pc.getReporters() != null && pc.getReporters().contains(user)) {
					pc.setReporterTag(true);
				}
			}
			
			uiModel.addAttribute("nbResultsTotal", postecandidatures.size());
			List<PosteAPourvoir> membresPostes2Display = new ArrayList<PosteAPourvoir>(user.getPostes());
			
			Collections.sort(membresPostes2Display, new Comparator<PosteAPourvoir>(){
				@Override
				public int compare(PosteAPourvoir p1, PosteAPourvoir p2) {
					return p1.getNumEmploi().compareTo(p2.getNumEmploi());
				}} );
			
			uiModel.addAttribute("membresPostes", membresPostes2Display);
		}
		
		uiModel.addAttribute("postecandidatures", postecandidatures);

		uiModel.addAttribute("zip", new Boolean(false));
		
		uiModel.addAttribute("texteMembreAideCandidatures", AppliConfig.getCacheTexteMembreAideCandidatures());
		uiModel.addAttribute("texteCandidatAideCandidatures", AppliConfig.getCacheTexteCandidatAideCandidatures());
		
		uiModel.addAttribute("legendColors", ManagerReviewLegendColor.getLegendColors());

		addDateTimeFormatPatterns(uiModel);
		return "postecandidatures/list";
	}
	
    @RequestMapping(params = "find=ByMultiParams", method = RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String findPosteCandidatures(HttpServletRequest request, 
    		HttpServletResponse response, 
    		@ModelAttribute("command") PosteCandidatureSearchCriteria searchCriteria, BindingResult bindResult,
    		@RequestParam(defaultValue="off", required=false) Boolean zip,
    		@RequestParam(defaultValue="off", required=false) Boolean mails,
    		@RequestParam(defaultValue="off", required=false) Boolean csv,
    		@RequestParam(required = false) Integer page, 
    		@RequestParam(required = false) Integer size, 
    		@RequestParam(required = false) String sortFieldName, 
    		@RequestParam(required = false) String sortOrder,
    		Model uiModel) throws IOException, SQLException {

    	uiModel.addAttribute("sortFieldName", sortFieldName);
		uiModel.addAttribute("sortOrder", sortOrder);

		if("poste".equals(sortFieldName))
    		sortFieldName = "poste.numEmploi";
    	if("nom".equals(sortFieldName))
    		sortFieldName = "candidat.nom";
    	if("email".equals(sortFieldName))
    		sortFieldName = "candidat.emailAddress";
		if("numCandidat".equals(sortFieldName))
			sortFieldName = "candidat.numCandidat";
    	if("managerReviewState".equals(sortFieldName))
    		sortFieldName = "managerReview.reviewStatus";
    	if("galaxieEntryEtatDossier".equals(sortFieldName))
    		sortFieldName = "galaxieEntry.etatDossier";
    	
    	if(zip) {
    		List<PosteCandidature> postecandidatures = PosteCandidature.findPostesCandidatures(searchCriteria, sortFieldName, sortOrder).getResultList();
    		String contentType = "application/zip";
    		String baseName = "demat.zip";
    		response.setContentType(contentType);
    		response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
    		zipService.writeZip(postecandidatures, response.getOutputStream());
    		return null; 
    	} else if(searchCriteria.getTemplateFile() != null) {
    		List<PosteCandidature> postecandidatures = PosteCandidature.findPostesCandidatures(searchCriteria, sortFieldName, sortOrder).getResultList();
			String filename = searchCriteria.getTemplateFile().getFilename();
			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    		templateService.generateTemplateFile(searchCriteria.getTemplateFile(), postecandidatures, response.getOutputStream());
    		return null; 
    	} else {
    		
    		if(mails) {
    			
    			List<PosteCandidature> postecandidatures = PosteCandidature.findPostesCandidatures(searchCriteria, null, null).getResultList();
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
    			
    			List<PosteCandidature> posteCandidatures = PosteCandidature.findPostesCandidatures(searchCriteria, null, null).getResultList();
    			
        		String contentType = "text/csv";
        		String baseName = "candidatures.csv";

        		response.setContentType(contentType);
        		response.setCharacterEncoding("utf-8");
        		response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
        		csvService.csvWrite(response.getWriter(), posteCandidatures);

        		return null; 
    			
    		} else {
    	    	
	    		if (page != null || size != null) {
	                int sizeNo = size == null ? 10 : size.intValue();
	                final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
	                uiModel.addAttribute("postecandidatures", PosteCandidature.findPostesCandidatures(searchCriteria, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
	                long nbResultsTotal = PosteCandidature.countFindPosteCandidatures(searchCriteria);
	                uiModel.addAttribute("nbResultsTotal", nbResultsTotal);
	                float nrOfPages = (float) nbResultsTotal / sizeNo;
	                uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	            } else {
	            	List<PosteCandidature> postecandidatures = PosteCandidature.findPostesCandidatures(searchCriteria, sortFieldName, sortOrder).getResultList();
	                uiModel.addAttribute("postecandidatures", postecandidatures);
	                uiModel.addAttribute("nbResultsTotal", postecandidatures.size());
	            }
	    		
	    		uiModel.addAttribute("texteMembreAideCandidatures", AppliConfig.getCacheTexteMembreAideCandidatures());
	    		uiModel.addAttribute("texteCandidatAideCandidatures", AppliConfig.getCacheTexteCandidatAideCandidatures());
	    		
	    		uiModel.addAttribute("legendColors", ManagerReviewLegendColor.getLegendColors());
	    		
				uiModel.addAttribute("posteapourvoirs", PosteAPourvoir.findAllPosteAPourvoirNumEplois());
				uiModel.addAttribute("candidats", User.findAllCandidatsIds());
				uiModel.addAttribute("reviewStatusList", Arrays.asList(ReviewStatusTypes.values()));
				
	    		uiModel.addAttribute("command", searchCriteria);
	    		uiModel.addAttribute("finderview", true);
	    		
			    String mailAuditionnableEntete = AppliConfig.getCacheTexteEnteteMailCandidatAuditionnable();
			    String mailAuditionnablePiedPage = AppliConfig.getCacheTextePiedpageMailCandidatAuditionnable();	    
			    uiModel.addAttribute("mailAuditionnableEntete", mailAuditionnableEntete);
			    uiModel.addAttribute("mailAuditionnablePiedPage", mailAuditionnablePiedPage);
			    
				uiModel.addAttribute("laureatEnable", AppliConfig.getCacheLaureatEnable());
				uiModel.addAttribute("texteMailCandidatLaureat", AppliConfig.getCacheTexteMailCandidatLaureat());
	    		
				List<TemplateFile> templateFiles = TemplateFile.findTemplateFilesByTemplateFileType(TemplateFileType.MULTI_CANDIDATURES).getResultList();
				uiModel.addAttribute("templateFiles", templateFiles);
				
	            addDateTimeFormatPatterns(uiModel);       
	            return "postecandidatures/list";           
    		}
    	}
    }    
	
    @RequestMapping(value = "/{id}/updateManagerComment", method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String updateManagerComment(@PathVariable("id") Long id, @RequestParam String comment, Model uiModel) {
    	PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
    	postecandidature.setManagerComment4Members(comment);
    	postecandidature.merge();
        uiModel.asMap().clear();
        return "redirect:/postecandidatures/" + id;
    }
    
    @RequestMapping(value = "/{id}/addReporter", method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'manageReporters')")
    public String addReporter(@PathVariable("id") Long id, @RequestParam Long userId, Model uiModel) {
    	PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
    	User user = User.findUser(userId);
    	postecandidature.getReporters().add(user);
    	postecandidature.merge();
        uiModel.asMap().clear();
        return "redirect:/postecandidatures/" + id;
    }
    
    @RequestMapping(value = "/{id}/delReporter", method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'manageReporters')")
    public String delReporter(@PathVariable("id") Long id, @RequestParam Long userId, Model uiModel) {
    	PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
    	User user = User.findUser(userId);
    	postecandidature.getReporters().remove(user);
    	postecandidature.merge();
        uiModel.asMap().clear();
        return "redirect:/postecandidatures/" + id;
    }
    
}
