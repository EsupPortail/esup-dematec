/**
 * Licensed to ESUP-Portail under one or more contributor license
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

import fr.univrouen.poste.domain.MemberReviewFile;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.test.TestUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration pour MyPosteCandidatureController - Parcours complet d'un membre
 *
 * Ce test simule le processus naturel d'un membre (membre@example.org) qui :
 * 1. Liste ses candidatures
 * 2. Vérifie qu'il a une candidature (et récupère le numéro de poste)
 * 5. Télécharge le fichier de candidature
 *
 * Note: Ce test dépend des données créées en amont
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyPosteCandidatureControllerMembreTest extends MyPosteCandidatureControllerTestBase {

    /**
     * Test 01 : Le membre liste ses candidatures
     */
    @Test
    @WithUserDetails("membre@example.org")
    public void test01_CandidatListeSesCandidatures() throws Exception {
        test_CandidatListeCandidatures();
    }

    /**
     * Test 02 : Le membre vérifie qu'il a une candidature et récupère le numéro de poste
     */
    @Test
    @WithUserDetails("membre@example.org")
    public void test02_VerificationTelechargementFichierCandidat() throws Exception {
        test_VerificationTelechargementFichier();
    }

    @Test
    @WithUserDetails("membre3@example.org")
    public void test03_VerificationAccesAutreCandidat() throws Exception {
        test_VerificationAccesAInterdit();
    }

    @Test
    @WithUserDetails("membre@example.org")
    public void test04_VerificationUploadRapportCommission() throws Exception {
        MockMultipartFile multipartFile = TestUtils.getPdfFile();
        mockMvc.perform(multipart("/postecandidatures/" + candidatureId + "/addMemberReviewFile")
                        .file(multipartFile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails("membre@example.org")
    public void test05_VerificationDownloadRapportCommission() throws Exception {
        // 1. Télécharger le rapport de commission
        MvcResult candidatureResult = mockMvc.perform(get("/postecandidatures/" + candidatureId))
                .andExpect(status().isOk())
                .andReturn();

        PosteCandidature candidature = (PosteCandidature) candidatureResult.getModelAndView().getModel().get("postecandidature");
        assertNotNull("La candidature doit être présente dans le modèle", candidature);
        assertFalse("La candidature doit avoir au moins un rapport de commission", candidature.getMemberReviewFiles().isEmpty());

        MemberReviewFile memberReviewFile = candidature.getMemberReviewFiles().stream().findFirst().get();

        mockMvc.perform(get("/postecandidatures/" + candidatureId + "/reviewFile/" + memberReviewFile.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andReturn();
    }

    @Test
    @WithUserDetails("candidat@example.org")
    public void test06_VerificationNoDownloadRapportCommission4Candidat() throws Exception {
        // 1. Télécharger le rapport de commission
        MvcResult candidatureResult = mockMvc.perform(get("/postecandidatures/" + candidatureId))
                .andExpect(status().isOk())
                .andReturn();

        PosteCandidature candidature = (PosteCandidature) candidatureResult.getModelAndView().getModel().get("postecandidature");
        assertNotNull("La candidature doit être présente dans le modèle", candidature);
        assertFalse("La candidature doit avoir au moins un rapport de commission", candidature.getMemberReviewFiles().isEmpty());

        MemberReviewFile memberReviewFile = candidature.getMemberReviewFiles().stream().findFirst().get();

        mockMvc.perform(get("/postecandidatures/" + candidatureId + "/reviewFile/" + memberReviewFile.getId()))
                .andExpect(status().isForbidden());
    }

}

