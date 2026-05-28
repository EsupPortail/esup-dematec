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
import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.test.AbstractBatchTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Test d'intégration pour le batch <b>deletedata</b>.
 *
 * Vérifie que l'exécution du batch {@code deletedata} (équivalent de
 * {@code mvn exec:java -Dexec.args="deletedata"}) se déroule sans erreur et
 * supprime effectivement les données de candidature de la base.
 *
 * ⚠️ Ce test est <strong>destructeur</strong> : il efface les données de
 * candidature, de postes et d'utilisateurs candidats/membres.  Il doit être
 * placé en fin de suite, avant le (ré)import Galaxie.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une
 * exécution séquentielle.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BatchDeleteDataTest extends AbstractBatchTest {

    @Autowired
    private DbToolService dbToolService;

    @Autowired
    private PosteCandidatureDao posteCandidatureDao;

    @Autowired
    private PosteAPourvoirDao posteAPourvoirDao;

    /**
     * Test 01 : exécution du batch deletedata
     *
     * Le batch supprime toutes les données de candidature via des requêtes SQL
     * directes (hors transaction Spring).  On vérifie que l'exécution se termine
     * sans exception et que les tables de candidatures et de postes sont vides
     * après l'opération.
     */
    @Test
    public void test01_DeleteDataBatch() {
        dbToolService.deleteData();

        List<?> candidatures = posteCandidatureDao.findAllPosteCandidatures();
        assertTrue("Toutes les candidatures doivent avoir été supprimées par deletedata",
                candidatures.isEmpty());

        List<?> postes = posteAPourvoirDao.findAllPosteAPourvoirs();
        assertTrue("Tous les postes doivent avoir été supprimés par deletedata",
                postes.isEmpty());

        System.out.println("✓ Batch deletedata exécuté avec succès");
        System.out.println("  - PosteCandidatures restantes : " + candidatures.size());
        System.out.println("  - PosteAPourvoirs restants    : " + postes.size());
    }
}

