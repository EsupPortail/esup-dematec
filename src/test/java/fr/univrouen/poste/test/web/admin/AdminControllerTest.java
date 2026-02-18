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
package fr.univrouen.poste.test.web.admin;

import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour AdminController
 * Vérifie :
 * - Accès aux statistiques
 * - Téléchargement du ZIP
 * - Affichage des graphiques
 * - Contrôle d'accès selon les rôles
 *
 * Note: Ces tests utilisent uniquement MockMvc avec @WithMockUser pour tester la sécurité.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminControllerTest extends AbstractControllerTest {


    /**
     * Test 01 : Page d'administration NON accessible sans authentification
     */
    @Test
    public void test01_AdminStatsPageNotAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * Test 02 : Page d'administration NON accessible pour CANDIDAT
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test02_AdminStatsPageNotAccessibleForCandidat() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 03 : Page d'administration accessible pour ADMIN
     */
    @Test
    @WithUserDetails("admin")
    public void test03_AdminStatsPageAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("stat"));
    }

    /**
     * Test 05 : Page de graphiques accessible pour ADMIN
     */
    @Test
    @WithUserDetails("admin")
    public void test05_AdminChartPageAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin/chart"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/chart"))
                .andExpect(model().attributeExists("uploadStatsLabels"))
                .andExpect(model().attributeExists("uploadStatsValues"))
                .andExpect(model().attributeExists("authStatsLabels"))
                .andExpect(model().attributeExists("authStatsValues"));
    }

    /**
     * Test 06 : Endpoint ZIP retourne du contenu ZIP pour ADMIN
     */
    @Test
    @WithUserDetails("admin")
    public void test06_AdminZipDownloadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/zip"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().exists("Content-Disposition"));
    }

    /**
     * Test 07 : Endpoint ZIP NON accessible pour CANDIDAT
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test07_AdminZipDownloadNotAccessibleForCandidat() throws Exception {
        mockMvc.perform(get("/admin/zip"))
                .andExpect(status().isForbidden());
    }
}

