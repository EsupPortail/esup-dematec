package fr.univrouen.poste.domain;

import javax.persistence.Column;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class AppliConfigFileType {
	
	@Column(columnDefinition="TEXT")
	private String typeTitle;
	
	@Column(columnDefinition="TEXT")
	private String typeDescription;
	
    @Column
	private Long candidatureFileMoSizeMax = new Long(-1); 
	
    @Column
	private Long candidatureNbFileMax = new Long(-1); 
	
    @Column(columnDefinition="TEXT")
	private String candidatureContentTypeRestrictionRegexp = ".*"; 
    
    @Column(columnDefinition="TEXT")
	private String candidatureFilenameRestrictionRegexp = ".*";

	public static AppliConfigFileType getDefaultFileType() {
		return AppliConfigFileType.findAllAppliConfigFileTypes().get(0);
	} 
	
}
