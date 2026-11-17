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

import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.test.AbstractBatchTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real committed controller operations: revocation must occur only after commit. */
public class SessionRevocationTest extends AbstractBatchTest {
    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        webApplicationContext.getServletContext().setAttribute(
                org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                webApplicationContext);
        var viewFilter = new org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter();
        viewFilter.init(new org.springframework.mock.web.MockFilterConfig(webApplicationContext.getServletContext()));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(viewFilter)
                .apply(springSecurity()).build();
    }

    private User read(String email) throws Exception {
        Page<User> users = (Page<User>) mockMvc.perform(get("/admin/users")
                .with(user("admin").roles("ADMIN")).param("nomOrPrenomOrEmailAddress", email))
                .andReturn().getModelAndView().getModel().get("users");
        return users.getContent().get(0);
    }

    @Test
    public void accountAndPasswordMutationsRevokeExistingSessions() throws Exception {
        for (String action : new String[]{"disable", "demote", "password", "rename", "reset", "change", "delete"}) {
            String email = "session-" + action + "@example.org";
            mockMvc.perform(post("/admin/users").with(user("admin").roles("ADMIN")).with(csrf())
                    .param("emailAddress", email).param("password", "Initial123!")
                    .param("isAdmin", "true").param("isManager", "false").param("isSuperManager", "false"))
                    .andExpect(status().is3xxRedirection());
            User target = read(email);
            mockMvc.perform(post("/admin/users").with(user("admin").roles("ADMIN")).with(csrf())
                    .param("id", target.getId().toString()).param("emailAddress", email).param("password", "Initial123!"))
                    .andExpect(status().isBadRequest());
            MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/login").with(csrf())
                    .param("username", email).param("password", "Initial123!"))
                    .andExpect(redirectedUrl("/")).andReturn().getRequest().getSession(false);
            assertNotNull(session);
            mockMvc.perform(get("/admin").session(session)).andExpect(status().isOk())
                    .andExpect(view().name("admin"));
            try {
                if (action.equals("delete")) {
                    mockMvc.perform(delete("/admin/users/" + target.getId())
                            .with(user("admin").roles("ADMIN")).with(csrf()))
                            .andExpect(status().is3xxRedirection());
                } else if (action.equals("reset")) {
                    mockMvc.perform(post("/forgotpassword/update").with(csrf()).param("emailAddress", email))
                            .andExpect(view().name("forgotpassword/thanks"));
                    String key = read(email).getActivationKey();
                    assertNotNull(key);
                    mockMvc.perform(post("/forgotpassword/change").with(csrf()).param("activationKey", key)
                            .param("newPassword", "Replacement123!").param("newPasswordAgain", "Replacement123!"))
                            .andExpect(status().is3xxRedirection());
                } else if (action.equals("change")) {
                    mockMvc.perform(post("/changepassword/update").session(session).with(csrf())
                            .param("oldPassword", "Initial123!").param("newPassword", "Replacement123!")
                            .param("newPasswordAgain", "Replacement123!"))
                            .andExpect(view().name("changepassword/thanks"));
                } else {
                    var request = put("/admin/users").with(user("admin").roles("ADMIN")).with(csrf())
                            .param("id", target.getId().toString()).param("version", target.getVersion().toString())
                            .param("emailAddress", action.equals("rename") ? "renamed-" + email : email)
                            .param("enabled", action.equals("disable") ? "false" : "true")
                            .param("isAdmin", action.equals("demote") ? "false" : "true")
                            .param("isManager", "false").param("isSuperManager", "false");
                    if (action.equals("password")) request.param("password", "Replacement123!");
                    mockMvc.perform(request).andExpect(status().is3xxRedirection());
                }
                mockMvc.perform(get("/admin").session(session))
                        .andExpect(content().string(containsString("session has been expired")));
                assertTrue(session.isInvalid());
            } finally {
                if (!action.equals("delete")) {
                    mockMvc.perform(delete("/admin/users/" + target.getId())
                            .with(user("admin").roles("ADMIN")).with(csrf()))
                            .andExpect(status().is3xxRedirection());
                }
            }
        }
    }
}
