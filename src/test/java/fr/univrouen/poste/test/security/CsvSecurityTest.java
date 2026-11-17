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
package fr.univrouen.poste.test.security;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import fr.univrouen.poste.domain.*;
import fr.univrouen.poste.test.AbstractControllerTest;
import fr.univrouen.poste.test.TestUtils;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Rollback
@WithUserDetails("admin")
public class CsvSecurityTest extends AbstractControllerTest {
    @Test
    public void exportNeutralizesFormulasAndPreservesColumnsWhenTagNamesCollide() throws Exception {
        Page<PosteCandidature> candidatures = (Page<PosteCandidature>) mockMvc.perform(get("/postecandidatures"))
                .andReturn().getModelAndView().getModel().get("postecandidatures");
        PosteCandidature candidature = candidatures.getContent().get(0);
        User candidat = (User) mockMvc.perform(get("/admin/users/" + candidature.getCandidat().getId()))
                .andReturn().getModelAndView().getModel().get("user");
        var params = TestUtils.getParamsAsStringMap(candidat, "postes", "password");
        params.set("nom", "=1+1");
        params.set("prenom", " +2+2");
        mockMvc.perform(put("/admin/users").with(csrf()).params(params))
                .andExpect(status().is3xxRedirection());
        String tagUrl = mockMvc.perform(post("/admin/candidaturetags").with(csrf()).param("name", "nom"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        mockMvc.perform(post(tagUrl).with(csrf()).param("create", "value").param("value", "@SUM(1,2)"))
                .andExpect(status().is3xxRedirection());
        PosteCandidatureTag tag = (PosteCandidatureTag) mockMvc.perform(get(tagUrl).param("form", ""))
                .andReturn().getModelAndView().getModel().get("posteCandidatureTag");
        mockMvc.perform(post("/postecandidatures/" + candidature.getId() + "/updateTags").with(csrf())
                .param("tags[" + tag.getId() + "]", tag.getValues().iterator().next().getId().toString()))
                .andExpect(status().is3xxRedirection());
        String formulaTagUrl = mockMvc.perform(post("/admin/candidaturetags").with(csrf()).param("name", "=header"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        assertNotNull(formulaTagUrl);
        String csv = mockMvc.perform(get("/postecandidatures").param("csv", "true"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        try (var reader = new CsvMapper().readerFor(Map.class).with(CsvSchema.emptySchema().withHeader()).readValues(csv)) {
            List<Map<String, String>> rows = (List) reader.readAll();
            Map<String, String> row = rows.stream().filter(r -> candidat.getEmailAddress().equals(r.get("email")))
                    .findFirst().orElseThrow();
            assertEquals("'=1+1", row.get("nom"));
            assertEquals("' +2+2", row.get("prenom"));
            assertEquals("'@SUM(1,2)", row.get("tag:nom"));
            assertTrue(row.containsKey("'=header"));
        }
    }
    @org.springframework.beans.factory.annotation.Autowired
    private fr.univrouen.poste.services.ArchiveService archiveService;

    @Test
    public void archiveMetadataNeutralizesFormulaFilenames() throws Exception {
        String url = mockMvc.perform(post("/posteapourvoirs").with(csrf()).param("numEmploi", "CSV-METADATA"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        String filename = "=SUM(1,2).txt";
        mockMvc.perform(multipart(url + "/addFile")
                .file(new org.springframework.mock.web.MockMultipartFile("file", filename, "text/plain", new byte[]{1}))
                .with(csrf())).andExpect(status().is3xxRedirection());
        var parent = java.nio.file.Files.createTempDirectory("csv-metadata-");
        try {
            var archive = parent.resolve("archive");
            archiveService.archive(archive.toString());
            var csv = archive.resolve("CSV-METADATA/Fichiers_Internes/metadata.csv");
            try (var input = java.nio.file.Files.newBufferedReader(csv);
                 var rows = new CsvMapper().readerFor(Map.class).with(CsvSchema.emptySchema().withHeader()).readValues(input)) {
                Map<String, String> row = (Map) rows.nextValue();
                assertEquals("'" + filename, row.get("filename"));
                assertFalse(rows.hasNextValue());
            }
        } finally {
            org.apache.commons.io.FileUtils.deleteDirectory(parent.toFile());
        }
    }
}
