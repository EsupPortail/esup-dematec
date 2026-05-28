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
package fr.univrouen.poste.test.batch;

import fr.univrouen.poste.batch.ResendActivationMailService;
import fr.univrouen.poste.test.AbstractBatchTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test d'intégration pour le batch <b>resendactivation</b>.
 *
 * Vérifie que l'exécution du batch {@code resendactivation} (équivalent
 * de {@code mvn exec:java -Dexec.args="resendactivation"}) se déroule sans
 * erreur.  Le batch envoie un mail d'activation à chaque candidat dont le
 * compte n'a pas encore été activé.  Dans l'environnement de test l'envoi
 * peut échouer silencieusement ; ce test vérifie uniquement que la méthode
 * s'exécute sans lever d'exception.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une
 * exécution séquentielle.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BatchResendActivationTest extends AbstractBatchTest {

    @Autowired
    private ResendActivationMailService resendActivationMailService;

    /**
     * Test 01 : exécution du batch resendactivation
     *
     * Le service parcourt tous les candidats non activés et tente de leur
     * renvoyer un mail d'activation.  On vérifie que l'exécution se termine
     * sans exception (les éventuels échecs d'envoi SMTP sont logués mais ne
     * propagent pas d'exception).
     */
    @Test
    public void test01_ResendActivationBatch() {
        resendActivationMailService.resendActivationMails();
        System.out.println("✓ Batch resendactivation exécuté avec succès");
    }
}

