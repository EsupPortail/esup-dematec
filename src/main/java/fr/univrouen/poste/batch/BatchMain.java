package fr.univrouen.poste.batch;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.univrouen.poste.services.ArchiveService;

public class BatchMain {

	public static void main(String[] args) throws IOException, SQLException  {
		ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext*.xml");
		
		if(args.length < 1 || !"archive".equals(args[0]) && !"dbupgrade".equals(args[0]) && !"deletedata".equals(args[0])) {
			System.err.println("#####\n" +
					"Merci de préciser les arguments.\n" +
					"Voici les possibilités : \n" +
					"\t* mvn exec:java -Dexec.args=\"archive /opt/archive-demat\"\n" +
					"\t* mvn exec:java -Dexec.args=\"dbupgrade\"\n" +
					"\t* mvn exec:java -Dexec.args=\"deletedata\"\n" +
					"-- Attention, cette dernière commande efface les données de candidature de la base !! " +
					"A utiliser pour nettoyer une base pour l'utiliser sur une nouvelle campagne --\n" +
					"#####");
			return;
		}

		
		if("archive".equals(args[0])) {
			String destFolder = args[1];
			ArchiveService archiveService = springContext.getBean("archiveService", ArchiveService.class);
			archiveService.archive(destFolder);
		} else if("dbupgrade".equals(args[0])) {
			DbToolService dbToolService = springContext.getBean("dbToolService", DbToolService.class);
			dbToolService.upgrade();		
		} else if("deletedata".equals(args[0])) {
			DbToolService dbToolService = springContext.getBean("dbToolService", DbToolService.class);
			dbToolService.deleteData();					
		} else {
			System.err.println("Commande non trouvée.");
		}
		
		springContext.close();
	}

}
