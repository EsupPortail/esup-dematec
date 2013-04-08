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

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.web.multipart.MultipartFile;

@RooJavaBean
@RooToString(excludeFields="bigFile,file,cells")
@RooJpaActiveRecord
public class GalaxieExcel {
	
    private String filename;

    @OneToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval=true)
    private BigFile bigFile = new BigFile();
    
    @Transient
    private MultipartFile file;
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern="dd/MM/yyyy HH:mm")
    private Date creation;
    
    @Transient
    private  List<List<String>> cells;
    
    public static List<GalaxieExcel> findAllGalaxieExcels() {
        return entityManager().createQuery("SELECT o FROM GalaxieExcel o order by o.creation desc", GalaxieExcel.class).getResultList();
    }
    
    public static List<GalaxieExcel> findGalaxieExcelEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM GalaxieExcel o order by o.creation desc", GalaxieExcel.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }


}
