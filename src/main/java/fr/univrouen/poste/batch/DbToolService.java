package fr.univrouen.poste.batch;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.AppliVersion;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.utils.TxPdfService;

@Service
public class DbToolService {

	private final Logger logger = Logger.getLogger(getClass());
	
	final static String currentEsupDematEcVersion = "1.2.x";
	
	@Resource
	DataSource dataSource;
	
	@Resource 
	TxPdfService txPdfService;
	
	@Transactional
	public void upgrade() {
		AppliVersion appliVersion = null;
		List<AppliVersion> appliVersions = AppliVersion.findAllAppliVersions();
		if(appliVersions.isEmpty()) {
			appliVersion = new AppliVersion();
			appliVersion.setEsupDematEcVersion("1.1.x");
			appliVersion.persist();
		} else {
			appliVersion = appliVersions.get(0);
		}
		upgradeIfNeeded(appliVersion);
	}

	private void upgradeIfNeeded(AppliVersion appliVersion) {
		String esupDematEcVersion = appliVersion.getEsupDematEcVersion();
		try{
			if("1.0.x".equals(esupDematEcVersion)) {
				String sqlUpdate = "alter table poste_candidature add column manager_review bigint;" +
						"WITH manager_review_id AS (" +
						"UPDATE poste_candidature SET manager_review=nextval('hibernate_sequence') RETURNING manager_review" +
						")" +
						"INSERT INTO manager_review (id, review_status) SELECT manager_review,'Non_vue' FROM manager_review_id;";
				logger.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
	    		logger.warn("\n\n#####\n\t" +
	    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM - menu 'Configuration' !" +
	    				"\n#####\n");
	    		
			} else if("1.1.x".equals(esupDematEcVersion)) {
				
				List<PosteCandidatureFile> pcFiles = PosteCandidatureFile.findAllPosteCandidatureFiles();
				for(PosteCandidatureFile pcFile: pcFiles) {
					txPdfService.updateNbPages(pcFile.getId());
				}
				
	    		logger.warn("\n\n#####\n\t" +
	    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM - menu 'Configuration' !" +
	    				"\n#####\n");
	    		
			} else {
				logger.warn("\n\n#####\n\t" +
	    				"Base de données à jour !" +
	    				"\n#####\n");
			}
			appliVersion.setEsupDematEcVersion(currentEsupDematEcVersion);
			appliVersion.merge();
		} catch(Exception e) {
			throw new RuntimeException("Erreur durant le mise à jour de la base de données", e);
		}
	}
	
	@Transactional
	public void deleteData() {
		try{
		String sqlUpdate = "" +
				"delete from galaxie_entry;" +
				"delete from galaxie_excel;" +
				"delete from commission_entry;" +
				"delete from commission_excel;" +
				"delete from log_auth;" +
				"delete from log_file;" +
				"delete from log_import_commission;" +
				"delete from log_import_galaxie;" +
				"delete from log_mail;" +
				"delete from poste_candidature_member_review_files;" +
				"delete from member_review_file;" +
				"delete from poste_candidature_candidature_files;" +
				"delete from poste_candidature_file;" +
				"delete from poste_candidature;" +
				"delete from manager_review;" +
				"delete from posteapourvoir_membres;" +
				"delete from posteapourvoir;" +
				"delete from c_user where is_admin=false and is_manager=false and is_super_manager=false;" +
				"-- na pas faire de truncate sur big_file pour appel du trigger et suppression effective du blob" +
				"delete from big_file;" +
				"VACUUM FULL;";
		logger.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
		Connection connection = dataSource.getConnection();
		CallableStatement statement = connection.prepareCall(sqlUpdate);
		statement.execute();
		connection.close();
		logger.warn("\n\n#####\n\t" +
				"La base de données ne contient plus que la config et comptes admin/super-manager/manager !" +
				"\n#####\n");
		} catch(Exception e) {
			throw new RuntimeException("Erreur durant la suppressions des données de la base de données", e);
		}
	}

}
