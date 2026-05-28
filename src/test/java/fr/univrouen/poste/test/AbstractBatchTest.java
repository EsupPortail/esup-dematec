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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

/**
 * Classe de base pour les tests de batchs.
 *
 * Contrairement à AbstractControllerTest, cette classe ne déclare PAS
 * @Transactional afin de laisser chaque service batch gérer ses propres
 * transactions (notamment generateCandidatsPostes() qui gère lui-même
 * son EntityManager).
 */
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
public abstract class AbstractBatchTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;
}

