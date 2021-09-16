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
import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.domain.AppliVersion;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.utils.TxPdfService;

@Service
public class DbToolService {

	private final Logger logger = Logger.getLogger(getClass());
	
	final static String currentEsupDematEcVersion = "1.8.x";
	
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
						"INSERT INTO manager_review (id, review_status, version) SELECT manager_review,'Non_vue',0 FROM manager_review_id;";
				logger.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
	    		logger.warn("\n\n#####\n\t" +
	    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM - menu 'Configuration' !" +
	    				"\n#####\n");
	    		
	    		esupDematEcVersion = "1.1.x";
			} 
			
			if("1.1.x".equals(esupDematEcVersion)) {
				
				List<PosteCandidatureFile> pcFiles = PosteCandidatureFile.findAllPosteCandidatureFiles();
				for(PosteCandidatureFile pcFile: pcFiles) {
					txPdfService.updateNbPages(pcFile.getId());
				}
				
	    		logger.warn("\n\n#####\n\t" +
	    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM - menu 'Configuration' !" +
	    				"\n#####\n");
	    		
	    		esupDematEcVersion = "1.2.x";
	    		
			} 
			
			
			if("1.2.x".equals(esupDematEcVersion)) {
				
				// oublie de mettre en 1.3.x la version 1.3.0 et 1.3.1 - on est donc peut-être déjà en 1.3.0 ici 
				// -> on regarde si AppliConfigFileType est bien vide ... si non vide, on est déjà en 1.3.x
				if(AppliConfigFileType.findAllAppliConfigFileTypes().size()==0) {
					
					String sqlUpdate = "insert into appli_config_file_type (id, type_title, type_description, " +
							"candidature_file_mo_size_max, candidature_nb_file_max, candidature_content_type_restriction_regexp, candidature_filename_restriction_regexp, version) " +
							"values (1, 'Fichier de candidature', 'Merci de sélectionner un fichier puis de valider votre sélection pour l''envoi effectif.', -1, -1, '.*', '.*', 1); " +
							"insert into galaxie_mapping(id, id_email, id_num_candidat, id_numemploi, id_civilite, id_localisation, id_nom, id_prenom, id_profil, version) " +
							"values (1, 'Email', 'N° candidat', 'N° emploi', 'Civilité', 'Localisation', 'Nom', 'Prénom', 'Profil J.o', 0);";
	
					logger.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
					Connection connection = dataSource.getConnection();
					CallableStatement statement = connection.prepareCall(sqlUpdate);
					statement.execute();
					connection.close();
					
					List<PosteCandidatureFile> pcFiles = PosteCandidatureFile.findAllPosteCandidatureFiles();
					for(PosteCandidatureFile pcFile: pcFiles) {
						pcFile.setFileType(AppliConfigFileType.getDefaultFileType());
					}
					
		    		logger.warn("\n\n#####\n\t" +
		    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM - menus 'Mapping Galaxie', 'Configuration' et 'Types de fichiers' !" +
		    				"\n#####\n");
				}
	    		
	    		esupDematEcVersion = "1.3.x";
	    		
			} 
			
			if("1.3.x".equals(esupDematEcVersion)) { 
				
	    		logger.warn("\n\n#####\n\t" +
	    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM - menu 'Configuration' !" +
	    				"\n#####\n");
	    		
	    		esupDematEcVersion = "1.4.x";
			} 
			
			if("1.4.x".equals(esupDematEcVersion)) { 
				
				// Hack : en 1.5.0 et 1.5.1 l'initialisation de la base mettait 1.4.x comme numéro de version au lieu de 1.5.x 
				
				String sqlTestDematEcVersion = "SELECT column_name FROM information_schema.columns WHERE table_name='poste_candidature' and column_name='textsearchable_index_col';";
				Connection connectionTest = dataSource.getConnection();
				CallableStatement statementTest = connectionTest.prepareCall(sqlTestDematEcVersion);
				statementTest.execute();
				Boolean realVersionIs14x = !statementTest.getResultSet().next();
				connectionTest.close();
				
				if(realVersionIs14x) {
					
					logger.info("\n\nMaj depuis 1.4.x ...\n\n");
					
					String sqlUpdate = "update appli_config_file_type set list_index = 0;";
					
					sqlUpdate += "ALTER TABLE poste_candidature ADD COLUMN textsearchable_index_col tsvector;"
							+ " UPDATE poste_candidature SET textsearchable_index_col = "
							+ " setweight(to_tsvector('simple', replace(coalesce(c_user.nom,''),'-',' ')), 'A') "
							+ " || setweight(to_tsvector('simple', replace(coalesce(c_user.prenom,''),'-',' ')), 'B') "
					    + "  || setweight(to_tsvector('simple', coalesce(c_user.email_address,'')), 'B') "
					     + " || setweight(to_tsvector('simple', coalesce(c_user.num_candidat,'')), 'B') "
					     + " FROM c_user where poste_candidature.candidat=c_user.id;"
					+ " CREATE INDEX textsearch_idx ON poste_candidature USING gin(textsearchable_index_col);"
	
	
					+ " CREATE FUNCTION textsearchable_poste_candidature_trigger() RETURNS trigger AS $$"
					+ " begin"
					  + " new.textsearchable_index_col :="
					    + " setweight(to_tsvector('simple', replace(coalesce(c_user.nom,''),'-',' ')), 'A') "
					    + " || setweight(to_tsvector('simple', replace(coalesce(c_user.prenom,''),'-',' ')), 'B') "
					    + "  || setweight(to_tsvector('simple', coalesce(c_user.email_address,'')), 'B') "
					    + "  || setweight(to_tsvector('simple', coalesce(c_user.num_candidat,'')), 'B') "
					   + "   FROM c_user where new.candidat=c_user.id;"
					  + " return new;"
					+ " end"
					+ " $$ LANGUAGE plpgsql;"
	
					+ " CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE"
					+ "    ON poste_candidature FOR EACH ROW EXECUTE PROCEDURE textsearchable_poste_candidature_trigger();";
					
					
					logger.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
					
					Connection connection = dataSource.getConnection();
					CallableStatement statement = connection.prepareCall(sqlUpdate);
					statement.execute();
					connection.close();
					
		    		logger.warn("\n\n#####\n\t" +
		    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM - menu 'Configuration' !" +
		    				"\n#####\n");
				} else {
					logger.info("\n\nVersion précédente en 1.5.0 ou 1.5.1 ; seul le numéro de version en base était en 1.4.x\n\n");
					logger.warn("\n\n#####\n\t" +
		    				"Base de données à jour en 1.5.x" +
		    				"\n#####\n");
				}
	    		
			} 
			
			if("1.5.x".equals(esupDematEcVersion)) { 
			
				logger.info("\n\nMaj depuis 1.5.x ...\n\n");
				
				String sqlUpdate = "update template_file set template_file_type = 'CANDIDATURE';";
				
				sqlUpdate += "update poste_candidature set recevable_enum = 'RECEVABLE' where recevable is true;";
				sqlUpdate += "update poste_candidature set recevable_enum = 'NON_RECEVABLE' where recevable is false;";
				sqlUpdate += "update poste_candidature set recevable_enum = 'NON_DEFINI' where recevable is null;";
				sqlUpdate += "alter table poste_candidature drop column recevable;";
				
				sqlUpdate += "update appli_config set candidature_recevable_enum_default = 'RECEVABLE' where candidature_recevable_default is true;";
				sqlUpdate += "update appli_config set candidature_recevable_enum_default = 'NON_RECEVABLE' where candidature_recevable_default is true;";
				sqlUpdate += "update appli_config set candidature_recevable_enum_default = 'NON_DEFINI' where candidature_recevable_default is null;";
				sqlUpdate += "alter table appli_config drop column candidature_recevable_default;";

				logger.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();	
				
			} 
					
			if("1.6.x".equals(esupDematEcVersion)) { 
				
				logger.info("\n\nMaj depuis 1.6.x ...\n\n");
				
				String sqlUpdate = "alter table galaxie_entry alter column profil type varchar(300);";		
				sqlUpdate += "alter table galaxie_entry alter column localisation type varchar(300);";

				logger.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();	
				
			} 
			if("1.7.x".equals(esupDematEcVersion)) { 
				
				logger.info("\n\nMaj depuis 1.7.x ...\n\n");
				
				String sqlUpdate = "alter table posteapourvoir alter column profil type varchar(300);";
				sqlUpdate += "alter table posteapourvoir alter column localisation type varchar(300);";
				sqlUpdate += "alter table posteapourvoir add constraint posteapourvoir_num_emploi_unique unique (num_emploi);";
				sqlUpdate += "alter table posteapourvoir alter column num_emploi set not null;";
				
				logger.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();	
				
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
				"delete from log_poste_file;" +
				"delete from poste_candidature_reporters;" +
				"delete from poste_candidature_member_review_files;" +
				"delete from member_review_file;" +
				"delete from poste_candidature_candidature_files;" +
				"delete from poste_candidature_file;" +
				"delete from poste_candidature_tags;" +
				"delete from poste_candidature;" +
				"delete from manager_review;" +
				"delete from posteapourvoir_membres;" +
				"delete from posteapourvoir_presidents;" +		
				"delete from posteapourvoir_poste_files;" +
				"delete from posteapourvoir_file;" +
				"delete from posteapourvoir;" +
				"delete from c_user where is_admin=false and is_manager=false and is_super_manager=false;" +
				"-- ne pas faire de truncate sur big_file pour appel du trigger et suppression effective du blob\n" +
				"delete from big_file where id not in (select big_file from template_file);" +
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
