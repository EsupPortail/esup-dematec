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
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour MyPosteCandidatureController - Parcours complet d'un candidat
 *
 * Ce test simule le processus naturel d'un candidat (candidat@example.org) qui :
 * 1. Liste ses candidatures
 * 2. Vérifie qu'il a une candidature (et récupère le numéro de poste)
 * 3. Upload un fichier de candidature
 * 4. Vérifie que le fichier a été uploadé
 * 5. Télécharge le fichier
 *
 * Note: Ce test dépend des données créées par GalaxieImportControllerTest qui doit s'exécuter en premier.
 * Les candidats, postes et candidatures sont créés via l'import du fichier Excel.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MyPosteCandidatureControllerCandidatTest extends AbstractControllerTest {

    // Variable statique pour stocker le numéro de poste trouvé lors du test 02
    private static String numEmploiPoste = null;

    /**
     * Test 01 : Le candidat liste ses candidatures
     * Vérifie que le candidat peut accéder à la liste de ses candidatures
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test01_CandidatListeSesCandidatures() throws Exception {
        MvcResult result = mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<PosteCandidature> candidatures = (List<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        assertNotNull("La liste des candidatures ne doit pas être null", candidatures);
        System.out.println("✓ Nombre de candidatures pour " + "candidat@example.org" + " : " + candidatures.size());
    }

    /**
     * Test 02 : Vérification que le candidat a au moins une candidature et récupération du numéro de poste
     * Vérifie qu'une candidature existe et stocke le numéro de poste pour les tests suivants
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test02_VerificationCandidatureExistante() throws Exception {
        MvcResult result = mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<PosteCandidature> candidatures = (List<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        assertNotNull("La liste des candidatures ne doit pas être null", candidatures);
        assertFalse("Le candidat doit avoir au moins une candidature", candidatures.isEmpty());

        // Récupérer la première candidature
        PosteCandidature premiereCandidature = candidatures.get(0);
        assertNotNull("La candidature ne doit pas être null", premiereCandidature);
        assertNotNull("Le poste de la candidature ne doit pas être null", premiereCandidature.getPoste());

        // Stocker le numéro d'emploi pour les tests suivants
        numEmploiPoste = premiereCandidature.getPoste().getNumEmploi();
        assertNotNull("Le numéro d'emploi ne doit pas être null", numEmploiPoste);

        assertEquals("L'email du candidat doit correspondre",
                "candidat@example.org",
                     premiereCandidature.getCandidat().getEmailAddress());

        System.out.println("✓ Candidature trouvée pour le poste " + numEmploiPoste +
                           " (ID=" + premiereCandidature.getId() + ")");
    }

    /**
     * Test 03 : Le candidat upload un fichier de candidature
     * Vérifie que le candidat peut uploader un fichier PDF sur sa candidature
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test03_CandidatUploadFichier() throws Exception {
        assertNotNull("Le numéro de poste doit avoir été déterminé par le test précédent", numEmploiPoste);

        // 1. Récupérer la candidature pour le poste
        MvcResult result = mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<PosteCandidature> candidatures = (List<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        PosteCandidature candidature = candidatures.stream()
                .filter(c -> numEmploiPoste.equals(c.getPoste().getNumEmploi()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Candidature pour le poste " + numEmploiPoste + " introuvable"));

        System.out.println("✓ Candidature trouvée : ID=" + candidature.getId());

        // 2. Charger le fichier PDF test
        File pdfFile = new File("src/test/resources/doc-dummy.pdf");
        assertTrue("Le fichier doc-dummy.pdf doit exister", pdfFile.exists());

        FileInputStream fis = new FileInputStream(pdfFile);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "doc-dummy.pdf",
                "application/pdf",
                fis
        );

        // 3. Upload le fichier sur la candidature
        mockMvc.perform(multipart("/postecandidatures/" + candidature.getId() + "/addFile")
                .file(multipartFile)
                .with(csrf()))
                .andExpect(status().isOk());

        System.out.println("✓ Fichier doc-dummy.pdf uploadé avec succès sur la candidature ID=" + candidature.getId());
    }

    /**
     * Test 04 : Vérification que le fichier a été uploadé en listant les fichiers
     * Vérifie que le fichier uploadé apparaît bien dans la liste des fichiers de la candidature
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test04_VerificationFichierUploade() throws Exception {
        assertNotNull("Le numéro de poste doit avoir été déterminé par le test précédent", numEmploiPoste);

        // 1. Récupérer la candidature pour le poste
        MvcResult result = mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<PosteCandidature> candidatures = (List<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        PosteCandidature candidature = candidatures.stream()
                .filter(c -> numEmploiPoste.equals(c.getPoste().getNumEmploi()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Candidature pour le poste " + numEmploiPoste + " introuvable"));

        // 2. Afficher la candidature et vérifier que le fichier est présent
        MvcResult candidatureDetailResult = mockMvc.perform(get("/postecandidatures/" + candidature.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidature"))
                .andReturn();

        PosteCandidature candidatureDetail = (PosteCandidature) candidatureDetailResult.getModelAndView()
                .getModel().get("postecandidature");

        assertNotNull("La candidature ne doit pas être null", candidatureDetail);
        assertNotNull("La liste des fichiers ne doit pas être null", candidatureDetail.getCandidatureFiles());
        assertFalse("Au moins un fichier doit être présent", candidatureDetail.getCandidatureFiles().isEmpty());

        // 3. Vérifier qu'un fichier avec le bon nom existe
        boolean fichierTrouve = candidatureDetail.getCandidatureFiles().stream()
                .anyMatch(f -> "doc-dummy.pdf".equals(f.getFilename()));

        assertTrue("Le fichier doc-dummy.pdf doit être présent", fichierTrouve);

        System.out.println("✓ Nombre de fichiers dans la candidature : " + candidatureDetail.getCandidatureFiles().size());
        candidatureDetail.getCandidatureFiles().forEach(f ->
            System.out.println("  - " + f.getFilename() + " (" + f.getFileSize() + " octets)")
        );
    }

    /**
     * Test 05 : Vérification du téléchargement du fichier
     * Vérifie que le candidat peut télécharger le fichier uploadé
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test05_VerificationTelechargementFichier() throws Exception {
        assertNotNull("Le numéro de poste doit avoir été déterminé par le test précédent", numEmploiPoste);

        // 1. Récupérer la candidature pour le poste
        MvcResult result = mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<PosteCandidature> candidatures = (List<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        PosteCandidature candidature = candidatures.stream()
                .filter(c -> numEmploiPoste.equals(c.getPoste().getNumEmploi()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Candidature pour le poste " + numEmploiPoste + " introuvable"));

        // 2. Récupérer les détails de la candidature pour obtenir l'ID du fichier
        MvcResult candidatureDetailResult = mockMvc.perform(get("/postecandidatures/" + candidature.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidature"))
                .andReturn();

        PosteCandidature candidatureDetail = (PosteCandidature) candidatureDetailResult.getModelAndView()
                .getModel().get("postecandidature");

        // 3. Récupérer le premier fichier
        PosteCandidatureFile fichier = candidatureDetail.getCandidatureFiles().stream()
                .filter(f -> "doc-dummy.pdf".equals(f.getFilename()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Fichier doc-dummy.pdf introuvable"));

        System.out.println("✓ Fichier à télécharger : ID=" + fichier.getId() + ", Nom=" + fichier.getFilename());

        // 4. Télécharger le fichier
        MvcResult downloadResult = mockMvc.perform(get("/postecandidatures/" + candidature.getId() + "/" + fichier.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fichier.getFilename() + "\""))
                .andReturn();

        // 5. Vérifier que le contenu du fichier n'est pas vide
        byte[] downloadedContent = downloadResult.getResponse().getContentAsByteArray();
        assertTrue("Le contenu du fichier téléchargé ne doit pas être vide", downloadedContent.length > 0);

        System.out.println("✓ Fichier téléchargé avec succès : " + downloadedContent.length + " octets");
    }
}

