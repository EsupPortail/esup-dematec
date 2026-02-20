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

import fr.univrouen.poste.domain.PosteCandidature;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyPosteCandidatureControllerCandidatAuditionnableTest extends MyPosteCandidatureControllerTestBase {


    /*
        * Vérifier que le candidat ne peut plus accéder à la candidature ni au fichier
        * (les dates étant maintenant dépassées)
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test01_VerificationAccesCandidatDatesKo() throws Exception {
        test_VerificationAccesAInterdit();
    }

    @Test
    @WithUserDetails("super-manager@example.org")
    public void test02_CandidatAuditionnable() throws Exception {
        MvcResult candidatureDetailResult = mockMvc.perform(get("/postecandidatures/" + candidatureId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidature"))
                .andReturn();

        PosteCandidature candidatureDetail = (PosteCandidature) candidatureDetailResult.getModelAndView()
                .getModel().get("postecandidature");
        candidatureDetail.setAuditionnable(true);
    }

    @Test
    @WithUserDetails("candidat@example.org")
    public void test03_VerificationAccesCandidatDatesKoAudtionnable() throws Exception {
        test_VerificationTelechargementFichier();
    }

}

