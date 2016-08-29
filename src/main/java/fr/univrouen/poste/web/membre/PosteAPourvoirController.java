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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
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

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteAPourvoirFile;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.LogService;

@RequestMapping("/posteapourvoirs")
@Controller
@RooWebScaffold(path = "posteapourvoirs", formBackingObject = PosteAPourvoir.class, create=true, update=true, delete=false)
@Transactional
public class PosteAPourvoirController {

	private final Logger logger = Logger.getLogger(getClass());
	
    void populateEditForm(Model uiModel, PosteAPourvoir posteAPourvoir) {
        uiModel.addAttribute("posteAPourvoir", posteAPourvoir);
        uiModel.addAttribute("users", User.findAllNoCandidats());
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String create(@Valid PosteAPourvoir posteAPourvoir, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, posteAPourvoir);
            return "posteapourvoirs/create";
        }
        uiModel.asMap().clear();
        posteAPourvoir.persist();
        return "redirect:/posteapourvoirs/" + encodeUrlPathSegment(posteAPourvoir.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'viewposte')")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("posteapourvoir", PosteAPourvoir.findPosteAPourvoir(id));
        uiModel.addAttribute("itemId", id);
        uiModel.addAttribute("posteFile", new PosteAPourvoirFile());
        return "posteapourvoirs/show";
    }
    
    @RequestMapping(produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_MEMBRE')")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	boolean isMembre = auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_MEMBRE"));
    	
    	if(isMembre) {
    		String emailAddress = auth.getName();
    		User user = User.findUsersByEmailAddress(emailAddress, null, null).getSingleResult();
    		List<PosteAPourvoir> posteapourvoirs = PosteAPourvoir.findPosteAPourvoirsByMembre(user);
    		uiModel.addAttribute("posteapourvoirs", posteapourvoirs);
    	} else if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("posteapourvoirs", PosteAPourvoir.findPosteAPourvoirEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) PosteAPourvoir.countPosteAPourvoirs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("posteapourvoirs", PosteAPourvoir.findAllPosteAPourvoirs(sortFieldName, sortOrder));
        }
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
        posteAPourvoir.merge();
        return "redirect:/posteapourvoirs/" + encodeUrlPathSegment(posteAPourvoir.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, PosteAPourvoir.findPosteAPourvoir(id));
        return "posteapourvoirs/update";
    }
    
    
	@RequestMapping(value = "/{id}/{idFile}")
	@PreAuthorize("hasPermission(#id, 'viewposte')")
	public void downloadPosteFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		try {
			PosteAPourvoir poste = PosteAPourvoir.findPosteAPourvoir(id);
			PosteAPourvoirFile posteFile = PosteAPourvoirFile.findPosteAPourvoirFile(idFile);
			String filename = posteFile.getFilename();
			Long size = posteFile.getFileSize();
			String contentType = posteFile.getContentType();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			response.setContentLength(size.intValue());
			IOUtils.copy(posteFile.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
	
			Calendar cal = Calendar.getInstance();
			Date currentTime = cal.getTime();
	
			//logService.logActionFile(LogService.DOWNLOAD_ACTION, postecandidature, postecandidatureFile, request, currentTime);
		} catch(IOException ioe) {
	        String ip = request.getRemoteAddr();	
			logger.warn("Download IOException, that can be just because the client [" + ip +
					"] canceled the download process : " + ioe.getCause());
		}
	}
	
	@RequestMapping(value = "/{id}/delFile/{idFile}")
	@PreAuthorize("hasPermission(#id, 'manageposte')")
	public String deletePosteFile(@PathVariable("id") Long id, @PathVariable("idFile") Long idFile, HttpServletRequest request, HttpServletResponse response) throws IOException {
		PosteAPourvoir poste = PosteAPourvoir.findPosteAPourvoir(id);
		PosteAPourvoirFile posteFile = PosteAPourvoirFile.findPosteAPourvoirFile(idFile);
		poste.getPosteFiles().remove(posteFile);

		//logService.logActionFile(LogService.DELETE_ACTION, postecandidature, postecandidatureFile, request, currentTime);
		return "redirect:/posteapourvoirs/" + id.toString();
	}

	@RequestMapping(value = "/{id}/addFile", method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasPermission(#id, 'manageposte')")
	public String addFile(@PathVariable("id") Long id, @Valid PosteAPourvoirFile posteFile, BindingResult bindingResult, Model uiModel, HttpServletRequest request) throws IOException {
		if (bindingResult.hasErrors()) {
			logger.warn(bindingResult.getAllErrors());
			return "redirect:/posteapourvoirs/" + id.toString();
		}
		uiModel.asMap().clear();

		PosteAPourvoir poste = PosteAPourvoir.findPosteAPourvoir(id);

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
				
					posteFile.setFilename(filename);
					posteFile.setFileSize(fileSize);
					posteFile.setContentType(contentType);
					logger.info("Upload and set file in DB with filesize = " + fileSize);
					posteFile.getBigFile().setBinaryFileStream(inputStream, fileSize);
					posteFile.getBigFile().persist();
					
					Calendar cal = Calendar.getInstance();
					Date currentTime = cal.getTime();
					posteFile.setSendTime(currentTime);
					
					poste.getPosteFiles().add(posteFile);
					poste.persist();
				
					// logService.logActionFile(LogService.UPLOAD_ACTION, posteCandidature, posteCandidatureFile, request, currentTime);
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
}
