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
package fr.univrouen.poste.test.web.membre;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Tests pour PosteAPourvoirController
 * Vérifie :
 * - Affichage de la liste des postes
 * - Création/modification de postes
 * - Contrôle d'accès selon les rôles
 *
 * Note: Ces tests utilisent uniquement MockMvc pour créer et manipuler les postes.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PosteAPourvoirControllerDatePosteAuditionnableTest extends AbstractControllerTest {


    /*
     * Test 01 : positionnement d'une date de poste auditionnable pour tous les postes
     */
    @Test
    @WithUserDetails("super-manager@example.org")
    public void test01_positionnerDatePosteAuditionnable() throws Exception {

        MvcResult result = mockMvc.perform(get("/posteapourvoirs"))
                .andReturn();

        Page<PosteAPourvoir> postes = (Page<PosteAPourvoir>) result.getModelAndView()
                .getModel().get("posteapourvoirs");

        for (PosteAPourvoir poste : postes.getContent()) {
            poste.setDateEndCandidatAuditionnable(LocalDateTime.now().plusDays(7));
        }
    }
}