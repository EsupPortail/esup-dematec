package fr.univrouen.poste.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class GalaxieMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
            name = "my_seq",
            sequenceName = "hibernate_sequence",
            allocationSize = 1
    )
	Long id;


    String id_numemploi;
    
    String id_numCandidat;
    
    String id_civilite;
    
    String id_nom;
    
    String id_prenom;
    
    String id_email;
    
    String id_localisation;
    
    String id_profil;
    
    String id_etat_dossier;

}
