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
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@RequestMapping("/")
@Controller
public class IndexController {

    @PersistenceContext
    transient EntityManager entityManager;

	@Resource
	AppliConfigDao appliConfigDao;
    
	@RequestMapping
	public String index(Model uiModel) throws IOException {

		AppliConfig config = appliConfigDao.getAppliConfig();
		String textePremierePageCandidat = config != null ? config.getTextePremierePageCandidat() : "";
		String textePremierePageMembre = config != null ? config.getTextePremierePageMembre() : "";

		uiModel.addAttribute("textePremierePageCandidat", textePremierePageCandidat);
		uiModel.addAttribute("textePremierePageMembre", textePremierePageMembre);
		
		String hbm2ddlAuto = getHbm2ddlAuto();
		uiModel.addAttribute("hbm2ddlAuto", hbm2ddlAuto);
		
		return "index";
	}

	String getHbm2ddlAuto() {
		Session session = entityManager.unwrap(Session.class);
		SessionFactoryImpl sessionImpl = (SessionFactoryImpl)session.getSessionFactory();
		// Dans Hibernate 6, on accède directement à la propriété de configuration
		Object hbm2ddlProperty = sessionImpl.getProperties().get("hibernate.hbm2ddl.auto");
		return hbm2ddlProperty != null ? hbm2ddlProperty.toString() : "none";
	}

}
