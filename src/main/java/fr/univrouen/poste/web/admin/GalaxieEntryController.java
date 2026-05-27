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

import fr.univrouen.poste.dao.GalaxieEntryDao;
import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.services.GalaxieEntriesService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@RequestMapping("/admin/galaxieentrys")
@Controller
@Transactional
public class GalaxieEntryController {
	
	@Resource
	GalaxieEntriesService galaxieEntriesService;

	@Resource
	GalaxieEntryDao galaxieEntryDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	UserDao userDao;

    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, @RequestParam(value = "search", required = false) String search, Model uiModel) {
    	if(sortFieldName==null)
        	sortFieldName = "numEmploi,numCandidat";
        if (search != null && !search.trim().isEmpty()) {
            uiModel.addAttribute("galaxieentrys", galaxieEntryDao.findGalaxieEntryEntriesBySearch(search.trim(), pageable));
        } else if (pageable.isPaged()) {
            Page<GalaxieEntry> page = galaxieEntryDao.findGalaxieEntryEntries(pageable, sortFieldName, sortOrder);
            uiModel.addAttribute("galaxieentrys", page);
        } else {
            uiModel.addAttribute("galaxieentrys", galaxieEntryDao.findAllGalaxieEntrys(sortFieldName, sortOrder));
        }
        uiModel.addAttribute("search", search);
        
        Map<String, String> unknowCandidats = new HashMap<String, String>();
        Map<String, String> unknowPostes = new HashMap<String, String>();
        Map<List<String>, String> unknowCandidatures = new HashMap<List<String>, String>();
        
        List<GalaxieEntry> galaxieEntrysWithCandidatNull = galaxieEntryDao.findAllGalaxieEntrysWithCandidatNull();
        for(GalaxieEntry  galaxieEntry : galaxieEntrysWithCandidatNull) {
        	unknowCandidats.put(galaxieEntry.getNumCandidat(), "dummy");
        }
        
        List<GalaxieEntry> galaxieEntrysWithPosteNull = galaxieEntryDao.findAllGalaxieEntrysWithPosteNull();
        for(GalaxieEntry  galaxieEntry : galaxieEntrysWithPosteNull) {
            unknowPostes.put(galaxieEntry.getNumEmploi(), "dummy");
        }

        List<GalaxieEntry> galaxieEntrysWithCandidatureNull = galaxieEntryDao.findGalaxieEntrysByCandidatureIsNull();
        for(GalaxieEntry  galaxieEntry : galaxieEntrysWithCandidatureNull) {
       		List<String> candidatureKey = new Vector<String>();
       		candidatureKey.add(galaxieEntry.getNumEmploi());
       		candidatureKey.add(galaxieEntry.getNumCandidat());
       		candidatureKey.add(galaxieEntry.getId().toString());
       		unknowCandidatures.put(candidatureKey, "dummy");
        }

        uiModel.addAttribute("unknowCandidats", unknowCandidats.keySet());
        uiModel.addAttribute("unknowPostes", unknowPostes.keySet());
        uiModel.addAttribute("unknowCandidatures", unknowCandidatures.keySet());
        
        return "admin/galaxieentrys/list";
    }
    
    @RequestMapping("/generatecandidatspostes")
    public String generateCandidatsPostes () {
    	galaxieEntriesService.generateCandidatsPostes();
        return "redirect:/admin/logimportgalaxies";
    }
    

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid GalaxieEntry galaxieEntry, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, galaxieEntry);
            return "admin/galaxieentrys/create";
        }
        uiModel.asMap().clear();
        galaxieEntryDao.saveGalaxieEntry(galaxieEntry);
        return "redirect:/admin/galaxieentrys/" + encodeUrlPathSegment(galaxieEntry.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new GalaxieEntry());
        return "admin/galaxieentrys/create";
    }

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String show(@PathVariable Long id, Model uiModel) {
        uiModel.addAttribute("galaxieentry", galaxieEntryDao.findGalaxieEntry(id));
        uiModel.addAttribute("itemId", id);
        return "admin/galaxieentrys/show";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid GalaxieEntry galaxieEntry, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, galaxieEntry);
            return "admin/galaxieentrys/update";
        }
        uiModel.asMap().clear();
        galaxieEntryDao.saveGalaxieEntry(galaxieEntry);
        return "redirect:/admin/galaxieentrys/" + encodeUrlPathSegment(galaxieEntry.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, galaxieEntryDao.findGalaxieEntry(id));
        return "admin/galaxieentrys/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, @PageableDefault(size = 10) Pageable pageable, Model uiModel) {
        galaxieEntryDao.deleteGalaxieEntry(id);
        uiModel.asMap().clear();
        return "redirect:/admin/galaxieentrys";
    }

	void populateEditForm(Model uiModel, GalaxieEntry galaxieEntry) {
        uiModel.addAttribute("galaxieEntry", galaxieEntry);
        uiModel.addAttribute("posteapourvoirs", posteAPourvoirDao.findAllPosteAPourvoirs());
        uiModel.addAttribute("postecandidatures", posteCandidatureDao.findAllPosteCandidatures());
        uiModel.addAttribute("users", userDao.findAllUsers());
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
