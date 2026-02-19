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
package fr.univrouen.poste.test;

import fr.univrouen.poste.config.AppContextConfig;
import fr.univrouen.poste.config.AppContextTestConfig;
import fr.univrouen.poste.config.MethodSecurityConfig;
import fr.univrouen.poste.config.SecurityConfig;
import fr.univrouen.poste.config.WebMvcConfig;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Classe de base pour tous les tests de contrôleurs.
 *
 * Configure :
 * - MockMvc pour les tests web
 * - PostgreSQL via Testcontainers (hérité de AbstractDatabaseTest)
 * - Méthodes utilitaires pour l'authentification
 *
 * Les tests construisent progressivement les données en base PostgreSQL.
 */
@Transactional
@Rollback(false)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {
                AppContextConfig.class,
                AppContextTestConfig.class,
                SecurityConfig.class,
                MethodSecurityConfig.class,
                WebMvcConfig.class
        },
        initializers = TestcontainersInitializer.class
)
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class AbstractControllerTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }


    /**
     * Nettoie le contexte de sécurité après chaque test
     */
    protected void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}

