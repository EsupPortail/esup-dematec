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
package fr.univrouen.poste.test.web;

import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour LoginController
 * Vérifie :
 * - Affichage de la page de login
 * - Accès sans authentification
 *
 * Note: Ces tests utilisent uniquement MockMvc.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginControllerTest extends AbstractControllerTest {

    /**
     * Test 01 : Page de login accessible sans authentification
     * Vérifie que le contrôleur est appelé et remplit le modèle correctement
     */
    @Test
    public void test01_LoginPageAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/login").with(anonymous()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("textePremierePageAnonyme"))
                .andExpect(model().attributeExists("candidatCanSignup"));
    }

    /**
     * Test 02 : Redirection après échec de login
     */
    @Test
    public void test02_LoginFailureRedirection() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", "wronguser@example.org")
                .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    public void test03_LoginOkWithRedirection() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}

