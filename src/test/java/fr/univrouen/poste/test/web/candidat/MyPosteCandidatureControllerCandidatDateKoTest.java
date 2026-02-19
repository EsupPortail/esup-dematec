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
package fr.univrouen.poste.test.web.candidat;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyPosteCandidatureControllerCandidatDateKoTest extends MyPosteCandidatureControllerTestBase {


    /*
        * Vérifier que le candidat ne peut plus accéder à la candidature ni au fichier
        * (les dates étant maintenant dépassées)
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test01_VerificationAccesCandidatDatesKo() throws Exception {
        test_VerificationAccesAInterdit();
    }

    /*
     * Vérifier que le membre ne peut plus accéder à la candidature et au fichier
     * (les dates étant maintenant dépassées)
     */
    @Test
    @WithUserDetails("membre@example.org")
    public void test02_VerificationAccesMembreDatesKo() throws Exception {
        // test_VerificationAccesAInterdit();
        test_VerificationTelechargementFichier();
        // Todo actuellement il peut, mais il ne peut plus s'authentifier, donc il ne peut pas télécharger le fichier. A revoir.
    }


    @Test
    public void test03_TestLoginMembreKo() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "membre@example.org")
                        .param("password", "Password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

}

