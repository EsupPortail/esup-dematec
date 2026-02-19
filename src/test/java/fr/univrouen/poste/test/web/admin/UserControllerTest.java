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

import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.test.AbstractControllerTest;
import fr.univrouen.poste.test.TestUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour UserController (gestion des utilisateurs)
 * Vérifie :
 * - CRUD des utilisateurs
 * - Recherche et filtrage
 * - Validation des données
 *
 * Note: Ces tests utilisent uniquement MockMvc pour créer et manipuler les utilisateurs.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserControllerTest extends AbstractControllerTest {

    /**
     * Test 01 : Liste des utilisateurs NON accessible pour CANDIDAT
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test01_ListUsersNotAccessibleForCandidat() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 02 : Liste des utilisateurs accessible pour ADMIN
     */
    @Test
    @WithUserDetails("admin")
    public void test02_ListUsersAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"))
                .andExpect(model().attributeExists("users"));
    }

    /**
     * Test 03 : Formulaire de création d'utilisateur
     */
    @Test
    @WithUserDetails("admin")
    public void test03_CreateUserForm() throws Exception {
        mockMvc.perform(get("/admin/users?form"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/create"))
                .andExpect(model().attributeExists("user"));
    }

    /**
     * Test 04 : Création d'un nouvel utilisateur admin via MockMvc
     */
    @Test
    @WithUserDetails("admin")
    public void test04_CreateAdminUser() throws Exception {
        mockMvc.perform(post("/admin/users")
                .with(csrf())
                .param("emailAddress", "newadmin@example.org")
                .param("nom", "NewAdmin")
                .param("prenom", "User")
                .param("civilite", "M.")
                .param("password", "Password123")
                .param("isAdmin", "true")
                .param("isManager", "false")
                .param("isSuperManager", "false")
                .param("enabled", "true"))
                .andExpect(status().is3xxRedirection());

        // Vérifier que l'utilisateur a été créé via MockMvc
        MvcResult result = mockMvc.perform(get("/admin/users")
                .param("nomOrPrenomOrEmailAddress", "newadmin@example.org"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> users = (Page<User>) result.getModelAndView().getModel().get("users");
        assertNotNull("La liste des utilisateurs ne doit pas être null", users);
        assertEquals("Un utilisateur doit avoir été créé", 1, users.getContent().size());
    }

    /**
     * Test 05 : Création d'un utilisateur manager via MockMvc
     */
    @Test
    @WithUserDetails("admin")
    public void test05_CreateManagerUser() throws Exception {
        // Utiliser un email unique pour éviter les conflits avec d'autres tests
        String uniqueEmail = "manager.test05@example.org";

        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("emailAddress", uniqueEmail)
                        .param("nom", "Manager")
                        .param("prenom", "User")
                        .param("civilite", "M.")
                        .param("password", "Password123")
                        .param("isAdmin", "false")
                        .param("isManager", "true")
                        .param("isSuperManager", "false")
                        .param("enabled", "true"))
                .andExpect(status().is3xxRedirection());

        // Vérifier que l'utilisateur a été créé via MockMvc
        MvcResult result = mockMvc.perform(get("/admin/users")
                .param("nomOrPrenomOrEmailAddress", uniqueEmail))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> users = (Page<User>) result.getModelAndView().getModel().get("users");
        assertNotNull("La liste des utilisateurs ne doit pas être null", users);
        assertEquals("Un utilisateur doit avoir été créé", 1, users.getContent().size());
    }

    @Test
    @WithUserDetails("admin")
    public void test06_ModifyPassword() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> users = (Page<User>) result.getModelAndView().getModel().get("users");
        User candidat3 = users.getContent().stream()
                .filter(u -> "candidat3@example.org".equals(u.getEmailAddress()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        mockMvc.perform(put("/admin/users")
                .with(csrf())
                .params(TestUtils.getParamsAsStringMap(candidat3, "postes", "password"))
                .param("password", "Password123"))
                .andExpect(status().is3xxRedirection());


        User membre = users.getContent().stream()
                .filter(u -> "membre@example.org".equals(u.getEmailAddress()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        mockMvc.perform(put("/admin/users")
                        .with(csrf())
                        .params(TestUtils.getParamsAsStringMap(membre, "postes", "password"))
                        .param("password", "Password123"))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    public void test07_TestLoginCandidat() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "candidat3@example.org")
                .param("password", "Password123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void test08_TestLoginMembre() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "membre@example.org")
                        .param("password", "Password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}

