package fr.univrouen.poste.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@Setter
public class AppliConfigFileType {
	
	public static final List<String> fieldNames4OrderClauseFilter = Arrays.asList("typeTitle", "typeDescription", "candidatureFileMoSizeMax", "candidatureNbFileMax", "candidatureContentTypeRestrictionRegexp", "candidatureFilenameRestrictionRegexp", "id", "listIndex", "listIndex, id");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
            name = "my_seq",
            sequenceName = "hibernate_sequence",
            allocationSize = 1
    )
    Long id;


    @Column(columnDefinition="TEXT")
	String typeTitle;
	
	@Column(columnDefinition="TEXT")
	String typeDescription;
	
    @Column
    @NotNull
	Long candidatureFileMoSizeMax = Long.valueOf(-1);
	
    @Column
    @NotNull
	Long candidatureNbFileMax = Long.valueOf(-1);
	
    @Column(columnDefinition="TEXT")
	String candidatureContentTypeRestrictionRegexp = ".*"; 
    
    @Column(columnDefinition="TEXT")
	String candidatureFilenameRestrictionRegexp = ".*";

    @Column
    @NotNull
	Long listIndex = Long.valueOf(0);

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}

