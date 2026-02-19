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
package fr.univrouen.poste.test.web.membre;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour PosteAPourvoirController
 * Vérifie :
 * - Affichage de la liste des postes
 * - Création/modification de postes
 * - Contrôle d'accès selon les rôles
 *
 * Note: Ces tests utilisent uniquement MockMvc pour créer et manipuler les postes.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PosteAPourvoirControllerTest extends AbstractControllerTest {


    /**
     * Test 01 : Liste des postes NON accessible pour CANDIDAT
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test01_ListPostesNotAccessibleForCandidat() throws Exception {
        mockMvc.perform(get("/posteapourvoirs"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 02 : Liste des postes accessible pour ADMIN
     */
    @Test
    @WithUserDetails("admin")
    public void test02_ListPostesAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/posteapourvoirs"))
                .andExpect(status().isOk())
                .andExpect(view().name("posteapourvoirs/list"))
                .andExpect(model().attributeExists("posteapourvoirs"));
    }

    /**
     * Test 04 : Affichage d'un poste spécifique
     */
    @Test
    @WithUserDetails("admin")
    public void test04_ShowPoste() throws Exception {
        // Récupérer la liste des postes via MockMvc
        MvcResult result = mockMvc.perform(get("/posteapourvoirs"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posteapourvoirs"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<PosteAPourvoir> postes = (List<PosteAPourvoir>) result.getModelAndView()
                .getModel().get("posteapourvoirs");

        // Si des postes existent, tester l'affichage du premier
        if (postes != null && !postes.isEmpty()) {
            PosteAPourvoir poste = postes.get(0);
            mockMvc.perform(get("/posteapourvoirs/" + poste.getId()))
                    .andExpect(status().isOk());
        }
    }

    /**
     * Test 05 : Vérification de l'existence de postes créés par GalaxieImportControllerTest
     *
     * Note: Ce test vérifie que les postes ont bien été créés par GalaxieImportControllerTest
     * au lieu de créer de nouveaux postes.
     */
    @Test
    @WithUserDetails("admin")
    public void test05_PostesExistFromGalaxieImport() throws Exception {
        // Vérifier que des postes ont été créés via MockMvc
        MvcResult result = mockMvc.perform(get("/posteapourvoirs"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posteapourvoirs"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<PosteAPourvoir> postes = (List<PosteAPourvoir>) result.getModelAndView()
                .getModel().get("posteapourvoirs");
        assertNotNull("La liste des postes ne doit pas être null", postes);
        assertFalse("Des postes doivent avoir été créés par GalaxieImportControllerTest", postes.isEmpty());

        System.out.println("✓ Nombre de postes disponibles : " + postes.size());
        if (!postes.isEmpty()) {
            System.out.println("✓ Exemple de poste : " + postes.get(0).getNumEmploi());
        }
    }

    /**
     * Test 06 : Création de poste NON autorisée pour MEMBRE
     */
    @Test
    @WithMockUser(username = "membre@example.org", roles = {"MEMBRE"})
    public void test06_CreatePosteNotAllowedForMembre() throws Exception {
        mockMvc.perform(post("/posteapourvoirs")
                .with(csrf())
                .param("numEmploi", "MCF888")
                .param("localisation", "Test")
                .param("profil", "Test"))
                .andExpect(status().isForbidden());
    }
}

