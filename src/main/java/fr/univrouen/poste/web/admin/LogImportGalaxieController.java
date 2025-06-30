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

import fr.univrouen.poste.dao.LogImportGalaxieDao;
import fr.univrouen.poste.domain.LogImportGalaxie;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/admin/logimportgalaxies")
@Controller
public class LogImportGalaxieController {

    @Resource
    LogImportGalaxieDao logImportGalaxieDao;

    @ModelAttribute("command") 
    public LogSearchCriteria getLogSearchCriteria() {
    	return new LogSearchCriteria();
    }
    
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public String list(@ModelAttribute("command") LogSearchCriteria searchCriteria, @PageableDefault(size = 10, sort = "actionDate", direction = Sort.Direction.DESC) Pageable pageable, Model uiModel) {
        Page<LogImportGalaxie> logimportgalaxies = logImportGalaxieDao.findLogImportGalaxies(searchCriteria, pageable);
        uiModel.addAttribute("logimportgalaxies", logimportgalaxies);
        uiModel.addAttribute("command", searchCriteria);
        addDateTimeFormatPatterns(uiModel);
        return "admin/logimportgalaxies/list";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("logImportGalaxie_actiondate_date_format", "dd/MM/yyyy HH:mm");
    }

}
