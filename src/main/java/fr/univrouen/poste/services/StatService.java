package fr.univrouen.poste.services;

import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;

@Service
public class StatService {
	
	public StatBean stats() {


		Long posteNumber = PosteAPourvoir.countPosteAPourvoirs();
		Long userNumber = User.countUsers();
		Long adminNumber = User.countAdmins();
		Long supermanagerNumber = User.countSupermanagers();
		Long managerNumber = User.countManagers();
		Long membreNumber = User.countMembres();
		Long candidatNumber = User.countCandidats();
		Long userActifNumber = User.countActifCUsers();
		Long candidatActifNumber = User.countActifCandidats();
		Long posteCandidatureNumber = PosteCandidature.countPosteCandidatures();
		Long posteCandidatureActifNumber = PosteCandidature.countPosteActifCandidatures();
		Long posteCandidatureFileNumber = PosteCandidatureFile.countPosteCandidatureFiles();

		long totalFileSize = 0;
		long nbPages = 0;
		for (PosteCandidatureFile posteCandidatureFile : PosteCandidatureFile.findAllPosteCandidatureFiles()) {
			totalFileSize += posteCandidatureFile.getFileSize();
			if(posteCandidatureFile.getNbPages() != null) {
				nbPages += posteCandidatureFile.getNbPages();
			}
		}
		String totalFileSizeFormatted = PosteCandidatureFile.readableFileSize(totalFileSize);

		String maxFileSize = PosteCandidatureFile.getMaxFileSize();

		Double pagesKilo = nbPages*0.005;
		long nbRames = (long)Math.floor(nbPages/500.0);
		Long moyNbPages = 0L;
		Long moyPagesGr = 0L;
		if(posteCandidatureActifNumber != 0) {
			moyNbPages = (long)Math.floor(nbPages/posteCandidatureActifNumber);
			moyPagesGr = (long)Math.floor(pagesKilo/posteCandidatureActifNumber*1000.0);
		}

		return new StatBean(posteNumber, userNumber, adminNumber, supermanagerNumber, managerNumber, membreNumber, 
				candidatNumber, userActifNumber, candidatActifNumber, posteCandidatureNumber, posteCandidatureActifNumber, 
				posteCandidatureFileNumber, totalFileSizeFormatted, maxFileSize, nbPages, pagesKilo, nbRames, moyNbPages, moyPagesGr);
	}
}
