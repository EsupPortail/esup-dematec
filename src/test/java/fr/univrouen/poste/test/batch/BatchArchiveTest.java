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

import fr.univrouen.poste.services.ArchiveService;
import fr.univrouen.poste.test.AbstractBatchTest;
import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/**
 * Test d'intégration pour le batch <b>archive</b>.
 *
 * Vérifie que l'exécution du batch {@code archive} (équivalent de
 * {@code mvn exec:java -Dexec.args="archive /opt/archive-demat"}) se
 * déroule sans erreur et produit le répertoire d'archive avec les fichiers
 * attendus (au minimum le CSV global des candidatures et le fichier de
 * statistiques).
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une
 * exécution séquentielle.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BatchArchiveTest extends AbstractBatchTest {

    @Autowired
    private ArchiveService archiveService;

    /**
     * Test 01 : exécution du batch archive
     *
     * Un répertoire temporaire est créé pour recevoir l'archive.  On vérifie
     * que le répertoire de destination a bien été créé par le service et qu'il
     * contient au moins le fichier CSV global {@code candidatures.csv} ainsi que
     * le fichier de statistiques {@code stat.txt}.  Le répertoire temporaire est
     * supprimé en fin de test.
     */
    @Test
    public void test01_ArchiveBatch() throws Exception {
        // Créer un répertoire temporaire parent
        Path tempParent = Files.createTempDirectory("esup-dematec-archive-test-");
        // Le batch crée lui-même le sous-répertoire via mkdir() ;
        // on lui passe donc un chemin qui n'existe pas encore.
        String archiveDir = tempParent.resolve("archive").toString();

        try {
            archiveService.archive(archiveDir);

            File archiveDirFile = new File(archiveDir);
            assertTrue("Le répertoire d'archive doit être créé par le batch",
                    archiveDirFile.exists() && archiveDirFile.isDirectory());

            File candidaturesCsv = new File(archiveDir, "candidatures.csv");
            assertTrue("Le fichier candidatures.csv doit être présent dans l'archive",
                    candidaturesCsv.exists());

            File statTxt = new File(archiveDir, "stat.txt");
            assertTrue("Le fichier stat.txt doit être présent dans l'archive",
                    statTxt.exists());

            System.out.println("✓ Batch archive exécuté avec succès dans : " + archiveDir);
            System.out.println("  - candidatures.csv : " + candidaturesCsv.length() + " octets");
            System.out.println("  - stat.txt         : " + statTxt.length() + " octets");
        } finally {
            // Nettoyage du répertoire temporaire
            FileUtils.deleteDirectory(tempParent.toFile());
        }
    }
}

