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

import fr.univrouen.poste.dao.LogFileDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.LogFile;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/admin/logfiles")
@Controller
public class LogFileController {

    @Resource
    LogFileDao logFileDao;

    @Resource
    UserDao userDao;
	
    @ModelAttribute("command") 
    public LogSearchCriteria getLogSearchCriteria() {
    	return new LogSearchCriteria();
    }
    
	@ModelAttribute("users")
	public List<String> getUserIds() {
		List<String> userIds = new java.util.ArrayList<>(userDao.findAllUserIds());
		userIds.add(0, "");
		return userIds;
	}
	
	@ModelAttribute("userNoms")
	public List<String> getUserNoms() {
		List<String> userNoms = new java.util.ArrayList<>(userDao.findAllUserNoms());
		if(!userNoms.contains("")) {
			userNoms.add(0, "");
		}
		return userNoms;
	}
	
    @RequestMapping(params = "find=ByActionEquals", method = RequestMethod.GET)
    public String findLogFilesByActionEquals(@ModelAttribute("command") LogSearchCriteria searchCriteria, @PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if (pageable.isPaged()) {
            List<LogFile> results = logFileDao.findLogFiles(searchCriteria, sortFieldName, sortOrder);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), results.size());
            List<LogFile> sub = start <= end ? results.subList(start, end) : new ArrayList<>();
            uiModel.addAttribute("logfiles", new PageImpl<>(sub, pageable, results.size()));
        } else {
            uiModel.addAttribute("logfiles", logFileDao.findLogFiles(searchCriteria, sortFieldName, sortOrder));
        }
    	
        uiModel.addAttribute("command", searchCriteria);
        uiModel.addAttribute("finderview", true);

        addDateTimeFormatPatterns(uiModel);
        return "admin/logfiles/list";
    }
    

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String show(@PathVariable Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("logfile", logFileDao.findLogFile(id));
        uiModel.addAttribute("itemId", id);
        return "admin/logfiles/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (pageable.isPaged()) {
            Page<LogFile> result = logFileDao.findLogFileEntries(pageable, sortFieldName, sortOrder);
            uiModel.addAttribute("logfiles", result);
        } else {
            uiModel.addAttribute("logfiles", logFileDao.findAllLogFiles(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logfiles/list";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("logFile_actiondate_date_format", "dd/MM/yyyy HH:mm");
    }
}
