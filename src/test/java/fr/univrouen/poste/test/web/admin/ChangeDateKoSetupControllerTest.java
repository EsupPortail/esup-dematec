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
package fr.univrouen.poste.test.web.admin;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.test.AbstractControllerTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'initialisation - Première classe de test exécutée
 *
 * Ce test s'exécute en tout premier dans la suite d'intégration et crée
 * les utilisateurs de base nécessaires pour tous les autres tests :
 * - super-manager@example.org : Super manager avec tous les droits
 *
 * Note: L'utilisateur admin existe déjà (créé automatiquement au démarrage de l'application)
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une exécution séquentielle
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChangeDateKoSetupControllerTest extends AbstractControllerTest {

    @Test
    @WithUserDetails("super-manager@example.org")
    public void test01_SuperManagerConfigDatesKo() throws Exception {

        MvcResult result = mockMvc.perform(get("/admin/appliconfig"))
                .andExpect(status().isOk()).andReturn();

        List<AppliConfig> appliconfigs = (List<AppliConfig>) result.getModelAndView().getModel().get("appliconfigs");
        assertTrue("Il doit y avoir 1 configuration appliconfig", appliconfigs.size()==1);

        AppliConfig config = appliconfigs.get(0);
        Date pastDate = new Date(System.currentTimeMillis() - 24 * 3600 * 1000); // -1 jour
        config.setDateEndCandidat(pastDate);
        config.setDateEndMembre(pastDate);
        config.setDateEndCandidatActif(pastDate);

        System.out.println("✓");
    }


}

