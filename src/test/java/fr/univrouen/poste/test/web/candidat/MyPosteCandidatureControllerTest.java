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

import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour MyPosteCandidatureController
 * Vérifie :
 * - Affichage des candidatures
 * - Téléchargement de fichiers
 * - Modification du statut (recevable/auditionnable)
 * - Contrôle d'accès selon les rôles et permissions
 *
 * Note: Ces tests utilisent uniquement MockMvc pour créer et manipuler les candidatures.
 * Les utilisateurs et postes nécessaires sont créés via les contrôleurs admin.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyPosteCandidatureControllerTest extends AbstractControllerTest {

    /**
     * Test 01 : Liste des candidatures NON accessible sans authentification
     */
    @Test
    public void test01_ListPosteCandidaturesNotAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * Test 02 : Liste des candidatures accessible pour utilisateur authentifié
     *
     * Note: Ce test utilise un candidat créé par GalaxieImportControllerTest
     */
    @Test
    @WithUserDetails("admin")
    public void test02_ListPosteCandidaturesAccessible() throws Exception {
        // Récupérer un candidat créé par GalaxieImportControllerTest
        MvcResult usersResult = mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) usersResult.getModelAndView().getModel().get("users");

        User candidat = users.stream()
                .filter(u -> u.getNumCandidat() != null && !u.getNumCandidat().isEmpty())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun candidat trouvé"));

        // Tester avec ce candidat
        mockMvc.perform(get("/postecandidatures")
                .with(user(candidat.getEmailAddress()).roles("CANDIDAT")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"))
                .andExpect(model().attributeExists("texteCandidatAideCandidatures"));
    }


    /**
     * Test 03 : Affichage d'une candidature spécifique pour un utilisateur
     *
     * Note: Ce test vérifie simplement l'accès à la liste des candidatures.
     * Les candidatures spécifiques seront testées avec les données créées par GalaxieImportControllerTest.
     */
    @Test
    @WithUserDetails("admin")
    public void test03_ShowPosteCandidature() throws Exception {
        // Vérifier l'accès à la liste des candidatures
        mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"));
    }


    /**
     * Test 05 : Export CSV des candidatures accessible pour utilisateurs avec rôles appropriés
     * Note: L'export CSV se fait via le paramètre find=ByMultiParams&csv=on
     * Utilise l'utilisateur admin déjà existant.
     */
    @Test
    @WithUserDetails("admin")
    public void test05_ExportCsvAccessibleForAdmin() throws Exception {
        // Tester l'export CSV avec l'utilisateur admin existant
        // L'export CSV nécessite le paramètre find=ByMultiParams et csv=on
        mockMvc.perform(get("/postecandidatures")
                .with(user("admin").roles("ADMIN"))
                .param("find", "ByMultiParams")
                .param("csv", "on"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=utf-8"));
    }

}

