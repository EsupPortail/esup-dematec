package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.*;
import fr.univrouen.poste.domain.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatService {

	@Resource
	UserDao userDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	LogAuthDao logAuthDao;

	@Resource
	LogFileDao logFileDao;

	@Resource
	PosteCandidatureFileDao posteCandidatureFileDao;

	@Resource
	MemberReviewFileDao memberReviewFileDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;

	@Resource
	PosteAPourvoirFileDao posteAPourvoirFileDao;
	
	public StatBean stats() {

		Long posteNumber = posteAPourvoirDao.countPosteAPourvoirs();
		Long userNumber = userDao.countUsers();
		Long adminNumber = userDao.countAdmins();
		Long supermanagerNumber = userDao.countSupermanagers();
		Long managerNumber = userDao.countManagers();
		Long membreNumber = userDao.countMembres();
		Long candidatNumber = userDao.countCandidats();
		Long userActifNumber = userDao.countActifCUsers();
		Long candidatActifNumber = userDao.countActifCandidats();
		Long posteCandidatureNumber = posteCandidatureDao.countPosteCandidatures();
		Long posteCandidatureActifNumber = posteCandidatureDao.countPosteActifCandidatures();
		Long posteCandidatureFileNumber = posteCandidatureFileDao.countPosteCandidatureFiles();

		long totalFileSize = posteCandidatureFileDao.getSumFileSize();
		long nbPages = posteCandidatureFileDao.getSumNbPages();
		String totalFileSizeFormatted = posteCandidatureFileDao.readableFileSize(totalFileSize);

		String maxFileSize = posteCandidatureFileDao.getMaxFileSize();

		Double pagesKilo = nbPages*0.005;
		long nbRames = (long)Math.floor(nbPages/500.0);
		Long moyNbPages = 0L;
		Long moyPagesGr = 0L;
		if(posteCandidatureActifNumber != 0) {
			moyNbPages = (long)Math.floor(nbPages/posteCandidatureActifNumber);
			moyPagesGr = (long)Math.floor(pagesKilo/posteCandidatureActifNumber*1000.0);
		}
		
		Long memberReviewFileNumber = memberReviewFileDao.countMemberReviewFiles();
		long totalMemberReviewFileSize = memberReviewFileDao.getSumFileSize();
		String totalMemberReviewFileSizeFormatted =  posteCandidatureFileDao.readableFileSize(totalMemberReviewFileSize);
		
		Long posteAPourvoirFileNumber = posteAPourvoirFileDao.countPosteAPourvoirFiles();
		long totalposteAPourvoirFileSize = posteAPourvoirFileDao.getSumFileSize();
		String totalposteAPourvoirFileSizeFormatted  =  posteCandidatureFileDao.readableFileSize(totalposteAPourvoirFileSize);

		return new StatBean(posteNumber, userNumber, adminNumber, supermanagerNumber, managerNumber, membreNumber, 
				candidatNumber, userActifNumber, candidatActifNumber, posteCandidatureNumber, posteCandidatureActifNumber, 
				posteCandidatureFileNumber, totalFileSizeFormatted, maxFileSize, nbPages, pagesKilo, nbRames, moyNbPages, moyPagesGr,
				memberReviewFileNumber, totalMemberReviewFileSizeFormatted, posteAPourvoirFileNumber, totalposteAPourvoirFileSizeFormatted);
	}
	
	
	public List<List<String>> countUploadLogFilesBydate() {
		List<Object[]> logfilesCounts = logFileDao.countUploadLogFilesBydate();
		return map4chart(logfilesCounts);
	}

	public List<List<String>> countSuccessLogAuthsByDate() {
		List<Object[]> logfilesCounts = logAuthDao.countSuccessLogAuthsByDate();
		return map4chart(logfilesCounts);
	}
	
	public List<List<String>> sumPosteCandidatureFileSizeByDate() {
		List<Object[]> logFilesSizes = posteCandidatureFileDao.sumPosteCandidatureFileSizeByDate();
		return map4chart(logFilesSizes);
	}
	
	public List<List<String>> sumMemberReviewFileSizeByDate() {
		List<Object[]> logFilesSizes = memberReviewFileDao.sumMemberReviewFileSizeByDate();
		return map4chart(logFilesSizes);
	}
	
	public List<List<String>> sumPosteAPourvoirFileSizeByDate() {
		List<Object[]> logFilesSizes = posteAPourvoirFileDao.sumPosteAPourvoirFileSizeByDate();
		return map4chart(logFilesSizes);
	}

	List<List<String>> map4chart(List<Object[]> logfilesCountsAsObjects) {
		List<String[]> logfilesCounts = logfilesCountsAsObjects.stream()
				.map(row -> Arrays.stream(row)
						.map(obj -> obj != null ? obj.toString() : null)
						.toArray(String[]::new))
				.collect(Collectors.toList());
		List<List<String>> labelsValues = new ArrayList<List<String>>();
		List<String> labels = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		for(String[] logfilesCount : logfilesCounts) {
			String label = logfilesCount[2] + "/" + logfilesCount[1] + "/" + logfilesCount[0];
			label = label.replaceAll("\\.0", "");
			String value = logfilesCount[3];
			labels.add(label);
			values.add(value);
		}
		labelsValues.add(labels);
		labelsValues.add(values);
		return labelsValues;
	}
	
}
