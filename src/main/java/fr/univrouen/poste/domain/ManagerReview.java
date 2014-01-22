package fr.univrouen.poste.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class ManagerReview {
	
	public static enum ReviewStatusTypes {Non_vue, Vue, Vue_mais_modifie_depuis, Vue_incomplet, Vue_incomplet_mais_modifie_depuis};

    @NotNull
    @ManyToOne
    private User manager;
    
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date reviewDate;
    
    @Column
    @NotNull
    @Enumerated(EnumType.STRING)
    private ReviewStatusTypes reviewStatus = ReviewStatusTypes.Non_vue;
    
}
