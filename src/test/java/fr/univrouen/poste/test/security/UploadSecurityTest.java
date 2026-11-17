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

import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Rollback
@WithUserDetails("admin")
public class UploadSecurityTest extends AbstractControllerTest {
    private static final String TITLE = "upload-security-test";

    private AppliConfigFileType createType() throws Exception {
        mockMvc.perform(post("/admin/appliconfigfiletype").with(csrf())
                .param("typeTitle", TITLE).param("candidatureNbFileMax", "1")
                .param("candidatureFileMoSizeMax", "1")
                .param("candidatureContentTypeRestrictionRegexp", "application/pdf")
                .param("candidatureFilenameRestrictionRegexp", ".*\\.pdf"))
                .andExpect(status().is3xxRedirection());
        return type();
    }

    private AppliConfigFileType type() throws Exception {
        Page<AppliConfigFileType> types = (Page<AppliConfigFileType>) mockMvc.perform(get("/admin/appliconfigfiletype")
                .param("size", "100")).andReturn().getModelAndView().getModel().get("appliconfigfiletypes");
        return types.stream().filter(t -> TITLE.equals(t.getTypeTitle())).findFirst().orElseThrow();
    }

    private String candidature() throws Exception {
        Page<PosteCandidature> page = (Page<PosteCandidature>) mockMvc.perform(get("/postecandidatures")
                .with(user("candidat@example.org").roles("CANDIDAT")))
                .andReturn().getModelAndView().getModel().get("postecandidatures");
        return "/postecandidatures/" + page.getContent().get(0).getId();
    }

    private int count(String url) throws Exception {
        PosteCandidature candidature = (PosteCandidature) mockMvc.perform(get(url))
                .andReturn().getModelAndView().getModel().get("postecandidature");
        return candidature.getCandidatureFiles().size();
    }

    @Test
    public void quotaIsEnforcedOnDirectRequests() throws Exception {
        AppliConfigFileType type = createType();
        String url = candidature();
        int before = count(url);
        byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("src/test/resources/doc-dummy.pdf"));
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(multipart(url + "/addFile")
                    .file(new MockMultipartFile("file", "quota-" + i + ".pdf", "application/pdf", bytes))
                    .param("fileType", type.getId().toString())
                    .with(user("candidat@example.org").roles("CANDIDAT")).with(csrf()))
                    .andExpect(status().is3xxRedirection());
            assertEquals(before + 1, count(url));
        }
    }

    @Test
    public void nestedParametersCannotChangeRestrictionsOrAuthorizeUnknownTypes() throws Exception {
        AppliConfigFileType type = createType();
        String url = candidature();
        int before = count(url);
        mockMvc.perform(multipart(url + "/addFile")
                .file(new MockMultipartFile("file", "forbidden.txt", "text/plain", new byte[]{1}))
                .param("fileType", type.getId().toString())
                .param("fileType.candidatureContentTypeRestrictionRegexp", ".*")
                .param("fileType.candidatureFilenameRestrictionRegexp", ".*")
                .param("fileType.candidatureNbFileMax", "-1")
                .with(user("candidat@example.org").roles("CANDIDAT")).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(before, count(url));
        assertEquals("application/pdf", type().getCandidatureContentTypeRestrictionRegexp());
        assertEquals(".*\\.pdf", type().getCandidatureFilenameRestrictionRegexp());
        assertEquals(Long.valueOf(1), type().getCandidatureNbFileMax());
        for (String id : new String[]{"", "999999999", "invalid"}) {
            mockMvc.perform(multipart(url + "/addFile")
                    .file(new MockMultipartFile("file", "bad.pdf", "application/pdf", new byte[]{1}))
                    .param("fileType", id).with(user("candidat@example.org").roles("CANDIDAT")).with(csrf()))
                    .andExpect(status().is3xxRedirection());
            assertEquals(before, count(url));
        }
        mockMvc.perform(multipart(url + "/addFile")
                .file(new MockMultipartFile("file", "bad.pdf", "application/pdf", new byte[]{1}))
                .with(user("candidat@example.org").roles("CANDIDAT")).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(before, count(url));
    }

    @Test
    public void missingMimeTypeIsRejectedWithoutServerError() throws Exception {
        AppliConfigFileType type = createType();
        String url = candidature();
        int before = count(url);
        mockMvc.perform(multipart(url + "/addFile")
                .file(new MockMultipartFile("file", "bad.pdf", null, new byte[]{1}))
                .param("fileType", type.getId().toString())
                .with(user("candidat@example.org").roles("CANDIDAT")).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(before, count(url));
    }
}
