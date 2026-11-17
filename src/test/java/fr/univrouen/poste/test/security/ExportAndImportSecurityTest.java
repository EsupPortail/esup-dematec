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

import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Rollback
@WithUserDetails("admin")
public class ExportAndImportSecurityTest extends AbstractControllerTest {
    @Test
    public void generationRequiresPostAndCsrf() throws Exception {
        for (String url : new String[]{"/admin/galaxieentrys/generatecandidatspostes",
                "/admin/commissionentrys/generatecommissions"}) {
            mockMvc.perform(get(url)).andExpect(status().is4xxClientError());
            mockMvc.perform(post(url)).andExpect(status().isForbidden());
        }
        // Successful POST generation is exercised by both existing import test classes.
    }

    @Test
    public void posteZipContainsOnlySafeDistinctRelativeEntries() throws Exception {
        var created = mockMvc.perform(post("/posteapourvoirs").with(csrf()).param("numEmploi", "ZIP-SECURITY"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String location = created.getResponse().getRedirectedUrl();
        byte[] payload = "zip-content".getBytes(StandardCharsets.UTF_8);
        String[] names = {"../../outside.txt", "/absolute.txt", "C:\\temp\\report.txt", "nested/a.txt", "nested_a.txt"};
        for (String name : names) {
            mockMvc.perform(multipart(location + "/addFile").file(new MockMultipartFile("file", name, "text/plain", payload))
                    .with(csrf())).andExpect(status().is3xxRedirection());
        }
        byte[] zip = mockMvc.perform(get(location).param("export", "zip"))
                .andExpect(status().isOk()).andExpect(content().contentType("application/zip"))
                .andReturn().getResponse().getContentAsByteArray();
        var entries = new HashSet<String>();
        try (var input = new ZipInputStream(new ByteArrayInputStream(zip))) {
            java.util.zip.ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                String name = entry.getName();
                assertFalse(name.startsWith("/"));
                assertFalse(name.contains("/"));
                assertFalse(name.contains("\\"));
                assertFalse(name.contains(":"));
                assertTrue(entries.add(name));
                assertArrayEquals(payload, input.readAllBytes());
            }
        }
        assertEquals(names.length, entries.size());
    }
}
