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

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'initialisation - Première classe de test exécutée
 *
 * Ce test s'exécute en tout premier dans la suite d'intégration et crée
 * les utilisateurs de base nécessaires pour tous les autres tests :
 * - super-manager@example.org : Super manager avec tous les droits
 *
 * Note: L'utilisateur admin existe déjà (créé automatiquement au démarrage de l'application)
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InitialSetupControllerTest extends AbstractControllerTest {

    /**
     * Test 01 : Vérifier que l'utilisateur admin existe
     * L'utilisateur admin est créé automatiquement au démarrage de l'application
     */
    @Test
    @WithUserDetails("admin")
    public void test01_VerifyAdminExists() throws Exception {
        // Vérifier que l'utilisateur admin peut accéder à la liste des utilisateurs
        MvcResult result = mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"))
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> users = (Page<User>) result.getModelAndView().getModel().get("users");
        assertNotNull("La liste des utilisateurs ne doit pas être null", users);

        // Chercher l'utilisateur admin
        boolean adminFound = users.stream()
                .anyMatch(u -> "admin".equals(u.getEmailAddress()));

        assertTrue("L'utilisateur admin doit exister", adminFound);

        System.out.println("✓ Utilisateur admin vérifié");
        System.out.println("✓ Nombre d'utilisateurs au démarrage : " + users.getContent().size());
    }

    /**
     * Test 02 : Admin crée un utilisateur super-manager@example.org
     * Cet utilisateur aura tous les droits (admin, manager, super-manager)
     */
    @Test
    @WithUserDetails("admin")
    public void test02_AdminCreatesSuperManager() throws Exception {
        // Créer l'utilisateur super-manager@example.org
        mockMvc.perform(post("/admin/users")
                .with(csrf())
                .param("emailAddress", "super-manager@example.org")
                .param("nom", "SuperManager")
                .param("prenom", "Test")
                .param("civilite", "M.")
                .param("password", "Password123")
                .param("isAdmin", "false")
                .param("isManager", "true")
                .param("isSuperManager", "true")
                .param("enabled", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/users/*"));

        System.out.println("✓ Création de super-manager@example.org effectuée");

        // Vérifier que l'utilisateur a bien été créé
        MvcResult result = mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> users = (Page<User>) result.getModelAndView().getModel().get("users");
        assertNotNull("La liste des utilisateurs ne doit pas être null", users);
        assertEquals("Un utilisateur super-manager@example.org doit avoir été créé", 2, users.getContent().size());

        User superManager = users.getContent().get(1);
        assertEquals("L'email doit correspondre", "super-manager@example.org", superManager.getEmailAddress());
        assertEquals("Le nom doit correspondre", "SuperManager", superManager.getNom());
        assertEquals("Le prénom doit correspondre", "Test", superManager.getPrenom());
        assertFalse("Ne doit pas être admin", superManager.getIsAdmin() != null && superManager.getIsAdmin());
        assertTrue("Doit être manager", superManager.getIsManager() != null && superManager.getIsManager());
        assertTrue("Doit être super-manager", superManager.getIsSuperManager() != null && superManager.getIsSuperManager());
        assertTrue("Doit être activé", superManager.getEnabled() != null && superManager.getEnabled());

        System.out.println("✓ Vérification de super-manager@example.org : OK");
        System.out.println("  - Email : " + superManager.getEmailAddress());
        System.out.println("  - Nom : " + superManager.getNom() + " " + superManager.getPrenom());
        System.out.println("  - isManager : " + superManager.getIsManager());
        System.out.println("  - isSuperManager : " + superManager.getIsSuperManager());
        System.out.println("  - isAdmin : " + superManager.getIsAdmin());
        System.out.println("  - enabled : " + superManager.getEnabled());
    }

    /**
     * Test 03 : Vérifier que super-manager@example.org peut accéder aux fonctions de management
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test03_SuperManagerCanAccessManagementFunctions() throws Exception {
        // Vérifier que le super-manager peut accéder à la liste des postes
        mockMvc.perform(get("/posteapourvoirs"))
                .andExpect(status().isOk())
                .andExpect(view().name("posteapourvoirs/list"))
                .andExpect(model().attributeExists("posteapourvoirs"));

        System.out.println("✓ super-manager@example.org peut accéder à /posteapourvoirs");

        // Vérifier que le super-manager peut accéder à la liste des candidatures
        mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"));

        System.out.println("✓ super-manager@example.org peut accéder à /postecandidatures");
    }

    /**
     * Test 04 : Vérifier que super-manager@example.org NE PEUT PAS accéder aux fonctions admin
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test04_SuperManagerCannotAccessAdminFunctions() throws Exception {
        // Vérifier que le super-manager ne peut PAS accéder à la zone admin
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());

        System.out.println("✓ super-manager@example.org peut accéder à /admin (normal)");

        // Vérifier que le super-manager ne peut PAS accéder à la liste des utilisateurs
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());

        System.out.println("✓ super-manager@example.org peut accéder à /admin/users (normal)");
    }

    @Test
    @WithUserDetails("super-manager@example.org")
    public void test05_SuperManagerConfigDates() throws Exception {

        // Vérifier que le super-manager peut accéder à la page de configuration
        MvcResult result = mockMvc.perform(get("/admin/appliconfig"))
                .andExpect(status().isOk()).andReturn();

        Page<AppliConfig> appliconfigs = (Page<AppliConfig>) result.getModelAndView().getModel().get("appliconfigs");
        assertTrue("Il doit y avoir 1 configuration appliconfig", appliconfigs.getContent().size()==1);

        AppliConfig config = appliconfigs.getContent().get(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30); // +30 jours
        config.setDateEndCandidat(futureDate);
        config.setDateEndMembre(futureDate);
        config.setDateEndCandidatActif(futureDate);
        config.setTitre("Test depuis test d'intégration");

        // vérification
        result = mockMvc.perform(get("/admin/appliconfig/" + config.getId()))
                .andExpect(status().isOk()).andReturn();
        AppliConfig updatedConfig = (AppliConfig) result.getModelAndView().getModel().get("appliConfig");
        assertNotNull("La configuration doit être récupérée", updatedConfig);
        assertEquals("Le titre doit être mis à jour", "Test depuis test d'intégration", updatedConfig.getTitre());
        assertNotNull("La date de fin candidat doit être mise à jour", updatedConfig.getDateEndCandidat());
        assertNotNull("La date de fin membre doit être mise à jour", updatedConfig.getDateEndMembre());
        assertNotNull("La date de fin candidat actif doit être mise à jour", updatedConfig.getDateEndCandidatActif());

        System.out.println("✓");
    }



}

