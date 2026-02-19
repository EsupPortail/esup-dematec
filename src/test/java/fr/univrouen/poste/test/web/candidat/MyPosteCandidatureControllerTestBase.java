package fr.univrouen.poste.test.web.candidat;

import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MvcResult;


import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class MyPosteCandidatureControllerTestBase extends AbstractControllerTest {

    static String numEmploiPoste = null;

    static Long candidatureId = null;

    static Long candidatureFileId = null;

    protected void test_CandidatListeCandidatures() throws Exception {
        MvcResult result = mockMvc.perform(get("/postecandidatures"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postecandidatures"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<PosteCandidature> candidatures = (Page<PosteCandidature>) result.getModelAndView()
                .getModel().get("postecandidatures");

        assertNotNull("La liste des candidatures ne doit pas être null", candidatures);
        System.out.println("✓ Nombre de candidatures pour " + "candidat@example.org" + " : " + candidatures.getContent().size());
    }

    void test_VerificationTelechargementFichier() throws Exception {

        // 2. Récupérer les détails de la candidature pour obtenir l'ID du fichier
        MvcResult candidatureDetailResult = mockMvc.perform(get("/postecandidatures/" + candidatureId))
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

        assertEquals(candidatureFileId, fichier.getId());

        System.out.println("✓ Fichier à télécharger : ID=" + fichier.getId() + ", Nom=" + fichier.getFilename());

        // 4. Télécharger le fichier
        MvcResult downloadResult = mockMvc.perform(get("/postecandidatures/" +candidatureId + "/" + candidatureFileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fichier.getFilename() + "\""))
                .andReturn();

        // 5. Vérifier que le contenu du fichier n'est pas vide
        byte[] downloadedContent = downloadResult.getResponse().getContentAsByteArray();
        assertTrue("Le contenu du fichier téléchargé ne doit pas être vide", downloadedContent.length > 0);

        System.out.println("✓ Fichier téléchargé avec succès : " + downloadedContent.length + " octets");
    }

    void test_VerificationAccesAInterdit() throws Exception {
        assertNotNull("L'ID de la candidature doit avoir été déterminé par le test précédent", candidatureId);
        assertNotNull("L'ID du fichier doit avoir été déterminé par le test précédent", candidatureFileId);

        // 1. Tenter d'accéder à la candidature de l'autre candidat
        mockMvc.perform(get("/postecandidatures/" + candidatureId))
                .andExpect(status().isForbidden());

        System.out.println("✓ Accès à la candidature interdit pour candidat2@example.org");

        // 2. Tenter d'accéder au fichier de l'autre candidat
        mockMvc.perform(get("/postecandidatures/" + candidatureId + "/" + candidatureFileId))
                .andExpect(status().isForbidden());

        System.out.println("✓ Accès au fichier interdit pour candidat2@example.org");

    }
}
