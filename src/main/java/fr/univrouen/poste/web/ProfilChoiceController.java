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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/profilChoice")
@Controller
public class ProfilChoiceController {

	private final Logger logger = Logger.getLogger(getClass());

    @RequestMapping
    public String profilChoice(@RequestParam(required=false) String profil) {
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(auth.getAuthorities());
    	
    	if(profil!=null) {
			logger.info(auth.getName() + " a sélectionné le profil " + profil);
    		if("membre".equals(profil)) {
    			authorities.remove(new SimpleGrantedAuthority("ROLE_CANDIDAT"));
    		}
    		if("candidat".equals(profil)) {
    			authorities.remove(new SimpleGrantedAuthority("ROLE_MEMBRE"));
    		}
    		auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
    		SecurityContextHolder.getContext().setAuthentication(auth);
    	}
    	
    	if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CANDIDAT")) && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEMBRE"))) {
    		return "profilChoice";
    	} else {
    		return "index";
    	}
    }
   
}
