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

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.PosteAPourvoir;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour l'import Galaxie et la génération automatique de candidats, postes et candidatures
 *
 * Ce test suit le workflow complet :
 * 1. Upload d'un fichier Excel via GalaxieExcelController (/admin/galaxieexcels/addFile)
 * 2. Génération des candidats, postes et candidatures via GalaxieEntryController (/admin/galaxieentrys/generatecandidatspostes)
 *
 * Note: Ce test utilise uniquement MockMvc pour tester l'intégration complète du workflow Galaxie.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GalaxieImportControllerTest extends AbstractControllerTest {

    /**
     * Test 01 : Upload d'un fichier Excel Galaxie et génération des entités
     *
     * Workflow complet :
     * 1. Upload du fichier Excel via MockMvc
     * 2. Vérification que le fichier a été importé (GalaxieExcel créé)
     * 3. Vérification que les GalaxieEntry ont été créées
     * 4. Génération des candidats, postes et candidatures
     * 5. Vérification que les entités ont été créées
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test01_GalaxieImportAndGeneration() throws Exception {
        // 1. Charger le fichier Excel d'exemple
        File excelFile = new File("src/main/webapp/doc/EXTETBF.xls");
        if (!excelFile.exists()) {
            // Essayer l'autre fichier
            excelFile = new File("src/main/webapp/doc/COMMISSION.xls");
        }

        assertTrue("Le fichier Excel d'exemple doit exister", excelFile.exists());

        FileInputStream fis = new FileInputStream(excelFile);
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            excelFile.getName(),
            "application/vnd.ms-excel",
            fis
        );

        // 2. Upload du fichier Excel via MockMvc
        mockMvc.perform(multipart("/admin/galaxieexcels/addFile")
                .file(multipartFile)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/galaxieexcels"));

        // 3. Vérifier que le GalaxieExcel a été créé via MockMvc
        MvcResult galaxieExcelResult = mockMvc.perform(get("/admin/galaxieexcels")
                .param("sortFieldName", "creation")
                .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/galaxieexcels/list"))
                .andExpect(model().attributeExists("galaxieexcels"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<GalaxieExcel> galaxieExcels = (Page<GalaxieExcel>) galaxieExcelResult.getModelAndView()
                .getModel().get("galaxieexcels");
        assertNotNull("La liste des GalaxieExcel ne doit pas être null", galaxieExcels);
        assertTrue("Au moins un GalaxieExcel doit être créé", !galaxieExcels.isEmpty());

        GalaxieExcel lastGalaxieExcel = galaxieExcels.getContent().get(0);
        assertNotNull("Le dernier GalaxieExcel doit exister", lastGalaxieExcel);
        assertEquals("Le nom du fichier doit correspondre", excelFile.getName(), lastGalaxieExcel.getFilename());

        System.out.println("✓ Fichier Excel importé : " + lastGalaxieExcel.getFilename());

        // 4. Vérifier que des GalaxieEntry ont été créées (parsing du fichier Excel) via MockMvc
        MvcResult galaxieEntryResult = mockMvc.perform(get("/admin/galaxieentrys"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/galaxieentrys/list"))
                .andExpect(model().attributeExists("galaxieentrys"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<GalaxieEntry> galaxieEntries = (Page<GalaxieEntry>) galaxieEntryResult.getModelAndView()
                .getModel().get("galaxieentrys");
        assertNotNull("La liste des GalaxieEntry ne doit pas être null", galaxieEntries);
        assertTrue("Des GalaxieEntry doivent être créées après le parsing", !galaxieEntries.isEmpty());

        System.out.println("✓ Nombre de GalaxieEntry créées : " + galaxieEntries.getContent().size());

        // 5. Compter les entités AVANT la génération via MockMvc
        MvcResult usersBeforeResult = mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<User> usersBefore = (Page<User>) usersBeforeResult.getModelAndView().getModel().get("users");
        int userCountBefore = usersBefore != null ? usersBefore.getContent().size() : 0;

        MvcResult postesBeforeResult = mockMvc.perform(get("/posteapourvoirs"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posteapourvoirs"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<PosteAPourvoir> postesBefore = (Page<PosteAPourvoir>) postesBeforeResult.getModelAndView()
                .getModel().get("posteapourvoirs");
        int posteCountBefore = postesBefore != null ? postesBefore.getContent().size() : 0;

        System.out.println("Avant génération : Users=" + userCountBefore +
                           ", Postes=" + posteCountBefore);

        // 6. Générer les candidats, postes et candidatures via MockMvc
        mockMvc.perform(get("/admin/galaxieentrys/generatecandidatspostes")
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

        // 8. Vérifier que des postes ont été créés via MockMvc
        MvcResult postesAfterResult = mockMvc.perform(get("/posteapourvoirs"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posteapourvoirs"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<PosteAPourvoir> postesAfter = (Page<PosteAPourvoir>) postesAfterResult.getModelAndView()
                .getModel().get("posteapourvoirs");
        assertNotNull("La liste des postes après génération ne doit pas être null", postesAfter);
        int posteCountAfter = postesAfter.getContent().size();

        assertTrue("Des postes doivent être créés", posteCountAfter > posteCountBefore);

        System.out.println("✓ Après génération : Users=" + userCountAfter +
                           ", Postes=" + posteCountAfter);

        System.out.println("✓ Nouveaux Users créés : " + (userCountAfter - userCountBefore));
        System.out.println("✓ Nouveaux Postes créés : " + (posteCountAfter - posteCountBefore));

        // 10. Vérifier qu'on peut récupérer un utilisateur créé
        if (userCountAfter > userCountBefore) {
            assertFalse("La liste des utilisateurs ne doit pas être vide", usersAfter.isEmpty());
            User firstUser = usersAfter.getContent().get(0);
            assertNotNull("L'utilisateur doit avoir un email", firstUser.getEmailAddress());
            System.out.println("✓ Exemple d'utilisateur créé : " + firstUser.getEmailAddress());
        }

        // 11. Vérifier qu'on peut récupérer un poste créé
        if (posteCountAfter > posteCountBefore) {
            assertFalse("La liste des postes ne doit pas être vide", postesAfter.isEmpty());
            PosteAPourvoir firstPoste = postesAfter.getContent().get(0);
            assertNotNull("Le poste doit avoir un numéro d'emploi", firstPoste.getNumEmploi());
            System.out.println("✓ Exemple de poste créé : " + firstPoste.getNumEmploi());
        }
        
    }

    /**
     * Test 02 : Accès à l'upload de fichier Excel NON autorisé pour CANDIDAT
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test02_GalaxieImportNotAllowedForCandidat() throws Exception {
        File excelFile = new File("src/main/webapp/doc/EXTETBF.xls");
        if (!excelFile.exists()) {
            excelFile = new File("src/main/webapp/doc/COMMISSION.xls");
        }

        if (excelFile.exists()) {
            FileInputStream fis = new FileInputStream(excelFile);
            MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                excelFile.getName(),
                "application/vnd.ms-excel",
                fis
            );

            mockMvc.perform(multipart("/admin/galaxieexcels/addFile")
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
        mockMvc.perform(get("/admin/galaxieentrys/generatecandidatspostes")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 04 : Liste des GalaxieEntry accessible pour ADMIN
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test04_ListGalaxieEntriesAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin/galaxieentrys"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/galaxieentrys/list"))
                .andExpect(model().attributeExists("galaxieentrys"));
    }

    /**
     * Test 05 : Liste des GalaxieExcel accessible pour ADMIN
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test05_ListGalaxieExcelsAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin/galaxieexcels"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/galaxieexcels/list"));
    }
}

