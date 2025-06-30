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

import fr.univrouen.poste.dao.LogAuthDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.LogAuth;
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

@RequestMapping("/admin/logauths")
@Controller
public class LogAuthController {

    @Resource
    LogAuthDao logAuthDao;

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
    
    @RequestMapping(params = "find=ByActionEqualsAndUserIdEquals", method = RequestMethod.GET)
    public String findLogAuthsByActionEquals(@ModelAttribute("command") LogSearchCriteria searchCriteria, @PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if("".equals(searchCriteria.getStatus()) && "".equals(searchCriteria.getUserId())) {
    		return this.list(pageable, sortFieldName, sortOrder, uiModel);
    	}
    	if (pageable.isPaged()) {
            List<LogAuth> results = logAuthDao.findLogAuths(searchCriteria, sortFieldName, sortOrder);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), results.size());
            List<LogAuth> sub = start <= end ? results.subList(start, end) : new ArrayList<>();
            uiModel.addAttribute("logauths", new PageImpl<>(sub, pageable, results.size()));
        } else {
            uiModel.addAttribute("logauths", logAuthDao.findLogAuths(searchCriteria, sortFieldName, sortOrder));
        }    
        uiModel.addAttribute("command", searchCriteria);
        uiModel.addAttribute("finderview", true);
        addDateTimeFormatPatterns(uiModel);
        return "admin/logauths/list";
    }
    

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String show(@PathVariable Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("logauth", logAuthDao.findLogAuth(id));
        uiModel.addAttribute("itemId", id);
        return "admin/logauths/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (pageable.isPaged()) {
            Page<LogAuth> result = logAuthDao.findLogAuthEntries(pageable, sortFieldName, sortOrder);
            uiModel.addAttribute("logauths", result);
        } else {
            uiModel.addAttribute("logauths", logAuthDao.findAllLogAuths(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logauths/list";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        LogAuth logAuth = logAuthDao.findLogAuth(id);
        logAuthDao.deleteLogAuth(logAuth);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/logauths";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("logAuth_actiondate_date_format", "dd/MM/yyyy HH:mm");
    }
}
