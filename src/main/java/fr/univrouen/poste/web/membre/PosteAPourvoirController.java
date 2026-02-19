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
package fr.univrouen.poste.web.membre;

import fr.univrouen.poste.dao.*;
import fr.univrouen.poste.domain.*;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.services.ZipService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RequestMapping("/posteapourvoirs")
@Controller
@Transactional
public class PosteAPourvoirController {

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	UserDao userDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;

	@Resource
	PosteAPourvoirFileDao posteAPourvoirFileDao;

	@Resource
	CommissionEntryDao commissionEntryDao;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	LogService logService;
	
	@Resource
	ZipService zipService;

	@Resource
	BigFileDao bigFileDao;
    
	protected User getCurrentUser() {
		String emailAddress = SecurityContextHolder.getContext().getAuthentication().getName();
		return userDao.findUserByEmailAddress(emailAddress);
	}
	
    void populateEditForm(Model uiModel, PosteAPourvoir posteAPourvoir) {
        uiModel.addAttribute("posteapourvoir", posteAPourvoir);
        uiModel.addAttribute("users", userDao.findAllNoCandidatsAndNoManagers());
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String create(@Valid PosteAPourvoir posteAPourvoir, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, posteAPourvoir);
            return "posteapourvoirs/create";
        }
        uiModel.asMap().clear();
        posteAPourvoirDao.savePosteAPourvoir(posteAPourvoir);
        return "redirect:/posteapourvoirs/" + encodeUrlPathSegment(posteAPourvoir.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'viewposte')")
    public String show(@PathVariable Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        PosteAPourvoir poste = posteAPourvoirDao.findPosteAPourvoir(id);
        uiModel.addAttribute("posteapourvoir", poste);
        uiModel.addAttribute("itemId", id);
        uiModel.addAttribute("posteFile", new PosteAPourvoirFile());
		Boolean isPresident = poste.getPresidents() != null && poste.getPresidents().contains(getCurrentUser());
		uiModel.addAttribute("isPresident", isPresident);
        return "posteapourvoirs/show";
    }
    
    @RequestMapping(produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_MEMBRE')")
    public String list(@PageableDefault(size = 10) Pageable pageable,
    		@RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel,
    		HttpServletRequest request) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	boolean isMembre = request.isUserInRole("ROLE_MEMBRE");
    	
    	if(isMembre) {
    		String emailAddress = auth.getName();
    		User user = userDao.findUserByEmailAddress(emailAddress);
    		if (user != null) {
				Page<PosteAPourvoir> posteapourvoirs = posteAPourvoirDao.findPosteAPourvoirsByMembre(pageable, user);
    			uiModel.addAttribute("posteapourvoirs", posteapourvoirs);
    		}
    	} else {
			Page<PosteAPourvoir> posteapourvoirs = posteAPourvoirDao.findPosteAPourvoirEntries(pageable, sortFieldName, sortOrder);
			uiModel.addAttribute("posteapourvoirs", posteapourvoirs);
		}
    	AppliConfig config = appliConfigDao.getAppliConfig();
    	String textePostesMenu4Members = config != null ? config.getTextePostesMenu4Members() : "";
    	uiModel.addAttribute("textePostesMenu4Members", textePostesMenu4Members);
        addDateTimeFormatPatterns(uiModel);
        return "posteapourvoirs/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String update(@Valid PosteAPourvoir posteAPourvoir, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, posteAPourvoir);
            return "posteapourvoirs/update";
        }
        uiModel.asMap().clear();
        
        // attention de preserver les fichiers ...
        PosteAPourvoir oldPoste = posteAPourvoirDao.findPosteAPourvoir(posteAPourvoir.getId());
        posteAPourvoir.setPosteFiles(oldPoste.getPosteFiles());
        
        // update poste par formulaire -> attention à ce que les CommissionEntry soient cohérents 
        // sinon la modification sera écrasée au prochain 'import/génération' d'un Excel de commissions
        for(User membre : oldPoste.getMembres()) {
        	if(posteAPourvoir.getMembres()== null || !posteAPourvoir.getMembres().contains(membre)) {
        		List<CommissionEntry> commissionEntriesForThisAffectation = commissionEntryDao.findCommissionEntrysByNumPosteAndEmail(oldPoste.getNumEmploi(), membre.getEmailAddress());
        		for(CommissionEntry commissionEntry : commissionEntriesForThisAffectation) {
        			commissionEntryDao.deleteCommissionEntry(commissionEntry);
        		}
        	}
        }
        for(User president : oldPoste.getPresidents()) {
        	if(posteAPourvoir.getPresidents()== null || !posteAPourvoir.getPresidents().contains(president)) {
        		List<CommissionEntry> commissionEntriesForThisAffectation = commissionEntryDao.findCommissionEntrysByNumPosteAndEmail(oldPoste.getNumEmploi(), president.getEmailAddress());
        		for(CommissionEntry commissionEntry : commissionEntriesForThisAffectation) {
        			commissionEntryDao.deleteCommissionEntry(commissionEntry);
        		}
        	}
        }
        
        posteAPourvoirDao.savePosteAPourvoir(posteAPourvoir);
        return "redirect:/posteapourvoirs/" + encodeUrlPathSegment(posteAPourvoir.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, posteAPourvoirDao.findPosteAPourvoir(id));
        return "posteapourvoirs/update";
    }
    
    
	@RequestMapping(value = "/{id}/{idFile}")
	@PreAuthorize("hasPermission(#id, 'viewposte')")
	public void downloadPosteFile(@PathVariable Long id, @PathVariable Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteAPourvoir poste = posteAPourvoirDao.findPosteAPourvoir(id);
			PosteAPourvoirFile posteFile = posteAPourvoirFileDao.findPosteAPourvoirFile(idFile);
			String filename = posteFile.getFilename();
			Long size = posteFile.getFileSize();
			String contentType = posteFile.getContentType();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.setContentLength(size.intValue());
			IOUtils.copy(posteFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
	
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
	
			logService.logActionPosteFile(LogService.DOWNLOAD_ACTION, poste, posteFile, request, currentTime);
		} catch(IOException ioe) {
	        String ip = request.getRemoteAddr();	
			logger.warn("Download IOException, that can be just because the client [" + ip +
					"] canceled the download process : " + ioe.getCause());
		}
	}
	
	@RequestMapping(value = "/{id}/delFile/{idFile}")
	@PreAuthorize("hasPermission(#id, 'manageposte')")
	public String deletePosteFile(@PathVariable Long id, @PathVariable Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException {
		PosteAPourvoir poste = posteAPourvoirDao.findPosteAPourvoir(id);
		PosteAPourvoirFile posteFile = posteAPourvoirFileDao.findPosteAPourvoirFile(idFile);
		poste.getPosteFiles().remove(posteFile);
		
		Calendar cal = Calendar.getInstance();
		Date currentTime = cal.getTime();
		
		logService.logActionPosteFile(LogService.DELETE_ACTION, poste, posteFile, request, currentTime);
		return "redirect:/posteapourvoirs/" + id.toString();
	}

	@RequestMapping(value = "/{id}/addFile", method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasPermission(#id, 'manageposte')")
	public String addFile(@PathVariable Long id, @Valid PosteAPourvoirFile posteFile, BindingResult bindingResult, Model uiModel, HttpServletRequest request) throws IOException {
		if (bindingResult.hasErrors()) {
			logger.warn("Errors on addFile method : {}", bindingResult.getAllErrors());
			return "redirect:/posteapourvoirs/" + id.toString();
		}
		uiModel.asMap().clear();

		PosteAPourvoir poste = posteAPourvoirDao.findPosteAPourvoir(id);

		// upload file
		MultipartFile file = posteFile.getFile();
		
		// sometimes file is null here, but I don't know how to reproduce this issue ... maybe that can occur only with some specifics browsers ?
		if(file != null) {
			String filename = file.getOriginalFilename();
			
			boolean filenameAlreadyUsed = false;
			for(PosteAPourvoirFile pcFile : poste.getPosteFiles()) {
				if(pcFile.getFilename().equals(filename)) {
					filenameAlreadyUsed = true;
					break;
				}
			}		
			
			if(filenameAlreadyUsed) {
				uiModel.addAttribute("filename_already_used", filename);
				logger.warn("Upload Restriction sur '" + filename + "' un fichier de même nom existe déjà pour le poste " + poste.getNumEmploi());
			} else {
				
				Long fileSize = file.getSize();
				
				if(fileSize != 0) {
					String contentType = file.getContentType();
					// cf https://github.com/EsupPortail/esup-dematec/issues/8 - workaround pour éviter mimetype erroné comme application/text-plain:formatted
					contentType = contentType.replaceAll(":.*", "");
					
					logger.info("Try to upload file '" + filename + "' with size=" + fileSize + " and contentType=" + contentType);
					
					InputStream inputStream = file.getInputStream();
					//byte[] bytes = IOUtils.toByteArray(inputStream);

					PosteAPourvoirFile newFile = new PosteAPourvoirFile();
					newFile.setContentType(posteFile.getContentType());
					newFile.setFilename(filename);
					newFile.setFileSize(fileSize);
					newFile.setContentType(contentType);
					logger.info("Upload and set file in DB with filesize = " + fileSize);
					bigFileDao.setBinaryFileStream(newFile.getBigFile(), inputStream, fileSize);
					bigFileDao.saveBigFile(newFile.getBigFile());
					
					Calendar cal = Calendar.getInstance();
					Date currentTime = cal.getTime();
					newFile.setSendTime(currentTime);
					
					User currentUser = getCurrentUser();
					newFile.setSender(currentUser);
					
					poste.getPosteFiles().add(newFile);
				
					logService.logActionPosteFile(LogService.UPLOAD_ACTION, poste, newFile, request, currentTime);
				}
			}
		} else {
			String userId = SecurityContextHolder.getContext().getAuthentication().getName();
			String ip = request.getRemoteAddr();
			String userAgent = request.getHeader("User-Agent");
			logger.warn(userId + "[" + ip + "] tried to add a 'null file' ... his userAgent is : " + userAgent);
		}

		return "redirect:/posteapourvoirs/" + id.toString();
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new PosteAPourvoir());
        return "posteapourvoirs/create";
    }
	
	@RequestMapping(value = "/{id}", params = {"export"})
	@PreAuthorize("hasPermission(#id, 'viewposte')")
	public String exportPosteFiles(@PathVariable Long id, @RequestParam(required=true) String export, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
			SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String currentTimeFmt = dateFmt.format(currentTime);
			
			PosteAPourvoir poste = posteAPourvoirDao.findPosteAPourvoir(id);
			String fileName = poste.getNumEmploi() + "-poste-" + currentTimeFmt + "." + export;
			DematFileDummy dematFile = new DematFileDummy(fileName, "-");
			
			if("zip".equals(export)) {						
	    		String contentType = "application/zip";
	    		response.setContentType(contentType);
	    		response.setHeader("Content-Disposition","attachment; filename=\"" + fileName +"\"");
	    		zipService.writeZip(poste, response.getOutputStream());
			} else {
				return "redirect:/postecandidatures/" + id.toString();
			}	
			logService.logActionPosteFile(LogService.DOWNLOAD_ACTION, poste, dematFile, request, currentTime);
		} catch(Exception e) {
			logger.info("PostCandidature " + id + " can't be exported as " + export, e);
			return "redirect:/postecandidatures/" + id.toString() + "?exportFailed=" + export;
		}
		return null;
	}

	

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("posteAPourvoir_dateendcandidatauditionnable_date_format", "dd/MM/yyyy HH:mm");
        uiModel.addAttribute("posteAPourvoir_dateendsignupcandidat_date_format", "dd/MM/yyyy HH:mm");
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
}

