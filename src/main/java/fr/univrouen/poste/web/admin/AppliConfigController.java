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

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.AppliConfig.MailReturnReceiptModeTypes;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import fr.univrouen.poste.services.AppliConfigService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.util.Arrays;
import java.util.List;

@RequestMapping("/admin/appliconfig")
@Controller
public class AppliConfigController {

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	AppliConfigService appliConfigService;

    @ModelAttribute("receiptModeTypes")
    public List<MailReturnReceiptModeTypes> getEnumTypes() {
            List<MailReturnReceiptModeTypes> receiptModeTypes = Arrays.asList(MailReturnReceiptModeTypes.values());
            return receiptModeTypes;
    }
    
	@ModelAttribute("recevableEnumList")
	public List<RecevableEnum> getRecevableEnumList() {
		return Arrays.asList(RecevableEnum.values());
	}


	@RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (pageable.isPaged()) {
            Page<AppliConfig> page = appliConfigDao.findAppliConfigEntries(pageable, sortFieldName, sortOrder);
            uiModel.addAttribute("appliconfigs", page);
        } else {
            uiModel.addAttribute("appliconfigs", appliConfigDao.findAllAppliConfigs(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/appliconfig/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    @Transactional
    public String update(@Valid AppliConfig appliConfig, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, appliConfig);
            return "admin/appliconfig/update";
        }
        uiModel.asMap().clear();
        appliConfigDao.saveAppliConfig(appliConfig);
        appliConfigService.clearCache();
        return "redirect:/admin/appliconfig/" + encodeUrlPathSegment(appliConfig.getId().toString(), httpServletRequest);
    }

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, appliConfigDao.findAppliConfig(id));
        return "admin/appliconfig/update";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("appliConfig_dateendcandidat_date_format", "dd/MM/yyyy HH:mm");
        uiModel.addAttribute("appliConfig_dateendcandidatactif_date_format", "dd/MM/yyyy HH:mm");
        uiModel.addAttribute("appliConfig_dateendmembre_date_format", "dd/MM/yyyy HH:mm");
    }

	void populateEditForm(Model uiModel, AppliConfig appliConfig) {
        uiModel.addAttribute("appliConfig", appliConfig);
        addDateTimeFormatPatterns(uiModel);
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
