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

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;

@RequestMapping("/posteapourvoirs")
@Controller
@RooWebScaffold(path = "posteapourvoirs", formBackingObject = PosteAPourvoir.class, create=true, update=true, delete=false)
public class PosteAPourvoirController {
	
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
    
}
