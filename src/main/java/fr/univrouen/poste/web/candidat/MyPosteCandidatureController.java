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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
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
import fr.univrouen.poste.domain.ManagerReview;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.provider.DatabaseAuthenticationProvider;
import fr.univrouen.poste.services.EmailService;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.services.ReturnReceiptService;
import fr.univrouen.poste.services.ZipService;

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
	
	@ModelAttribute("posteapourvoirs")
	public List<PosteAPourvoir> getPosteAPourvoirs() {
		return PosteAPourvoir.findAllPosteAPourvoirs();
	}

	@ModelAttribute("candidats")
	public List<User> getCandidats() {
		return User.findAllCandidats(null, null).getResultList();
	}

	@RequestMapping(value = "/{id}/{idFile}")
	@PreAuthorize("hasPermission(#id, 'view')")
	public void downloadCandidatureFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
			PosteCandidatureFile postecandidatureFile = PosteCandidatureFile.findPosteCandidatureFile(idFile);
			// byte[] file = postecandidatureFile.getBigFile().getBinaryFile();
			String filename = postecandidatureFile.getFilename();
			Long size = postecandidatureFile.getFileSize();
			String contentType = postecandidatureFile.getContentType();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.setContentLength(size.intValue());
			IOUtils.copy(postecandidatureFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
	
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
			//postecandidature.setModification(currentTime);
	
			logService.logActionFile(LogService.DOWNLOAD_ACTION, postecandidature, postecandidatureFile, request, currentTime);
		} catch(IOException ioe) {
	        String ip = request.getRemoteAddr();	
			logger.warn("Download IOException, that can be just because the client [" + ip +
					"] canceled the download process : " + ioe.getCause());
		}
	}

	@RequestMapping(value = "/{id}/delFile/{idFile}")
	@PreAuthorize("hasPermission(#id, 'manage') and hasPermission(#idFile, 'delFile') or hasRole('ROLE_ADMIN')")
	public String deleteCandidatureFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		PosteCandidatureFile postecandidatureFile = PosteCandidatureFile.findPosteCandidatureFile(idFile);
		postecandidature.getCandidatureFiles().remove(postecandidatureFile);

		Calendar cal = Calendar.getInstance();
		Date currentTime = cal.getTime();
		postecandidature.setModification(currentTime);

		postecandidature.persist();
		logService.logActionFile(LogService.DELETE_ACTION, postecandidature, postecandidatureFile, request, currentTime);
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
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);

		// upload file
		MultipartFile file = posteCandidatureFile.getFile();
		// sometimes file is null here, but I don't know how to reproduce this issue ... maybe that can occur only with some specifics browsers ?
		if(file != null) {
			String filename = file.getOriginalFilename();
			Long fileSize = file.getSize();
			
			if(fileSize != 0) {
				String contentType = file.getContentType();
				InputStream inputStream = file.getInputStream();
				//byte[] bytes = IOUtils.toByteArray(inputStream);
		
				posteCandidatureFile.setFilename(filename);
				posteCandidatureFile.setFileSize(fileSize);
				posteCandidatureFile.setContentType(contentType);
				logger.warn("Upload and set file in DB with filesize = " + fileSize);
				posteCandidatureFile.getBigFile().setBinaryFileStream(inputStream, fileSize);
				posteCandidatureFile.getBigFile().persist();
		
				Calendar cal = Calendar.getInstance();
				Date currentTime = cal.getTime();
				posteCandidatureFile.setSendTime(currentTime);
		
				postecandidature.getCandidatureFiles().add(posteCandidatureFile);
		
				postecandidature.setModification(currentTime);
		
				postecandidature.persist();
		
				logService.logActionFile(LogService.UPLOAD_ACTION, postecandidature, posteCandidatureFile, request, currentTime);
				returnReceiptService.logActionFile(LogService.UPLOAD_ACTION, postecandidature, posteCandidatureFile, request, currentTime);
			}
		} else {
			String userId = SecurityContextHolder.getContext().getAuthentication().getName();
			String ip = request.getRemoteAddr();
			String userAgent = request.getHeader("User-Agent");
			logger.warn(userId + "[" + ip + "] tried to add a 'null file' ... his userAgent is : " + userAgent);
		}

		return "redirect:/postecandidatures/" + id.toString();
	}

	
	@RequestMapping(value = "/{id}/modify")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String modifyRecevableCandidatureFile(@PathVariable("id") Long id, @RequestParam(required=true) Boolean recevable) {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		
		postecandidature.setRecevable(recevable);

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/auditionnable")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String modifyAuditionnableCandidatureFile(@PathVariable("id") Long id, @RequestParam(required=true) Boolean auditionnable) {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		
		if(auditionnable) {
			String mailTo = postecandidature.getEmail();
    	    String mailFrom = AppliConfig.getCacheMailFrom();
    	    String mailSubject = AppliConfig.getCacheMailSubject();
    	    
    	    String mailMessage = AppliConfig.getCacheTexteMailCandidatAuditionnable();
    	    mailMessage = mailMessage.replaceAll("@@numEmploi@@", postecandidature.getPoste().getNumEmploi());        
    		    		
    		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
		}
		
		for(PosteCandidatureFile candidatureFile : postecandidature.getCandidatureFiles()) {
			candidatureFile.setWriteable(false);
		}
		
		postecandidature.setAuditionnable(auditionnable);

		return "redirect:/postecandidatures/" + id.toString();
	}
	
	@RequestMapping(value = "/{id}/review")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
	public String reviewCandidatureFile(@PathVariable("id") Long id) {
		PosteCandidature postecandidature = PosteCandidature.findPosteCandidature(id);
		
		String emailAddress = SecurityContextHolder.getContext().getAuthentication().getName();
		User currentUser = User.findUsersByEmailAddress(emailAddress, null, null).getSingleResult();
		
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
		uiModel.addAttribute("posteCandidatureFile", new PosteCandidatureFile());
		uiModel.addAttribute("texteCandidatAideCandidatureDepot", AppliConfig.getCacheTexteCandidatAideCandidatureDepot());
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
	public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size,  @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {

		// uiModel.addAttribute("users", User.findUserEntries(firstResult,
		// sizeNo));

		List<PosteCandidature> postecandidatures = null;

		String emailAddress = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = User.findUsersByEmailAddress(emailAddress, null, null).getSingleResult();

		// pagination only for admin / manager users ...
		if (user.getIsAdmin() || user.getIsSuperManager() || user.getIsManager()) {

    		if(sortFieldName == null) 
            	sortFieldName = "o.poste.numEmploi,o.candidat.nom";   
    		if("nom".equals(sortFieldName))
    			sortFieldName = "candidat.nom";
    		if("email".equals(sortFieldName))
    			sortFieldName = "candidat.emailAddress";
    		
			if (page != null || size != null) {
				int sizeNo = size == null ? 10 : size.intValue();
				int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
				float nrOfPages = (float) PosteCandidature.countPosteCandidatures() / sizeNo;
				uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
				postecandidatures = PosteCandidature.findPosteCandidatureEntries(firstResult, sizeNo, sortFieldName, sortOrder);
			} else {
				postecandidatures = PosteCandidature.findAllPosteCandidatures(sortFieldName, sortOrder);
			}
		}

		else if (user.getIsCandidat()) {
			postecandidatures = PosteCandidature.findPosteCandidaturesByCandidat(user, null, null).getResultList();
			
			// restrictions si phase auditionnable
	        Date currentTime = new Date();     
			if(currentTime.compareTo(AppliConfig.getCacheDateEndCandidat()) > 0 || 
				currentTime.compareTo(AppliConfig.getCacheDateEndCandidatActif()) > 0) { 
				for(PosteCandidature postecandidature: postecandidatures) {
					if(!postecandidature.getAuditionnable()) {
						postecandidatures.remove(postecandidature);
					}
				}
			}
		}

		else if (user.getIsMembre()) {
			Set<PosteAPourvoir> postes = user.getPostes();
			postecandidatures = PosteCandidature.findPosteCandidaturesRecevableByPostes(postes).getResultList();
		}

		uiModel.addAttribute("postecandidatures", postecandidatures);
		uiModel.addAttribute("zip", new Boolean(false));
		
		uiModel.addAttribute("texteMembreAideCandidatures", AppliConfig.getCacheTexteMembreAideCandidatures());
		uiModel.addAttribute("texteCandidatAideCandidatures", AppliConfig.getCacheTexteCandidatAideCandidatures());
		
		addDateTimeFormatPatterns(uiModel);
		return "postecandidatures/list";
	}
	
    @RequestMapping(params = "find=ByPostes", method = RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String findPosteCandidaturesByPostes(HttpServletRequest request, HttpServletResponse response, @RequestParam(required=false, value="poste") List<PosteAPourvoir> postes, @RequestParam(required=false, defaultValue="off") Boolean zip, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) throws IOException, SQLException {
    	
    	if(zip) {  				
    		if(postes == null) 
    			return "redirect:/postecandidatures"; 
    		
    		File tmpFile = zipService.getZip(PosteCandidature.findPosteCandidaturesRecevableByPostes(new HashSet<PosteAPourvoir>(postes)).getResultList());

    		String contentType = "application/zip";
    		int zipSize = (int) tmpFile.length();
    		String baseName = "demat.zip";
    		InputStream inputStream = new FileInputStream(tmpFile);

    		response.setContentType(contentType);
    		response.setContentLength(zipSize);
    		//response.setCharacterEncoding("utf-8");
    		response.setHeader("Content-Disposition","attachment; filename=\"" + baseName +"\"");
    		FileCopyUtils.copy(inputStream, response.getOutputStream());

    		tmpFile.delete();
    		
    		return null;    		
    	} else {
    		
    		if(sortFieldName == null) 
            	sortFieldName = "o.poste.numEmploi,o.candidat.nom";   
    		if("nom".equals(sortFieldName))
    			sortFieldName = "candidat.nom";
    		if("email".equals(sortFieldName))
    			sortFieldName = "candidat.emailAddress";
    		
    		if (page != null || size != null) {
                int sizeNo = size == null ? 10 : size.intValue();
                final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
                uiModel.addAttribute("postecandidatures", PosteCandidature.findPosteCandidaturesByPostes(postes, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
                float nrOfPages = (float) PosteCandidature.countFindPosteCandidaturesByPostes(postes) / sizeNo;
                uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
            } else {
                uiModel.addAttribute("postecandidatures", PosteCandidature.findPosteCandidaturesByPostes(postes, sortFieldName, sortOrder).getResultList());
            }
            addDateTimeFormatPatterns(uiModel);       
            return "postecandidatures/list";           
    	}
    }

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @RequestMapping(params = "find=ByCandidats", method = RequestMethod.GET)
    public String findPosteCandidaturesByCandidats(@RequestParam(required=false, value="candidat") List<User> candidats, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {   	
		
		if(sortFieldName == null) 
        	sortFieldName = "o.poste.numEmploi,o.candidat.nom";   
		if("nom".equals(sortFieldName))
			sortFieldName = "candidat.nom";
		if("email".equals(sortFieldName))
			sortFieldName = "candidat.emailAddress";
		
		if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("postecandidatures", PosteCandidature.findPosteCandidaturesByCandidats(candidats, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) PosteCandidature.countFindPosteCandidaturesByCandidats(candidats) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("postecandidatures", PosteCandidature.findPosteCandidaturesByCandidats(candidats, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);       
        return "postecandidatures/list";
    }

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @RequestMapping(params = "find=ByRecevable", method = RequestMethod.GET)
    public String findPosteCandidaturesByRecevable(@RequestParam(value = "recevable", required = false) Boolean recevable, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        
		if(sortFieldName == null) 
        	sortFieldName = "o.poste.numEmploi,o.candidat.nom";   
		if("nom".equals(sortFieldName))
			sortFieldName = "candidat.nom";
		if("email".equals(sortFieldName))
			sortFieldName = "candidat.emailAddress";
        
		if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("postecandidatures", PosteCandidature.findPosteCandidaturesByRecevable(recevable == null ? Boolean.FALSE : recevable, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) PosteCandidature.countFindPosteCandidaturesByRecevable(recevable == null ? Boolean.FALSE : recevable) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("postecandidatures", PosteCandidature.findPosteCandidaturesByRecevable(recevable == null ? Boolean.FALSE : recevable, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "postecandidatures/list";
    }

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @RequestMapping(params = "find=ByAuditionnable", method = RequestMethod.GET)
    public String findPosteCandidaturesByAuditionnable(@RequestParam(value = "auditionnable", required = false) Boolean auditionnable, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        
		if(sortFieldName == null) 
        	sortFieldName = "o.poste.numEmploi,o.candidat.nom";   
		if("nom".equals(sortFieldName))
			sortFieldName = "candidat.nom";
		if("email".equals(sortFieldName))
			sortFieldName = "candidat.emailAddress";
        
		if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("postecandidatures", PosteCandidature.findPosteCandidaturesByAuditionnable(auditionnable == null ? Boolean.FALSE : auditionnable, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) PosteCandidature.countFindPosteCandidaturesByAuditionnable(auditionnable == null ? Boolean.FALSE : auditionnable) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("postecandidatures", PosteCandidature.findPosteCandidaturesByAuditionnable(auditionnable == null ? Boolean.FALSE : auditionnable, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "postecandidatures/list";
    }
}
