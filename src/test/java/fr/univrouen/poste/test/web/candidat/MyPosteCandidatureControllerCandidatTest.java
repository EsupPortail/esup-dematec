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

import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.test.TestUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class MyPosteCandidatureControllerCandidatTest extends MyPosteCandidatureControllerTestBase {


    /**
     * Test 01 : Le candidat liste ses candidatures
     * Vérifie que le candidat peut accéder à la liste de ses candidatures
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test01_CandidatListeSesCandidatures() throws Exception {
        test_CandidatListeCandidatures();
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
        Page<PosteCandidature> candidatures = (Page<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        assertNotNull("La liste des candidatures ne doit pas être null", candidatures);
        assertFalse("Le candidat doit avoir au moins une candidature", candidatures.isEmpty());
        assertTrue("Le candidat doit avoir une seule candidature", candidatures.getTotalElements() == 1);

        // Récupérer la première candidature
        PosteCandidature premiereCandidature = candidatures.getContent().get(0);
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
        Page<PosteCandidature> candidatures = (Page<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        PosteCandidature candidature = candidatures.getContent().stream()
                .filter(c -> numEmploiPoste.equals(c.getPoste().getNumEmploi()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Candidature pour le poste " + numEmploiPoste + " introuvable"));

        System.out.println("✓ Candidature trouvée : ID=" + candidature.getId());

        // 1 - bis - charger la vue de la candidature
        result = mockMvc.perform(get("/postecandidatures/" + candidature.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidature"))
                .andExpect(model().attributeExists("fileTypes"))
                .andReturn();

        List<AppliConfigFileType> fileTypes = (List<AppliConfigFileType>) result.getModelAndView()
                .getModel().get("fileTypes");
        assertNotNull("La liste des types de fichiers ne doit pas être null", fileTypes);

        AppliConfigFileType fileType = fileTypes.get(0);


        // 2. Charger le fichier PDF test
        MockMultipartFile multipartFile = TestUtils.getPdfFile();

        // 3. Upload le fichier sur la candidature
        mockMvc.perform(multipart("/postecandidatures/" + candidature.getId() + "/addFile")
                .file(multipartFile)
                .param("fileType", fileType.getId().toString())
                .with(csrf()))
                .andExpect(status().is3xxRedirection());

        System.out.println("✓ Fichier doc-dummy.pdf uploadé avec succès sur la candidature ID=" + candidature.getId());
    }

    /**
     * Test 04 : Vérification que le fichier a été uploadé en listant les fichiers
     * Vérifie que le fichier uploadé apparaît bien dans la liste des fichiers de la candidature
     */
    @Test
    @WithUserDetails("candidat@example.org")
    public void test04_VerificationFichierUpload() throws Exception {
        assertNotNull("Le numéro de poste doit avoir été déterminé par le test précédent", numEmploiPoste);

        // 1. Récupérer la candidature pour le poste
        MvcResult result = mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<PosteCandidature> candidatures = (Page<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        PosteCandidature candidature = candidatures.getContent().stream()
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

        candidatureId = candidature.getId();

        assertNotNull("La candidature ne doit pas être null", candidatureDetail);
        assertNotNull("La liste des fichiers ne doit pas être null", candidatureDetail.getCandidatureFiles());
        assertFalse("Au moins un fichier doit être présent", candidatureDetail.getCandidatureFiles().isEmpty());

        // 3. Vérifier qu'un fichier avec le bon nom existe
        PosteCandidatureFile posteCandidatureFile = candidatureDetail.getCandidatureFiles()
                .stream().filter(f -> "doc-dummy.pdf".equals(f.getFilename()))
                .findFirst().get();
        boolean fichierTrouve = posteCandidatureFile != null;
        assertTrue("Le fichier doc-dummy.pdf doit être présent", fichierTrouve);

        candidatureFileId = posteCandidatureFile.getId();

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
    public void test05_VerificationTelechargementFichierCandidat() throws Exception {
        test_VerificationTelechargementFichier();
    }

    /*
        * Test 06 : Vérifier qu'un autre candidat ne peut pas accéder à la candidature ni au fichier
        * Vérifie que le candidat "candidat2@example.org" ne peut pas accéder à la candidature ni au fichier de "candidat@example.org"
     */
    @Test
    @WithUserDetails("candidat2@example.org")
    public void test06_VerificationAccesAutreCandidat() throws Exception {
        test_VerificationAccesAInterdit();
    }

}

