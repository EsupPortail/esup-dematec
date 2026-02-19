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
package fr.univrouen.poste.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * JavaConfig de test remplaçant applicationContext-test.xml.
 *
 * Charge les propriétés de test en priorité (application-test.properties)
 * ainsi que les propriétés génériques du classpath.
 * Les valeurs de database.* sont ensuite surchargées dynamiquement
 * par TestcontainersInitializer avant le démarrage du contexte Spring.
 */
@Configuration
@PropertySource(
        value = {
                "classpath:application-test.properties",
                "classpath:META-INF/spring/database.properties",
                "classpath:META-INF/spring/email.properties",
                "classpath:META-INF/spring/security.properties"
        },
        ignoreResourceNotFound = true
)
public class AppContextTestConfig {
    // Pas de beans supplémentaires : les surcharges de propriétés
    // sont gérées par @PropertySource et TestcontainersInitializer.
}

