package fr.univrouen.poste.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Getter
@Setter
public class ManagerReview {
	
	public enum ReviewStatusTypes {Non_vue, Vue, Vue_mais_modifie_depuis, Vue_incomplet, Vue_incomplet_mais_modifie_depuis}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
            name = "my_seq",
            sequenceName = "hibernate_sequence",
            allocationSize = 1
    )
    Long id;


    @ManyToOne
    @JoinColumn(name = "manager")
    User manager;
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    Date reviewDate;
    
    @Column
    @NotNull
    @Enumerated(EnumType.STRING)
    ReviewStatusTypes reviewStatus = ReviewStatusTypes.Non_vue;



}
