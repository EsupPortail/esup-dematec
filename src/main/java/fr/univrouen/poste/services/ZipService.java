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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import static fr.univrouen.poste.utils.SafeFileNames.component;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.ServletOutputStream;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteAPourvoirFile;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;

@Service
public class ZipService {
	
	static final int BUFFER = 2048; 
	
	final Logger logger = LoggerFactory.getLogger(getClass());


    public void writeZip(List<PosteCandidature> candidatures, OutputStream destination) throws IOException, SQLException {
        try (ZipOutputStream out = new ZipOutputStream(destination)) {
            for (PosteCandidature candidature : candidatures) {
                String folder = component(candidature.getPoste().getNumEmploi()) + "/"
                        + component(candidature.getCandidat().getNom()) + "-"
                        + component(candidature.getCandidat().getPrenom()) + "-"
                        + component(candidature.getCandidat().getNumCandidat()) + "/";
                for (PosteCandidatureFile file : candidature.getCandidatureFiles()) {
                    out.putNextEntry(new ZipEntry(folder + file.getId() + "-" + component(file.getFilename())));
                    try (InputStream input = file.getBigFile().getBinaryFile().getBinaryStream()) {
                        input.transferTo(out);
                    }
                    out.closeEntry();
                }
            }
        }
    }

    public void writeZip(PosteAPourvoir poste, ServletOutputStream destination) throws IOException, SQLException {
        try (ZipOutputStream out = new ZipOutputStream(destination)) {
            for (PosteAPourvoirFile file : poste.getPosteFiles()) {
                // The id prevents collisions between names that sanitize to the same component.
                out.putNextEntry(new ZipEntry(file.getId() + "-" + component(file.getFilename())));
                try (InputStream input = file.getBigFile().getBinaryFile().getBinaryStream()) {
                    input.transferTo(out);
                }
                out.closeEntry();
            }
        }
    }
}
