package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.BigFile;
import fr.univrouen.poste.repository.BigFileRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.sql.Blob;

@Service
@Transactional
public class BigFileDao {

    @Resource
    BigFileRepository bigFileRepository;

    @PersistenceContext
    EntityManager entityManager;

    public BigFile saveBigFile(BigFile bigFile) {
        return bigFileRepository.save(bigFile);
    }

    public void flush() {
        bigFileRepository.flush();
    }

    // Méthode spéciale pour gérer les streams de gros fichiers avec Hibernate
    // @see http://stackoverflow.com/questions/10042766/jpa-analog-of-lobcreator-from-hibernate
    public void setBinaryFileStream(BigFile bigFile, InputStream inputStream, long length) {
        Session session = (Session) entityManager.getDelegate();
        LobHelper helper = session.getLobHelper();
        Blob blob = helper.createBlob(inputStream, length);
        bigFile.setBinaryFile(blob);
    }
}
