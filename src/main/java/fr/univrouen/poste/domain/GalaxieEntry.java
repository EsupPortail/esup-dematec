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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Table(indexes = {
        @Index(name = "num_emploi_index", columnList = "numEmploi"),
        @Index(name = "num_candidat_index", columnList = "numCandidat")
})
@Getter
@Setter
public class GalaxieEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
            name = "my_seq",
            sequenceName = "hibernate_sequence",
            allocationSize = 1
    )
    Long id;


    @NotEmpty
    String numEmploi;

    @NotEmpty
    String numCandidat;

    String civilite = "";

    String nom = "";

    String prenom = "";

    String email;
    
    @Column(length=300)
    String localisation = "";

    @Column(length=300)
    String profil = "";

    String etatDossier = "";

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "candidat")
    User candidat;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "poste")
    PosteAPourvoir poste;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "candidature")
    PosteCandidature candidature;

	public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("candidat", "poste", "candidature").toString();
    }

    // don't care of upper/lower case for authentication with email ...
    public void setEmail(String email) {
        if (email != null) this.email = email.toLowerCase(); else this.email = null;
    }

    public Boolean getDeletable() {
        return candidat == null && poste == null && candidature == null;
    }


}
