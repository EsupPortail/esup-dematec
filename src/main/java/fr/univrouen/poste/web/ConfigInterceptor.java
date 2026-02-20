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
package fr.univrouen.poste.web;

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.services.AppliVersionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.time.LocalDateTime;

@Component
public class ConfigInterceptor implements HandlerInterceptor {

    @Resource
    AppliConfigDao appliConfigDao;

    @Resource
    AppliVersionService appliVersionService;

    final static SortedMap<String, String> subTitles = new TreeMap<String, String>() {{
        put("/admin/galaxiemapping", "Mapping Excel Galaxie");
        put("/admin/appliconfig", "Configuration");
        put("/admin/appliconfigfiletype", "Configuration des types de fichier");
        put("/admin/templatefiles", "Configuration des templates pour les rapports de commission");
        put("/admin/users", "Utilisateurs");
        put("/admin/galaxieexcels",   "Fichiers Excel Galaxie");
        put("/admin/galaxieentrys",   "Importation depuis Galaxie");
        put("/posteapourvoirs",   "Liste des postes");
        put("/admin/candidats",   "Liste des candidats");
        put("/addpostecandidatures",   "Ajout de candidatures");
        put("/postecandidatures",   "Candidatures");
        put("/admin/logfiles",   "Statistiques sur les fichiers");
        put("/admin/logpostefiles",   "Statistiques sur les fichiers liés aux postes");
        put("/admin/logauths",   "Statistiques authentification");
        put("/admin/logmails",   "Statistiques Mail");
        put("/admin/commissionexcels",   "Fichiers Excel Commission");
        put("/admin/commissionentrys",   "Importation Commission");
        put("/admin/currentsessions",   "Sessions courantes / Utilisateurs connectés");
        put("/admin/logimportgalaxies",   "Logs (Rapports) Imports Galaxie");
        put("/admin/logimportcommissions",   "Logs (Rapports) Imports Commission");
        put("/admin/candidaturetags",   "Gestion des Tags");
        put("/admin/candidaturevaluetags",   "Gestion des Tags");
        put("/admin/su",   "SU");
        put("/admin/chart",   "Graphes");
        put("/admin",   "Résumé");
    }};

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {


        // we want to add usual model in modelAndView only when needed, ie with
        // direct html view :
        // not for download response (for example) because we don't need it
        // not for redirect view because we don't need it and we don't want that
        // they appears in the url
        if (modelAndView != null && !(modelAndView.getView() instanceof RedirectView) && !modelAndView.getViewName().startsWith("redirect:")) {
            completeModel(request.getRequestURI(), modelAndView);
        }
    }

    public void completeModel(String path, ModelAndView modelAndView) {
        AppliConfig config = appliConfigDao.getAppliConfig();
        String title = config != null ? config.getTitre() : "";
        modelAndView.addObject("title", title);

        String piedPage = config != null ? config.getPiedPage() : "";
        modelAndView.addObject("piedPage", piedPage);

        String imageUrl = config != null ? config.getImageUrl() : "";
        modelAndView.addObject("imageUrl", imageUrl);

        String subTitle = subTitles.get(path);
        String activeMenu = path.replaceAll("/", "");

        if (subTitle == null) {
            List<String> keys = new Vector<String>(subTitles.keySet());
            Collections.reverse(keys);
            for (String key : keys) {
                if (path.startsWith(key)) {
                    subTitle = subTitles.get(key);
                    activeMenu = key.replaceAll("/", "");
                    break;
                }
            }
        }

        modelAndView.addObject("subTitle", subTitle);
        modelAndView.addObject("activeMenu", activeMenu);

        Boolean candidatCanSignup = config != null && config.getDateEndCandidat() != null ? config.getCandidatCanSignup() : false;
        LocalDateTime currentTime = LocalDateTime.now();
        if (candidatCanSignup && config != null) {
            candidatCanSignup = currentTime.compareTo(config.getDateEndCandidat()) < 0;
        }
        modelAndView.addObject("candidatCanSignup", candidatCanSignup);

        Boolean postesMenu4Members = config != null ? config.getPostesMenu4Members() : false;
        modelAndView.addObject("postesMenu4Members", postesMenu4Members);

        modelAndView.addObject("versionEsupDematEC", appliVersionService.getCacheVersion());
    }

}
