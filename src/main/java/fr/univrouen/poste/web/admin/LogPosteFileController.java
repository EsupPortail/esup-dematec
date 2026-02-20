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

import fr.univrouen.poste.dao.LogPosteFileDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.LogPosteFile;
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

import java.util.List;

@RequestMapping("/admin/logpostefiles")
@Controller
public class LogPosteFileController {

    @Resource
    LogPosteFileDao logPosteFileDao;

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

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, sort="actionDate", direction = Sort.Direction.DESC) Pageable pageable, Model uiModel) {
        Page<LogPosteFile> page = logPosteFileDao.findLogPosteFileEntries(pageable);
        uiModel.addAttribute("logpostefiles", page);
        return "admin/logpostefiles/list";
    }

}
