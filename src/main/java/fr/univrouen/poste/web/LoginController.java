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
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@RequestMapping("/login")
@Controller
public class LoginController {

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	PasswordEncoder passwordEncoder;

	@Resource
	AppliConfigDao appliConfigDao;

    @RequestMapping
	@Transactional
    public String login(
    		@RequestParam(value = "error", required = false) String error,
    		HttpServletRequest request,
    		Model model) {
    	AppliConfig config = appliConfigDao.getAppliConfig();
    	String textePremierePageAnonyme = config != null ? config.getTextePremierePageAnonyme() : "";
    	model.addAttribute("textePremierePageAnonyme", textePremierePageAnonyme);
    	Boolean candidatCanSignup = config != null ? config.getCandidatCanSignup() : false;
    	LocalDateTime currentTime = LocalDateTime.now();
    	if (candidatCanSignup && config != null) {
    		candidatCanSignup = currentTime.isBefore(config.getDateEndCandidat());
    	}
    	model.addAttribute("candidatCanSignup", candidatCanSignup);

    	// Récupération de l'exception d'authentification si présente
    	if (error != null) {
    		HttpSession session = request.getSession(false);
    		if (session != null) {
    			AuthenticationException authException =
    				(AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    			if (authException != null) {
    				model.addAttribute("error", error);
    				model.addAttribute("errorMessage", authException.getMessage());
    				logger.debug("Échec d'authentification: {}", authException.getMessage());
    			}
    		}
    	}

        return "login";
    }
   
}
