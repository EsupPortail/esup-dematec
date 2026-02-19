// java
package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.domain.*;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;

import java.io.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class ArchiveService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    CsvService csvService;

    @Resource
    StatService statService;

    @Resource
    PosteAPourvoirDao posteAPourvoirDao;

    @Resource
    PosteCandidatureDao posteCandidatureDao;

    private static final DateTimeFormatter SDF = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    @Transactional(readOnly = true)
    public void archive(String destFolder) throws IOException, SQLException {

        List<PosteCandidature> posteCandidatures = posteCandidatureDao.findAllPosteCandidatures();

        File destFolderFile = new File(destFolder);
        if (destFolderFile.mkdir()) {

            try (Writer csvGlobalWriter = new FileWriter(destFolder.concat("/candidatures.csv"))) {
                csvService.csvWrite(csvGlobalWriter, posteCandidatures);
            }

            try (Writer statWriter = new FileWriter(destFolder.concat("/stat.txt"))) {
                StatBean stat = statService.stats();
                statWriter.write(stat.toText());
            }

            final String[] header = new String[]{"id", "filename", "sendDate", "owner"};

            for (PosteCandidature posteCandidature : posteCandidatures) {
                String folderName = destFolder.concat("/");
                String numEmploi = posteCandidature.getPoste().getNumEmploi();
                numEmploi = numEmploi.replaceAll("[^a-zA-Z0-9.-]", "_");
                folderName = folderName.concat(numEmploi).concat("/");

                File folder = new File(folderName);
                folder.mkdir();

                folderName = folderName.concat(posteCandidature.getRecevableEnum().name()).concat("/");
                folder = new File(folderName);
                folder.mkdir();

                if (posteCandidature.getAuditionnable() != null) {
                    folderName = folderName.concat(posteCandidature.getAuditionnable() ? "Auditionnable" : "Non_Auditionnable").concat("/");
                    folder = new File(folderName);
                    folder.mkdir();
                }

                String nom = posteCandidature.getCandidat().getNom().replaceAll("[^a-zA-Z0-9.-]", "_");
                String prenom = posteCandidature.getCandidat().getPrenom().replaceAll("[^a-zA-Z0-9.-]", "_");
                String numCandidat = posteCandidature.getCandidat().getNumCandidat().replaceAll("[^a-zA-Z0-9.-]", "_");
                folderName = folderName.concat(nom).concat("-");
                folderName = folderName.concat(prenom).concat("-");
                folderName = folderName.concat(numCandidat).concat("/");

                folder = new File(folderName);
                folder.mkdir();

                // écrire metadata.csv pour les candidatures
                writeMetadataCsvForFiles(posteCandidature.getCandidatureFiles(), folderName, header);

                if (!posteCandidature.getMemberReviewFiles().isEmpty()) {
                    folderName = folderName.concat("Rapports_commission").concat("/");
                    folder = new File(folderName);
                    folder.mkdir();

                    writeMetadataCsvForMemberFiles(posteCandidature.getMemberReviewFiles(), folderName, header);
                }
            }

            for (PosteAPourvoir poste : posteAPourvoirDao.findAllPosteAPourvoirs()) {

                String folderName = destFolder.concat("/");
                String numEmploi = poste.getNumEmploi();
                numEmploi = numEmploi.replaceAll("[^a-zA-Z0-9.-]", "_");
                folderName = folderName.concat(numEmploi).concat("/");

                File folder = new File(folderName);
                folder.mkdir();

                folderName = folderName.concat("Fichiers_Internes").concat("/");
                folder = new File(folderName);
                folder.mkdir();

                // écrire metadata.csv pour les fichiers internes du poste
                writeMetadataCsvForPosteFiles(poste.getPosteFiles(), folderName, header);
            }

        } else {
            logger.error("Le répertoire " + destFolder + " n'a pas pu être créé. Vérifiez qu'il n'existe pas déjà, que l'application a bien les droits de le créer, etc.");
        }
    }

    private void writeMetadataCsvForFiles(Set<PosteCandidatureFile> files, String folderName, String[] header) throws IOException, SQLException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        for (String col : header) schemaBuilder.addColumn(col);
        CsvSchema schema = schemaBuilder.build().withHeader();
        ObjectWriter objWriter = mapper.writer(schema);

        try (Writer metadataWriter = new FileWriter(folderName.concat("metadata.csv"));
             SequenceWriter seqWriter = objWriter.writeValues(metadataWriter)) {

            for (PosteCandidatureFile f : files) {
                String fileName = f.getId().toString().concat("-").concat(f.getFilename());
                String folderFileName = folderName.concat(fileName);
                File file = new File(folderFileName);
                file.createNewFile();

                try (OutputStream outputStream = new FileOutputStream(file);
                    InputStream inputStream = f.getBigFile().getBinaryFile().getBinaryStream()) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", fileName);
                row.put("filename", f.getFilename());
                row.put("sendDate", f.getSendTime() != null ? f.getSendTime().format(SDF) : "");
                row.put("owner", posteCandidatOwnerEmailSafe(f));
                seqWriter.write(row);
            }
        }
    }

    private void writeMetadataCsvForMemberFiles(Set<MemberReviewFile> files, String folderName, String[] header) throws IOException, SQLException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        for (String col : header) schemaBuilder.addColumn(col);
        CsvSchema schema = schemaBuilder.build().withHeader();
        ObjectWriter objWriter = mapper.writer(schema);

        try (Writer metadataWriter = new FileWriter(folderName.concat("metadata.csv"));
             SequenceWriter seqWriter = objWriter.writeValues(metadataWriter)) {

            for (MemberReviewFile f : files) {
                String fileName = f.getId().toString().concat("-").concat(f.getFilename());
                String folderFileName = folderName.concat(fileName);
                File file = new File(folderFileName);
                file.createNewFile();

                try (OutputStream outputStream = new FileOutputStream(file);
                     InputStream inputStream = f.getBigFile().getBinaryFile().getBinaryStream()) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", fileName);
                row.put("filename", f.getFilename());
                row.put("sendDate", f.getSendTime() != null ? f.getSendTime().format(SDF) : "");
                row.put("owner", memberFileOwnerEmailSafe(f));
                seqWriter.write(row);
            }
        }
    }

    private void writeMetadataCsvForPosteFiles(Set<PosteAPourvoirFile> files, String folderName, String[] header) throws IOException, SQLException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        for (String col : header) schemaBuilder.addColumn(col);
        CsvSchema schema = schemaBuilder.build().withHeader();
        ObjectWriter objWriter = mapper.writer(schema);

        try (Writer metadataWriter = new FileWriter(folderName.concat("metadata.csv"));
             SequenceWriter seqWriter = objWriter.writeValues(metadataWriter)) {

            for (PosteAPourvoirFile f : files) {
                String fileName = f.getId().toString().concat("-").concat(f.getFilename());
                String folderFileName = folderName.concat(fileName);
                File file = new File(folderFileName);
                file.createNewFile();

                try (OutputStream outputStream = new FileOutputStream(file);
                     InputStream inputStream = f.getBigFile().getBinaryFile().getBinaryStream()) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", fileName);
                row.put("filename", f.getFilename());
                row.put("sendDate", f.getSendTime() != null ? f.getSendTime().format(SDF) : "");
                row.put("owner", posteFileOwnerEmailSafe(f));
                seqWriter.write(row);
            }
        }
    }

    private String posteCandidatOwnerEmailSafe(PosteCandidatureFile f) {
        try {
            return f.getPosteCandidature().getCandidat().getEmailAddress();
        } catch (Exception e) {
            return "";
        }
    }

    private String memberFileOwnerEmailSafe(MemberReviewFile f) {
        try {
            return f.getMember().getEmailAddress();
        } catch (Exception e) {
            return "";
        }
    }

    private String posteFileOwnerEmailSafe(PosteAPourvoirFile f) {
        try {
            return f.getSender().getEmailAddress();
        } catch (Exception e) {
            return "";
        }
    }

    public class ArchiveMetadataFileBean {

        String id;
        String filename;
        LocalDateTime sendDate;
        String owner;

        public ArchiveMetadataFileBean(String id, String filename,
                                       LocalDateTime sendDate, String owner) {
            super();
            this.id = id;
            this.filename = filename;
            this.sendDate = sendDate;
            this.owner = owner;
        }

        public String getId() {
            return id;
        }

        public String getFilename() {
            return filename;
        }

        public LocalDateTime getSendDate() {
            return sendDate;
        }

        public String getOwner() {
            return owner;
        }

    }
}