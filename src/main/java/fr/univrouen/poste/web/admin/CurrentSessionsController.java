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

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/currentsessions")
@Controller
public class CurrentSessionsController {

	@Autowired
	private SessionRegistry sessionRegistry;
	
	@RequestMapping
	public String getCurrentSessions(Model uiModel) throws IOException {

		List<String> sessions = new Vector<String>();
		List<Object> principals = sessionRegistry.getAllPrincipals();
		
		for(Object p: principals) {
			if(p instanceof User) {
				sessions.add(((User)p).getUsername());
			}
		}
		
		uiModel.addAttribute("sessions", sessions);
		
		return "admin/currentsessions";
	}

}
