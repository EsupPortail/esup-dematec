// java
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
package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.PosteCandidatureTagDao;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureTag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class CsvService {

    final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    PosteCandidatureTagDao posteCandidatureTagDao;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy - HH:mm");

    @Transactional(readOnly = true)
    public void csvWrite(Writer writer, List<PosteCandidature> posteCandidatures) throws IOException {
        log.info("Generate CSV for " + posteCandidatures.size() + " posteCandidatures");

        // construire en-têtes
        List<String> header = new ArrayList<>(Arrays.asList(
                "poste", "nom", "email", "prenom", "galaxie", "recevable", "auditionnable",
                "vue", "creation", "modification", "gestionnaire", "dateGestion", "civilite"));

        List<PosteCandidatureTag> allTags = posteCandidatureTagDao.findAllPosteCandidatureTags();
        for (PosteCandidatureTag tag : allTags) {
            header.add(tag.getName());
        }

        // construire le schema CSV avec header (ordonné)
        CsvMapper mapper = new CsvMapper();
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        for (String col : header) {
            schemaBuilder.addColumn(col);
        }
        CsvSchema schema = schemaBuilder.build().withHeader();

        ObjectWriter objWriter = mapper.writer(schema);
        SequenceWriter seqWriter = objWriter.writeValues(writer);

        try {
            for (PosteCandidature p : posteCandidatures) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("poste", safe(() -> p.getPoste() != null ? p.getPoste().getNumEmploi() : null));
                row.put("nom", safe(() -> p.getNom()));
                row.put("email", safe(() -> p.getEmail()));
                row.put("prenom", safe(() -> p.getPrenom()));
                row.put("galaxie", safe(() -> p.getNumCandidat()));
                row.put("recevable", safe(() -> p.getRecevableEnum() != null ? p.getRecevableEnum().name() : ""));
                row.put("auditionnable", safe(() -> p.getAuditionnable() ? "true" : "false"));
                row.put("vue", safe(() -> p.getManagerReviewState()));
                row.put("creation", safeDate(() -> p.getCreation()));
                row.put("modification", safeDate(() -> p.getModification()));
                row.put("gestionnaire", safe(() -> p.getManagerReview().getManager().getEmailAddress()));
                row.put("dateGestion", safeDate(() -> {
                    Date d = p.getManagerReview().getReviewDate();
                    return d;
                }));
                row.put("civilite", safe(() -> p.getCandidat() != null ? p.getCandidat().getCivilite() : null));

                // tags, conserver l'ordre des en-têtes
                for (PosteCandidatureTag tag : allTags) {
                    String value = "";
                    try {
                        if (p.getTags() != null && p.getTags().get(tag) != null) {
                            value = safe(() -> p.getTags().get(tag).getValue());
                        }
                    } catch (Exception e) {
                        value = "";
                    }
                    row.put(tag.getName(), value);
                }

                seqWriter.write(row);
            }
        } finally {
            seqWriter.close();
        }

        log.info("Generate CSV OK");
    }

    private String safe(Supplier<String> supplier) {
        try {
            String s = supplier.get();
            return s == null ? "" : s;
        } catch (Exception e) {
            return "";
        }
    }

    private String safeDate(Supplier<Date> supplier) {
        try {
            Date d = supplier.get();
            return d != null ? SDF.format(d) : "";
        } catch (Exception e) {
            return "";
        }
    }
}