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
import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Index;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString(excludeFields = {"candidat","poste","candidature"})
@RooJpaActiveRecord(finders = { "findGalaxieEntrysByNumEmploiAndNumCandidat", "findGalaxieEntrysByCandidat", "findGalaxieEntrysByCandidatIsNull", "findGalaxieEntrysByPosteIsNull", "findGalaxieEntrysByCandidatureIsNull", "findGalaxieEntrysByCandidature" })
public class GalaxieEntry {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("numEmploi", "numCandidat", "civilite", "nom", "prenom", "email", "localisation", "profil", "candidat", "poste", "candidature", "numEmploi,numCandidat");

    @Index(name="num_emploi_index")
    private String numEmploi;

    @Index(name="num_candidat_index")
    private String numCandidat;

    private String civilite = "";

    private String nom = "";

    private String prenom = "";

    private String email;

    private String localisation = "";

    private String profil = "";

    private String etatDossier = "";

    @ManyToOne(fetch=FetchType.LAZY)
    private User candidat;

    @ManyToOne(fetch=FetchType.LAZY)
    private PosteAPourvoir poste;

    @OneToOne(fetch=FetchType.LAZY)
    private PosteCandidature candidature;

    // don't care of upper/lower case for authentication with email ...
    public void setEmail(String email) {
        if (email != null) this.email = email.toLowerCase(); else this.email = null;
    }

    public Boolean getDeletable() {
        return candidat == null && poste == null && candidature == null;
    }

    public static List<GalaxieEntry> findAllGalaxieEntrysWithCandidatNull() {
        return entityManager().createQuery("SELECT o FROM GalaxieEntry o WHERE o.candidat is NULL", GalaxieEntry.class).getResultList();
    }

    public static List<GalaxieEntry> findAllGalaxieEntrysWithPosteNull() {
        return entityManager().createQuery("SELECT o FROM GalaxieEntry o WHERE o.poste is NULL", GalaxieEntry.class).getResultList();
    }
}
