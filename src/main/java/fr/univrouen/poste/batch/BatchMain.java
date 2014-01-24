package fr.univrouen.poste.batch;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.univrouen.poste.services.ArchiveService;

public class BatchMain {

	public static void main(String[] args) throws IOException, SQLException  {
		ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext*.xml");
		
		if(args.length < 2) {
			System.err.println("Merci de préciser les arguments. Exemple : \n" +
					"mvn exec:java -Dexec.args=\"archive /opt/archive-demat\"");
			return;
		}

		
		if("archive".equals(args[0])) {
			String destFolder = args[1];
			ArchiveService archiveService = springContext.getBean("archiveService", ArchiveService.class);
			archiveService.archive(destFolder);
		} else {
			System.err.println("Commande non trouvée.");
		}
		
		springContext.close();
	}

}
