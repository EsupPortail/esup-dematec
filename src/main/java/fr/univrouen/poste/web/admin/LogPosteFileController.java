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
import fr.univrouen.poste.domain.LogPosteFile;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/logpostefiles")
@Controller
public class LogPosteFileController {

    @Resource
    LogPosteFileDao logPosteFileDao;

    @ModelAttribute("command")
    public LogSearchCriteria getLogSearchCriteria() {
        return new LogSearchCriteria();
    }

    @ModelAttribute("emails")
    public List<String> getEmails() {
        List<String> emails = new java.util.ArrayList<>(logPosteFileDao.findAllDistinctEmails());
        emails.add(0, "");
        return emails;
    }

    @ModelAttribute("actions")
    public List<String> getActions() {
        List<String> actions = new java.util.ArrayList<>(logPosteFileDao.findAllDistinctActions());
        actions.add(0, "");
        return actions;
    }

    @ModelAttribute("numEmplois")
    public List<String> getNumEmplois() {
        List<String> numEmplois = new java.util.ArrayList<>(logPosteFileDao.findAllDistinctNumEmplois());
        numEmplois.add(0, "");
        return numEmplois;
    }

    @RequestMapping(produces = "text/html")
    public String list(@ModelAttribute("command") LogSearchCriteria searchCriteria,
                       @PageableDefault(size = 10, sort="actionDate", direction = Sort.Direction.DESC) Pageable pageable,
                       Model uiModel) {
        Page<LogPosteFile> page = logPosteFileDao.findLogPosteFilesByCriteria(searchCriteria, pageable);
        uiModel.addAttribute("logpostefiles", page);
        uiModel.addAttribute("command", searchCriteria);
        return "admin/logpostefiles/list";
    }

}

