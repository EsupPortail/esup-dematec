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
/**
 * 
 */
package fr.univrouen.poste.test.security;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Rollback
@WithUserDetails("admin")
public class CredentialRegressionTest extends AbstractControllerTest {
    private static final String EMAIL = "credential-regression@example.org";
    private static final String PASSWORD = "Spécial'\"<&$%_\\+123!";

    private User findUser() throws Exception {
        Page<User> users = (Page<User>) mockMvc.perform(get("/admin/users")
                .param("nomOrPrenomOrEmailAddress", EMAIL)).andReturn()
                .getModelAndView().getModel().get("users");
        assertEquals(1, users.getTotalElements());
        return users.getContent().get(0);
    }

    private String signup() throws Exception {
        return signup("Test", "Credentials");
    }

    private String signup(String firstName, String lastName) throws Exception {
        Page<AppliConfig> configs = (Page<AppliConfig>) mockMvc.perform(get("/admin/appliconfig"))
                .andReturn().getModelAndView().getModel().get("appliconfigs");
        AppliConfig original = configs.getContent().get(0);
        AppliConfig copy = new AppliConfig();
        BeanUtils.copyProperties(original, copy);
        copy.setCandidatCanSignup(true);
        mockMvc.perform(put("/admin/appliconfig").with(csrf()).flashAttr("appliConfig", copy))
                .andExpect(status().is3xxRedirection());
        try {
            mockMvc.perform(post("/signup").with(anonymous()).with(csrf())
                    .param("civilite", "M.").param("firstName", firstName).param("lastName", lastName)
                    .param("emailAddress", EMAIL)).andExpect(view().name("signup/thanks"));
        } finally {
            mockMvc.perform(put("/admin/appliconfig").with(csrf()).flashAttr("appliConfig", original))
                    .andExpect(status().is3xxRedirection());
        }
        return findUser().getActivationKey();
    }

    private MockHttpServletRequestBuilder init(String key, String password) {
        return init(key, password, EMAIL);
    }

    private MockHttpServletRequestBuilder init(String key, String password, String email) {
        var request = post("/signup/initpassword").with(anonymous()).with(csrf()).param("emailAddress", email);
        if (key != null) request.param("activationKey", key);
        if (password != null) request.param("password", password).param("repeatPassword", password);
        return request;
    }

    @Test
    public void initializationRejectsMalformedCredentialsAndConsumesKey() throws Exception {
        String key = signup();
        mockMvc.perform(post("/forgotpassword/change").with(anonymous()).with(csrf()).param("activationKey", key)
                .param("newPassword", PASSWORD).param("newPasswordAgain", PASSWORD))
                .andExpect(model().attributeHasErrors("changePasswordForm"));
        assertNull(findUser().getPassword());
        for (String invalid : new String[]{null, "", " ", "\t", "\u00a0", "\u0000", "a".repeat(73), "é".repeat(37)}) {
            mockMvc.perform(init(key, invalid)).andExpect(view().name("signup/error"));
            assertNull(findUser().getPassword());
            assertEquals(key, findUser().getActivationKey());
        }
        for (String invalid : new String[]{null, "", " ", "' OR 1=1 --", "%", "_", key + " ", key.toUpperCase()}) {
            if (key.equals(invalid)) continue;
            mockMvc.perform(init(invalid, PASSWORD)).andExpect(view().name("signup/error"));
            if (invalid != null) {
                mockMvc.perform(get("/signup").with(anonymous()).param("activate", invalid).param("emailAddress", EMAIL))
                        .andExpect(view().name("signup/errorlink"));
                mockMvc.perform(get("/forgotpassword/formChange").with(anonymous()).param("activationKey", invalid))
                        .andExpect(status().is3xxRedirection());
            }
            assertNull(findUser().getPassword());
        }
        for (String invalidEmail : new String[]{"", " ", "invalid", "other@example.org", "' OR 1=1 --"}) {
            mockMvc.perform(init(key, PASSWORD, invalidEmail))
                    .andExpect(view().name("signup/error"));
            assertNull(findUser().getPassword());
        }
        mockMvc.perform(post("/signup/initpassword").with(anonymous()).with(csrf())
                .param("emailAddress", EMAIL).param("activationKey", key)
                .param("password", PASSWORD).param("repeatPassword", "different"))
                .andExpect(view().name("signup/error"));
        mockMvc.perform(post("/signup/initpassword").with(anonymous()).with(csrf())
                .param("activationKey", key).param("password", PASSWORD))
                .andExpect(view().name("signup/error"));
        mockMvc.perform(init(key, PASSWORD)).andExpect(view().name("login"));
        assertNull(findUser().getActivationKey());
        mockMvc.perform(init(key, "replacement")).andExpect(view().name("signup/error"));
        mockMvc.perform(post("/login").with(anonymous()).with(csrf()).param("username", EMAIL)
                .param("password", PASSWORD)).andExpect(redirectedUrl("/"));
        for (String invalid : new String[]{"", " ", PASSWORD + "x", "' OR 1=1 --", PASSWORD.toUpperCase(), PASSWORD + "\u0000"}) {
            mockMvc.perform(post("/login").with(anonymous()).with(csrf()).param("username", EMAIL)
                    .param("password", invalid)).andExpect(redirectedUrl("/login?error"));
        }
    }

    @Test
    public void oldActivationAndResetLinksCannotEnableDisabledAccount() throws Exception {
        String key = signup();
        mockMvc.perform(init(key, PASSWORD)).andExpect(view().name("login"));
        User user = findUser();
        mockMvc.perform(put("/admin/users").with(csrf()).param("id", user.getId().toString())
                .param("version", user.getVersion().toString()).param("emailAddress", EMAIL)
                .param("nom", "Credentials").param("prenom", "Test").param("civilite", "M.")
                .param("enabled", "false").param("isAdmin", "false").param("isManager", "false")
                .param("isSuperManager", "false")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/signup").with(anonymous()).param("activate", key).param("emailAddress", EMAIL))
                .andExpect(view().name("signup/errorlink"));
        assertFalse(findUser().getEnabled());
        mockMvc.perform(post("/forgotpassword/update").with(anonymous()).with(csrf()).param("emailAddress", EMAIL))
                .andExpect(view().name("forgotpassword/thanks"));
        String resetKey = findUser().getActivationKey();
        assertNotNull(resetKey);
        mockMvc.perform(get("/signup").with(anonymous()).param("activate", resetKey).param("emailAddress", EMAIL))
                .andExpect(view().name("signup/errorlink"));
        assertFalse(findUser().getEnabled());
    }

    @Test
    public void authenticatedPasswordChangeRejectsEmptyAndWrongOldPasswords() throws Exception {
        for (String invalid : new String[]{null, "", " ", "\u0000", "é".repeat(37)}) {
            var request = post("/changepassword/update").with(csrf()).param("oldPassword", "admin");
            if (invalid != null) request.param("newPassword", invalid).param("newPasswordAgain", invalid);
            mockMvc.perform(request).andExpect(view().name("changepassword/index"))
                    .andExpect(model().attributeHasErrors("changePasswordForm"));
        }
        for (String invalid : new String[]{"", " ", "admin\u0000", "admin' OR 1=1 --"}) {
            mockMvc.perform(post("/changepassword/update").with(csrf()).param("oldPassword", invalid)
                    .param("newPassword", PASSWORD).param("newPasswordAgain", PASSWORD))
                    .andExpect(model().attributeHasFieldErrors("changePasswordForm", "oldPassword"));
        }
        mockMvc.perform(post("/login").with(anonymous()).with(csrf()).param("username", "admin")
                .param("password", "admin")).andExpect(redirectedUrl("/"));
        mockMvc.perform(post("/changepassword/update").with(csrf()).param("oldPassword", "admin")
                .param("newPassword", PASSWORD).param("newPasswordAgain", PASSWORD))
                .andExpect(view().name("changepassword/thanks"));
        mockMvc.perform(post("/login").with(anonymous()).with(csrf()).param("username", "admin")
                .param("password", PASSWORD)).andExpect(redirectedUrl("/"));
    }

    @Test
    public void resetRejectsInvalidPasswordsAndReusedOrAlteredKeys() throws Exception {
        String key = signup();
        mockMvc.perform(init(key, PASSWORD)).andExpect(view().name("login"));
        mockMvc.perform(post("/forgotpassword/update").with(anonymous()).with(csrf()).param("emailAddress", EMAIL));
        String resetKey = findUser().getActivationKey();
        String hash = findUser().getPassword();
        for (String invalid : new String[]{null, "", " ", "\u0000", "a".repeat(73)}) {
            var request = post("/forgotpassword/change").with(anonymous()).with(csrf()).param("activationKey", resetKey);
            if (invalid != null) request.param("newPassword", invalid).param("newPasswordAgain", invalid);
            mockMvc.perform(request).andExpect(view().name("forgotpassword/formChange"))
                    .andExpect(model().attributeHasErrors("changePasswordForm"));
            assertEquals(hash, findUser().getPassword());
        }
        for (String invalid : new String[]{"", " ", "%", "_", "' OR 1=1 --", resetKey + " ", resetKey.toUpperCase()}) {
            if (resetKey.equals(invalid)) continue;
            mockMvc.perform(post("/forgotpassword/change").with(anonymous()).with(csrf()).param("activationKey", invalid)
                    .param("newPassword", "New123!").param("newPasswordAgain", "New123!"))
                    .andExpect(model().attributeHasErrors("changePasswordForm"));
            assertEquals(hash, findUser().getPassword());
        }
        mockMvc.perform(post("/forgotpassword/change").with(anonymous()).with(csrf()).param("activationKey", resetKey)
                .param("newPassword", "New123!").param("newPasswordAgain", "New123!"))
                .andExpect(status().is3xxRedirection());
        assertNull(findUser().getActivationKey());
        mockMvc.perform(post("/forgotpassword/change").with(anonymous()).with(csrf()).param("activationKey", resetKey)
                .param("newPassword", PASSWORD).param("newPasswordAgain", PASSWORD))
                .andExpect(model().attributeHasErrors("changePasswordForm"));
        mockMvc.perform(post("/login").with(anonymous()).with(csrf()).param("username", EMAIL)
                .param("password", "New123!")).andExpect(redirectedUrl("/"));
    }
    @org.springframework.beans.factory.annotation.Autowired
    private fr.univrouen.poste.services.ArchiveService archiveService;

    @Test
    public void archiveConfinesDotDirectoriesAndUploadedFilenames() throws Exception {
        var created = mockMvc.perform(post("/posteapourvoirs").with(csrf()).param("numEmploi", "..")
                .param("localisation", "Test").param("profil", "Archive traversal"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String location = created.getResponse().getRedirectedUrl();
        String id = location.substring(location.lastIndexOf('/') + 1);
        byte[] bytes = "archive-content".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (String name : new String[]{"../../outside.txt", "nested/report.txt", "C:\\temp\\report.txt"}) {
            mockMvc.perform(multipart("/posteapourvoirs/" + id + "/addFile").file(
                    new org.springframework.mock.web.MockMultipartFile("file", name, "text/plain", bytes))
                    .with(csrf())).andExpect(status().is3xxRedirection());
        }
        var parent = java.nio.file.Files.createTempDirectory("archive-security-");
        try {
            var archive = parent.resolve("archive");
            archiveService.archive(archive.toString());
            assertFalse(java.nio.file.Files.exists(parent.resolve("Fichiers_Internes")));
            assertFalse(java.nio.file.Files.exists(parent.resolve("outside.txt")));
            var files = archive.resolve("_/Fichiers_Internes");
            assertTrue(java.nio.file.Files.isDirectory(files));
            try (var stream = java.nio.file.Files.list(files)) {
                var payloads = stream.filter(path -> !path.getFileName().toString().equals("metadata.csv")).toList();
                assertEquals(3, payloads.size());
                for (var path : payloads) assertArrayEquals(bytes, java.nio.file.Files.readAllBytes(path));
            }
        } finally {
            org.apache.commons.io.FileUtils.deleteDirectory(parent.toFile());
        }
    }
    @Test
    public void bcryptBoundaryDoesNotAllowSuffixBypass() throws Exception {
        String key = signup();
        String boundary = "é".repeat(36); // 72 UTF-8 bytes
        mockMvc.perform(init(key, boundary)).andExpect(view().name("login"));
        mockMvc.perform(post("/login").with(anonymous()).with(csrf()).param("username", EMAIL)
                .param("password", boundary + "suffix")).andExpect(redirectedUrl("/login?error"));
        mockMvc.perform(post("/login").with(anonymous()).with(csrf()).param("username", EMAIL)
                .param("password", boundary)).andExpect(redirectedUrl("/"));
    }
    @Test
    public void signupAcceptsLiteralDollarsAndBackslashesInNames() throws Exception {
        String key = signup("Prénom$1", "Nom\\Test$");
        assertNotNull(key);
        assertEquals("Prénom$1", findUser().getPrenom());
        assertEquals("Nom\\Test$", findUser().getNom());
    }

    @Test
    public void errorPageDoesNotExposeExceptionDetailsOrRenderInjectedHtml() throws Exception {
        var exception = new IllegalStateException("<script>alert('secret')</script> database-password");
        var response = mockMvc.perform(get("/uncaughtException")
                .requestAttr("jakarta.servlet.error.exception", exception))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("uncaughtException"))
                .andExpect(model().attributeDoesNotExist("exception", "exception_message", "exception_stacktrace"))
                .andReturn().getResponse().getContentAsString();
        assertFalse(response.contains("alert('secret')"));
        assertFalse(response.contains("database-password"));
        assertFalse(response.contains("IllegalStateException"));
        mockMvc.perform(get("/uncaughtException"))
                .andExpect(status().isInternalServerError());
    }
}
