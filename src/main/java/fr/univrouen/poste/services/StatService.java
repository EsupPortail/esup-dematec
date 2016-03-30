package fr.univrouen.poste.services;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.LogAuth;
import fr.univrouen.poste.domain.LogFile;
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

		long totalFileSize = PosteCandidatureFile.getSumFileSize();
		long nbPages = PosteCandidatureFile.getSumNbPages();
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
	
	
	public List<List<Object>> countUploadLogFilesBydate() {
		List<Object[]> logfilesCounts = LogFile.countUploadLogFilesBydate();
		return map4chart(logfilesCounts);
	}

	public List<List<Object>> countSuccessLogAuthsByDate() {
		List<Object[]> logfilesCounts = LogAuth.countSuccessLogAuthsByDate();
		return map4chart(logfilesCounts);
	}
	
	public List<List<Object>> sumPosteCandidatureFileSizeByDate() {
		List<Object[]> logFilesSizes = PosteCandidatureFile.sumPosteCandidatureFileSizeByDate();
		return map4chart(logFilesSizes);
	}

	private List<List<Object>> map4chart(List<Object[]> logfilesCounts) {
		List<List<Object>> labelsValues = new ArrayList<List<Object>>();
		List<Object> labels = new ArrayList<Object>();
		List<Object> values = new ArrayList<Object>();
		for(Object[] logfilesCount : logfilesCounts) {
			String label = logfilesCount[2] + "/" + logfilesCount[1] + "/" + logfilesCount[0];
			label = label.replaceAll("\\.0", "");
			Long value = null;
			if(logfilesCount[3] instanceof BigDecimal) {
				value = ((BigDecimal)logfilesCount[3]).longValue();
			} else {
				value = ((BigInteger)logfilesCount[3]).longValue();
			}
			labels.add(label);
			values.add(value);
		}
		labelsValues.add(labels);
		labelsValues.add(values);
		return labelsValues;
	}
	
}
