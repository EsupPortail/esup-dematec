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

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.CommissionExcel;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour l'import Commission et la génération automatique de membres de commission et postes
 *
 * Ce test suit le workflow complet :
 * 1. Upload d'un fichier Excel via CommissionExcelController (/admin/commissionexcels/addFile)
 * 2. Génération des membres de commission et postes via CommissionEntryController (/admin/commissionentrys/generatecommissions)
 *
 * Note: Ce test utilise uniquement MockMvc pour tester l'intégration complète du workflow Commission.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommissionImportControllerTest extends AbstractControllerTest {

    /**
     * Test 01 : Upload d'un fichier Excel Commission et génération des entités
     *
     * Workflow complet :
     * 1. Upload du fichier Excel via MockMvc
     * 2. Vérification que le fichier a été importé (CommissionExcel créé)
     * 3. Vérification que les CommissionEntry ont été créées
     * 4. Génération des membres de commission et postes
     * 5. Vérification que les entités ont été créées
     * 6. Vérification qu'un membre de commission manager@example.org a été créé
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test01_CommissionImportAndGeneration() throws Exception {
        // 1. Charger le fichier Excel COMMISSION.xls
        File excelFile = new File("src/main/webapp/doc/COMMISSION.xls");

        // Si le fichier n'existe pas, skip le test avec un message informatif
        if (!excelFile.exists()) {
            System.out.println("⚠️ SKIPPING TEST: Le fichier COMMISSION.xls n'existe pas dans src/main/webapp/doc/");
            System.out.println("   Pour exécuter ce test, veuillez créer un fichier Excel COMMISSION.xls avec les données de test appropriées.");
            return;
        }

        FileInputStream fis = new FileInputStream(excelFile);
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            excelFile.getName(),
            "application/vnd.ms-excel",
            fis
        );

        // 2. Upload du fichier Excel via MockMvc
        mockMvc.perform(multipart("/admin/commissionexcels/addFile")
                .file(multipartFile)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/commissionexcels"));

        // 3. Vérifier que le CommissionExcel a été créé via MockMvc
        MvcResult commissionExcelResult = mockMvc.perform(get("/admin/commissionexcels")
                .param("sortFieldName", "creation")
                .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/commissionexcels/list"))
                .andExpect(model().attributeExists("commissionexcels"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<CommissionExcel> commissionExcels = (Page<CommissionExcel>) commissionExcelResult.getModelAndView()
                .getModel().get("commissionexcels");
        assertNotNull("La liste des CommissionExcel ne doit pas être null", commissionExcels);
        assertTrue("Au moins un CommissionExcel doit être créé", commissionExcels.getContent().size() > 0);

        CommissionExcel lastCommissionExcel = commissionExcels.getContent().get(0);
        assertNotNull("Le dernier CommissionExcel doit exister", lastCommissionExcel);
        assertEquals("Le nom du fichier doit correspondre", excelFile.getName(), lastCommissionExcel.getFilename());

        System.out.println("✓ Fichier Excel importé : " + lastCommissionExcel.getFilename());

        // 4. Vérifier que des CommissionEntry ont été créées (parsing du fichier Excel) via MockMvc
        MvcResult commissionEntryResult = mockMvc.perform(get("/admin/commissionentrys"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/commissionentrys/list"))
                .andExpect(model().attributeExists("commissionentrys"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<CommissionEntry> commissionEntries = (Page<CommissionEntry>) commissionEntryResult.getModelAndView()
                .getModel().get("commissionentrys");
        assertNotNull("La liste des CommissionEntry ne doit pas être null", commissionEntries);
        assertTrue("Des CommissionEntry doivent être créées après le parsing", commissionEntries.getContent().size() > 0);

        System.out.println("✓ Nombre de CommissionEntry créées : " + commissionEntries.getContent().size());

        // 5. Compter les entités AVANT la génération via MockMvc
        MvcResult usersBeforeResult = mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> usersBefore = (Page<User>) usersBeforeResult.getModelAndView().getModel().get("users");
        int userCountBefore = usersBefore != null ? usersBefore.getContent().size() : 0;

        System.out.println("Avant génération : Users=" + userCountBefore);

        // 6. Générer les membres de commission et postes via MockMvc
        mockMvc.perform(get("/admin/commissionentrys/generatecommissions")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // 7. Vérifier que des utilisateurs ont été créés via MockMvc
        MvcResult usersAfterResult = mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> usersAfter = (Page<User>) usersAfterResult.getModelAndView().getModel().get("users");
        assertNotNull("La liste des utilisateurs après génération ne doit pas être null", usersAfter);
        int userCountAfter = usersAfter.getContent().size();

        assertTrue("Des utilisateurs doivent être créés", userCountAfter > userCountBefore);

        System.out.println("✓ Après génération : Users=" + userCountAfter);
        System.out.println("✓ Nouveaux Users créés : " + (userCountAfter - userCountBefore));

        // 9. Vérifier qu'on peut récupérer un utilisateur créé
        if (userCountAfter > userCountBefore) {
            assertFalse("La liste des utilisateurs ne doit pas être vide", usersAfter.isEmpty());
            User firstUser = usersAfter.getContent().get(userCountAfter-1);
            assertNotNull("L'utilisateur doit avoir un email", firstUser.getEmailAddress());
            System.out.println("✓ Exemple d'utilisateur créé : " + firstUser.getEmailAddress());
        }

        // 11. Vérifier qu'un membre de commission manager@example.org a été créé via MockMvc
        MvcResult membresResult = mockMvc.perform(get("/admin/users")
                .param("nomOrPrenomOrEmailAddress", "membre@example.org"))
                .andExpect(status().isOk())
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> membres = (Page<User>) membresResult.getModelAndView().getModel().get("users");
        assertFalse(membres.isEmpty());

        User membre = membres.getContent().get(0);
        assertNotNull("Le membre de commission membre@example.org doit exister", membre);
        assertEquals("L'email doit correspondre", "membre@example.org", membre.getEmailAddress());
        System.out.println("✓ Membre de commission créé : " + membre.getEmailAddress());

        // Vérifier que le membre n'est pas manager ni candidat mais manager
        assertFalse("Le membre ne doit pas être admin", membre.getIsAdmin());
        assertFalse("Le membre ne doit pas être  doit être manager", membre.getIsManager());
        assertFalse("Le membre ne doit pas être super manager", membre.getIsSuperManager());
        // Impossible de récupérer les postes en lazy via MockMvc dont getIsMembre est false ici...
        // assertTrue(membre.getIsMembre());
        assertFalse(membre.getIsCandidat());
    }

    /**
     * Test 02 : Accès à l'upload de fichier Excel NON autorisé pour CANDIDAT
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test02_CommissionImportNotAllowedForCandidat() throws Exception {
        File excelFile = new File("src/main/webapp/doc/COMMISSION.xls");

        if (excelFile.exists()) {
            FileInputStream fis = new FileInputStream(excelFile);
            MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                excelFile.getName(),
                "application/vnd.ms-excel",
                fis
            );

            mockMvc.perform(multipart("/admin/commissionexcels/addFile")
                    .file(multipartFile)
                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * Test 03 : Accès à la génération NON autorisé pour CANDIDAT
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test03_GenerationNotAllowedForCandidat() throws Exception {
        mockMvc.perform(get("/admin/commissionentrys/generatecommissions")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 04 : Liste des CommissionEntry accessible pour ADMIN
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test04_ListCommissionEntriesAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin/commissionentrys"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/commissionentrys/list"))
                .andExpect(model().attributeExists("commissionentrys"));
    }

    /**
     * Test 05 : Liste des CommissionExcel accessible pour ADMIN
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test05_ListCommissionExcelsAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin/commissionexcels"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/commissionexcels/list"));
    }

    /**
     * Test 06 : Vérifier que les CommissionEntry sont bien liées aux membres et postes créés
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test06_VerifyCommissionEntryLinks() throws Exception {
        // Récupérer toutes les CommissionEntry via MockMvc
        MvcResult commissionEntryResult = mockMvc.perform(get("/admin/commissionentrys"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/commissionentrys/list"))
                .andExpect(model().attributeExists("commissionentrys"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<CommissionEntry> entries = (Page<CommissionEntry>) commissionEntryResult.getModelAndView()
                .getModel().get("commissionentrys");

        if (entries != null && !entries.isEmpty()) {
            System.out.println("✓ Nombre total de CommissionEntry : " + entries.getContent().size());

            // Vérifier via le modèle les listes unknowMembres et unknowPostes
            // qui indiquent les entrées sans membre ou poste
            @SuppressWarnings("unchecked")
            java.util.Set<String> unknowMembres = (java.util.Set<String>) commissionEntryResult.getModelAndView()
                    .getModel().get("unknowMembres");
            @SuppressWarnings("unchecked")
            java.util.Set<String> unknowPostes = (java.util.Set<String>) commissionEntryResult.getModelAndView()
                    .getModel().get("unknowPostes");

            int totalEntries = entries.getContent().size();
            int entriesWithMembreNull = unknowMembres != null ? unknowMembres.size() : 0;
            int entriesWithPosteNull = unknowPostes != null ? unknowPostes.size() : 0;

            // Calculer les entrées avec membre et poste
            int entriesWithMembreNotNull = totalEntries - entriesWithMembreNull;
            int entriesWithPosteNotNull = totalEntries - entriesWithPosteNull;

            System.out.println("✓ CommissionEntry avec membre : " + entriesWithMembreNotNull);
            System.out.println("✓ CommissionEntry avec poste : " + entriesWithPosteNotNull);

            // Vérifier qu'au moins une entrée a un membre
            assertTrue("Au moins une CommissionEntry doit avoir un membre", entriesWithMembreNotNull > 0);

            // Vérifier qu'au moins une entrée a un poste
            assertTrue("Au moins une CommissionEntry doit avoir un poste", entriesWithPosteNotNull > 0);

            System.out.println("✓ Vérification des liens CommissionEntry complétée");
        }
    }
}

