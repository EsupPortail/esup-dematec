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
package fr.univrouen.poste.test;

import fr.univrouen.poste.test.security.SecurityAccessTest;
import fr.univrouen.poste.test.web.LoginControllerTest;
import fr.univrouen.poste.test.web.admin.*;
import fr.univrouen.poste.test.web.candidat.MyPosteCandidatureControllerCandidatDateKoTest;
import fr.univrouen.poste.test.web.candidat.MyPosteCandidatureControllerCandidatTest;
import fr.univrouen.poste.test.web.candidat.MyPosteCandidatureControllerMembreTest;
import fr.univrouen.poste.test.web.candidat.MyPosteCandidatureControllerTest;
import fr.univrouen.poste.test.web.membre.PosteAPourvoirControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Suite de tests d'intégration ordonnée
 *
 * Les tests sont exécutés dans un ordre précis pour construire progressivement
 * les données en base de données PostgreSQL via les contrôleurs :
 *
 * 1. InitialSetupControllerTest : Crée les utilisateurs de base (super-manager@example.org)
 * 2. GalaxieImportControllerTest : Import Galaxie et création candidats/postes/candidatures
 * 3. CommissionImportControllerTest : Import Commission et création membres
 * 4. Puis tests fonctionnels utilisant ces données
 *
 * ⚠️ Important : Les tests utilisent UNIQUEMENT les contrôleurs (MockMvc)
 * pour créer et manipuler les données. Aucun appel direct à persist() ou merge().
 */
@RunWith(Suite.class)
@SuiteClasses({
    InitialSetupControllerTest.class,
    GalaxieImportControllerTest.class,
    CommissionImportControllerTest.class,
    SecurityAccessTest.class,
    LoginControllerTest.class,
    UserControllerTest.class,
    PosteAPourvoirControllerTest.class,
    MyPosteCandidatureControllerTest.class,
    MyPosteCandidatureControllerCandidatTest.class,
    MyPosteCandidatureControllerMembreTest.class,
    AdminControllerTest.class,
    ChangeDateKoSetupControllerTest.class,
    MyPosteCandidatureControllerCandidatDateKoTest.class,
})
public class IntegrationTestSuite {
    // Cette classe reste vide, elle sert uniquement à définir l'ordre d'exécution
}

