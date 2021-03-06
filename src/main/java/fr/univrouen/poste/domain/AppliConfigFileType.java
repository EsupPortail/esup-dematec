package fr.univrouen.poste.domain;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class AppliConfigFileType {
	
	public static final List<String> fieldNames4OrderClauseFilter = Arrays.asList("typeTitle", "typeDescription", "candidatureFileMoSizeMax", "candidatureNbFileMax", "candidatureContentTypeRestrictionRegexp", "candidatureFilenameRestrictionRegexp", "id", "listIndex", "listIndex, id");
	
	@Column(columnDefinition="TEXT")
	private String typeTitle;
	
	@Column(columnDefinition="TEXT")
	private String typeDescription;
	
    @Column
    @NotNull
	private Long candidatureFileMoSizeMax = new Long(-1); 
	
    @Column
    @NotNull
	private Long candidatureNbFileMax = new Long(-1); 
	
    @Column(columnDefinition="TEXT")
	private String candidatureContentTypeRestrictionRegexp = ".*"; 
    
    @Column(columnDefinition="TEXT")
	private String candidatureFilenameRestrictionRegexp = ".*";

    @Column
    @NotNull
	private Long listIndex = new Long(0); 
    
	public static AppliConfigFileType getDefaultFileType() {
		return AppliConfigFileType.findAllAppliConfigFileTypes().get(0);
	} 
	
	public Long getListIndex() {
		return listIndex == null ? Long.valueOf(0) : listIndex;
	}
	
}

