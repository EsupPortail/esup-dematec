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

import fr.univrouen.poste.batch.GalaxieImportService;
import fr.univrouen.poste.dao.GalaxieExcelDao;
import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.test.AbstractBatchTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test d'intégration pour le batch <b>importgalaxie</b>.
 *
 * Vérifie que l'exécution du batch {@code importgalaxie} (équivalent de
 * {@code mvn exec:java -Dexec.args="importgalaxie /opt/galaxie/EXTETBF.xls"})
 * se déroule sans erreur et produit bien les entités attendues en base :
 * <ul>
 *   <li>Un {@code GalaxieExcel} correspondant au fichier importé</li>
 *   <li>Des {@code PosteAPourvoir} générés à partir des entrées Galaxie</li>
 *   <li>Des {@code User} (candidats) générés à partir des entrées Galaxie</li>
 * </ul>
 *
 * Ce test s'exécute après {@link BatchDeleteDataTest} afin de travailler sur
 * une base nettoyée.
 *
 * ⚠️ Les tests sont ordonnés par nom (NAME_ASCENDING) pour garantir une
 * exécution séquentielle.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BatchImportGalaxieTest extends AbstractBatchTest {

    @Autowired
    private GalaxieImportService galaxieImportService;

    @Autowired
    private GalaxieExcelDao galaxieExcelDao;

    @Autowired
    private PosteAPourvoirDao posteAPourvoirDao;

    @Autowired
    private UserDao userDao;

    /**
     * Test 01 : exécution du batch importgalaxie
     *
     * Importe le fichier Galaxie de démonstration {@code EXTETBF.xls} puis
     * déclenche la génération des candidats et des postes.  On vérifie ensuite
     * que la base contient bien un {@code GalaxieExcel}, des postes et des
     * utilisateurs candidats.
     */
    @Test
    public void test01_ImportGalaxieBatch() throws Exception {
        // Localiser le fichier Galaxie d'exemple fourni avec l'application
        File galaxieFile = new File("src/main/webapp/doc/EXTETBF.xls");
        assertTrue("Le fichier Galaxie d'exemple doit exister pour exécuter le batch",
                galaxieFile.exists());

        String galaxieFilePath = galaxieFile.getAbsolutePath();

        // -- Étape 1 : import du fichier (équivalent de BatchMain "importgalaxie") --
        galaxieImportService.importGalaxie(galaxieFilePath);
        System.out.println("✓ importGalaxie exécuté pour : " + galaxieFilePath);

        // Vérifier qu'un GalaxieExcel a été créé
        List<GalaxieExcel> galaxieExcels = galaxieExcelDao.findAllGalaxieExcels("creation", "desc");
        assertNotNull("La liste des GalaxieExcel ne doit pas être null", galaxieExcels);
        assertFalse("Au moins un GalaxieExcel doit être créé après l'import",
                galaxieExcels.isEmpty());

        GalaxieExcel lastExcel = galaxieExcels.get(0);
        assertEquals("Le nom du fichier importé doit correspondre",
                galaxieFile.getName(), lastExcel.getFilename());
        System.out.println("  - GalaxieExcel créé : " + lastExcel.getFilename());

        // -- Étape 2 : génération des candidats et des postes --
        galaxieImportService.generateCandidatsPostes();
        System.out.println("✓ generateCandidatsPostes exécuté");

        // Vérifier que des postes ont été créés
        List<PosteAPourvoir> postes = posteAPourvoirDao.findAllPosteAPourvoirs();
        assertNotNull("La liste des postes ne doit pas être null", postes);
        assertFalse("Des postes doivent être créés après la génération",
                postes.isEmpty());
        System.out.println("  - Postes créés : " + postes.size());

        // Vérifier que des candidats ont été créés
        List<User> users = userDao.findAllUsers();
        assertNotNull("La liste des utilisateurs ne doit pas être null", users);
        boolean hasCandidats = users.stream()
                .anyMatch(u -> u.getNumCandidat() != null && !u.getNumCandidat().isEmpty());
        assertTrue("Des candidats doivent être créés après la génération", hasCandidats);
        System.out.println("  - Utilisateurs créés (total) : " + users.size());

        System.out.println("✓ Batch importgalaxie exécuté avec succès");
    }
}

