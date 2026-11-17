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

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.test.AbstractControllerTest;
import fr.univrouen.poste.test.TestUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Rollback
@WithUserDetails("admin")
public class CandidatureSignupFlowTest extends AbstractControllerTest {
    private long count(Long posteId) throws Exception {
        Page<PosteCandidature> page = (Page<PosteCandidature>) mockMvc.perform(get("/postecandidatures").param("size", "100"))
                .andExpect(status().isOk()).andReturn().getModelAndView().getModel().get("postecandidatures");
        return page.stream().filter(pc -> posteId.equals(pc.getPoste().getId())).count();
    }

    @Test
    public void signupPageAndCancellationRespectPosteDeadline() throws Exception {
        Page<AppliConfig> configs = (Page<AppliConfig>) mockMvc.perform(get("/admin/appliconfig"))
                .andReturn().getModelAndView().getModel().get("appliconfigs");
        AppliConfig config = configs.getContent().get(0);
        var original = TestUtils.getParamsAsStringMap(config);
        var enabled = TestUtils.getParamsAsStringMap(config);
        enabled.set("candidatCanSignup", "true");
        try {
            mockMvc.perform(put("/admin/appliconfig").with(csrf()).params(enabled))
                    .andExpect(status().is3xxRedirection());
            PosteAPourvoir poste = new PosteAPourvoir();
            poste.setNumEmploi("SIGNUP-FLOW");
            poste.setProfil("Test candidature libre");
            poste.setMembres(new HashSet<>());
            poste.setPresidents(new HashSet<>());
            poste.setDateEndSignupCandidat(LocalDateTime.now().plusDays(1));
            String location = mockMvc.perform(post("/posteapourvoirs").with(csrf()).flashAttr("posteAPourvoir", poste))
                    .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
            Long id = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
            String html = mockMvc.perform(get("/addpostecandidatures")
                    .with(user("candidat@example.org").roles("CANDIDAT")))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertTrue(html.contains("SIGNUP-FLOW"));
            assertTrue(html.contains("name=\"posteIds\""));
            assertTrue(html.contains("name=\"_csrf\""));
            assertFalse(html.contains("/postecandidatures/new"));
            for (int i = 0; i < 2; i++) {
                mockMvc.perform(post("/addpostecandidatures").with(csrf())
                        .with(user("candidat@example.org").roles("CANDIDAT")).param("posteIds", id.toString()))
                        .andExpect(status().is3xxRedirection());
                assertEquals(1, count(id));
                if (i == 0) {
                    html = mockMvc.perform(get("/addpostecandidatures")
                            .with(user("candidat@example.org").roles("CANDIDAT")))
                            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
                    assertTrue(html.contains("value=\"DELETE\""));
                    mockMvc.perform(delete("/addpostecandidatures").with(csrf())
                            .with(user("candidat@example.org").roles("CANDIDAT")).param("posteIds", id.toString()))
                            .andExpect(status().is3xxRedirection());
                    assertEquals(0, count(id));
                }
            }
            PosteAPourvoir stored = (PosteAPourvoir) mockMvc.perform(get(location)).andReturn()
                    .getModelAndView().getModel().get("posteapourvoir");
            PosteAPourvoir closed = new PosteAPourvoir();
            BeanUtils.copyProperties(stored, closed);
            closed.setDateEndSignupCandidat(LocalDateTime.now().minusDays(1));
            mockMvc.perform(put("/posteapourvoirs").with(csrf()).flashAttr("posteAPourvoir", closed))
                    .andExpect(status().is3xxRedirection());
            mockMvc.perform(delete("/addpostecandidatures").with(csrf())
                    .with(user("candidat@example.org").roles("CANDIDAT"))
                    .param("posteIds", id.toString(), Long.toString(Long.MAX_VALUE)))
                    .andExpect(status().is3xxRedirection());
            assertEquals(1, count(id));
        } finally {
            AppliConfig current = (AppliConfig) mockMvc.perform(get("/admin/appliconfig/" + config.getId()))
                    .andReturn().getModelAndView().getModel().get("appliConfig");
            original.set("version", current.getVersion().toString());
            mockMvc.perform(put("/admin/appliconfig").with(csrf()).params(original))
                    .andExpect(status().is3xxRedirection());
        }
    }
}
