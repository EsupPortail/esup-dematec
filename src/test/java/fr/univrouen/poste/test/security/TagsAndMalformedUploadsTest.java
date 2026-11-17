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

import fr.univrouen.poste.domain.*;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Rollback
@WithUserDetails("admin")
public class TagsAndMalformedUploadsTest extends AbstractControllerTest {
    @Test
    public void tagNamesAndValuesAreEscapedInCandidatureList() throws Exception {
        String name = "<script id=\"tag-regression\">alert(1)</script>";
        String value = "<img src=x onerror=\"alert('tag-value')\">";
        String url = mockMvc.perform(post("/admin/candidaturetags").with(csrf()).param("name", name))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        mockMvc.perform(post(url).with(csrf()).param("create", "value").param("value", value))
                .andExpect(status().is3xxRedirection());
        PosteCandidatureTag tag = (PosteCandidatureTag) mockMvc.perform(get(url).param("form", ""))
                .andExpect(status().isOk()).andReturn().getModelAndView().getModel().get("posteCandidatureTag");
        PosteCandidatureTagValue tagValue = tag.getValues().iterator().next();
        Page<PosteCandidature> page = (Page<PosteCandidature>) mockMvc.perform(get("/postecandidatures"))
                .andReturn().getModelAndView().getModel().get("postecandidatures");
        Long id = page.getContent().get(0).getId();
        mockMvc.perform(post("/postecandidatures/" + id + "/updateTags").with(csrf())
                .param("tags[" + tag.getId() + "]", tagValue.getId().toString()))
                .andExpect(status().is3xxRedirection());
        String html = mockMvc.perform(get("/postecandidatures").param("size", "100"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(html.contains(name));
        assertFalse(html.contains(value));
        assertTrue(html.contains("&lt;script id=&quot;tag-regression&quot;&gt;"));
        assertTrue(html.contains("&lt;img src=x onerror=&quot;"));
    }

    @Test
    public void administrativeUploadsRejectMissingEmptyOrUnnamedFiles() throws Exception {
        for (String kind : new String[]{"galaxieexcels", "commissionexcels", "templatefiles"}) {
            String url = "/admin/" + kind;
            Page<?> before = (Page<?>) mockMvc.perform(get(url)).andReturn().getModelAndView().getModel().get(kind);
            mockMvc.perform(post(url + "/addFile").with(csrf()))
                    .andExpect(redirectedUrl(url));
            for (MockMultipartFile file : new MockMultipartFile[]{
                    new MockMultipartFile("file", "empty.xls", "application/vnd.ms-excel", new byte[0]),
                    new MockMultipartFile("file", " ", "application/octet-stream", new byte[]{1})}) {
                mockMvc.perform(multipart(url + "/addFile").file(file).with(csrf()))
                        .andExpect(redirectedUrl(url));
            }
            Page<?> after = (Page<?>) mockMvc.perform(get(url)).andReturn().getModelAndView().getModel().get(kind);
            assertEquals(before.getTotalElements(), after.getTotalElements());
        }
    }

    @Test
    public void posteUploadAcceptsMissingMimeWithoutServerError() throws Exception {
        String url = mockMvc.perform(post("/posteapourvoirs").with(csrf()).param("numEmploi", "MIME-REGRESSION"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        mockMvc.perform(multipart(url + "/addFile")
                .file(new MockMultipartFile("file", "test.bin", null, new byte[]{1, 2})).with(csrf()))
                .andExpect(redirectedUrl(url));
        PosteAPourvoir poste = (PosteAPourvoir) mockMvc.perform(get(url)).andReturn()
                .getModelAndView().getModel().get("posteapourvoir");
        assertEquals(1, poste.getPosteFiles().size());
        assertEquals("application/octet-stream", poste.getPosteFiles().iterator().next().getContentType());
    }
}
