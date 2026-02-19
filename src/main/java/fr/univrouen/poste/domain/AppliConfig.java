/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AppliConfig {
	
	public enum MailReturnReceiptModeTypes {NEVER, EACH_UPLOAD, EACH_SESSION}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
            name = "my_seq",
            sequenceName = "hibernate_sequence",
            allocationSize = 1
    )
	Long id;

	@DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    LocalDateTime dateEndCandidat;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    LocalDateTime dateEndCandidatActif;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    LocalDateTime dateEndMembre;

    //General
    @Column(columnDefinition="TEXT")
    String titre;

    @Column(columnDefinition="TEXT")
    String imageUrl;
    
    @Column(columnDefinition="TEXT")
    String piedPage;
    
    @Column(columnDefinition="TEXT")
    String mailFrom;

    @Column(columnDefinition="TEXT")
    String mailSubject;

    @Column(columnDefinition="TEXT")
    String texteMailActivation;
    
    @Column(columnDefinition="TEXT")
    String texteMailPasswordOublie;
    
    
    // Anonyme
    
    @Column(columnDefinition="TEXT")
    String textePremierePageAnonyme;


    // Membre
    
    @Column(columnDefinition="TEXT")
    String texteMembreAideCandidatures;
    
    @Column(columnDefinition="TEXT")
    String mailSubjectMembre;

    @Column(columnDefinition="TEXT")
    String texteMailActivationMembre;
    
    @Column(columnDefinition="TEXT")
    String texteMailNewCommissions;
    
    @Column
    Boolean membreSupprReviewFile;
    
    // Candidat
    
    @Column(columnDefinition="TEXT")
    String textePremierePageCandidat;

    @Column(columnDefinition="TEXT")
    String textePremierePageMembre;
    
    @Column(columnDefinition="TEXT")
    String texteCandidatAideCandidatures;
    
    @Column(columnDefinition="TEXT")
    String texteCandidatAideCandidatureDepot;

    @Column
    @Enumerated(EnumType.STRING)
    MailReturnReceiptModeTypes mailReturnReceiptModeType;
    
    @Column(columnDefinition="TEXT")
	String texteMailNewCandidatures;
	
    @Column(columnDefinition="TEXT")
    String texteMailCandidatReturnReceipt;
    
    @Column(columnDefinition="TEXT")
    String texteEnteteMailCandidatAuditionnable;
    
    @Column(columnDefinition="TEXT")
    String textePiedpageMailCandidatAuditionnable;
    
    @Column(columnDefinition="TEXT")
    String colorCandidatureNonVue;
    
    @Column(columnDefinition="TEXT")
    String colorCandidatureVue;
    
    @Column(columnDefinition="TEXT")
    String colorCandidatureVueIncomplet;
    
    @Column(columnDefinition="TEXT")
    String colorCandidatureVueModifieDepuis;
    
    @Column(columnDefinition="TEXT")
    String colorCandidatureVueIncompletModifieDepuis;

    @Column
    @Enumerated(EnumType.STRING)
    RecevableEnum candidatureRecevableEnumDefault;
    
    @Column
    Boolean candidatCanSignup;
    
    @Column
    String colorReporterTag;
    
    @Column(name = "postes_menu4members")
    Boolean postesMenu4Members;
    
    @Column
    Boolean presidentReportersView;
    
    @Column(columnDefinition="TEXT", name="texte_postes_menu4members")
    String textePostesMenu4Members;
    
    @Column
    Boolean laureatEnable;  
    
    @Column(columnDefinition="TEXT")
	String texteMailCandidatLaureat;

}

