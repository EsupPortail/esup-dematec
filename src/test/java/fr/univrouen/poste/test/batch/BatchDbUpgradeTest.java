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

import fr.univrouen.poste.batch.DbToolService;
import fr.univrouen.poste.dao.AppliVersionDao;
import fr.univrouen.poste.domain.AppliVersion;
import fr.univrouen.poste.test.AbstractBatchTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Test d'intégration pour le batch <b>dbupgrade</b>.
 *
 * Vérifie que l'exécution du batch {@code dbupgrade} (équivalent de
 * {@code mvn exec:java -Dexec.args="dbupgrade"}) se déroule sans erreur.
 * Le batch met à jour le schéma de base de données si nécessaire et
 * enregistre la version courante dans la table {@code appli_version}.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une
 * exécution séquentielle.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BatchDbUpgradeTest extends AbstractBatchTest {

    @Autowired
    private DbToolService dbToolService;

    @Autowired
    private AppliVersionDao appliVersionDao;

    /**
     * Test 01 : exécution du batch dbupgrade
     *
     * La base de données de test est déjà à la version courante ; le batch
     * doit donc se terminer sans lever d'exception et sans modifier le schéma.
     * On vérifie en outre que la table {@code appli_version} contient bien un
     * enregistrement après l'exécution.
     */
    @Test
    public void test01_DbUpgradeBatch() {
        dbToolService.upgrade();

        List<AppliVersion> versions = appliVersionDao.findAllAppliVersions();
        assertNotNull("La liste des AppliVersion ne doit pas être null", versions);
        assertFalse("Au moins une AppliVersion doit exister après dbupgrade", versions.isEmpty());

        AppliVersion version = versions.get(0);
        assertNotNull("La version de l'application ne doit pas être null", version.getEsupDematEcVersion());
        System.out.println("✓ Batch dbupgrade exécuté avec succès – version en base : "
                + version.getEsupDematEcVersion());
    }
}

