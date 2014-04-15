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

import java.text.DecimalFormat;
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
@RooToString(excludeFields = "bigFile,file")
@RooJpaActiveRecord
public class PosteCandidatureFile implements DematFile {

    private String filename;

    @Transient
    private MultipartFile file;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern="dd/MM/yyyy HH:mm")
    private Date sendTime;

    private Long fileSize;
    
    private Long nbPages;
    
    private Boolean writeable = true;

    private String contentType;
    
    @OneToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval=true)
    private BigFile bigFile = new BigFile();
    
    @Transient
    public String getFileSizeFormatted() {
    	return readableFileSize(fileSize.longValue());
    }
    
    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

	public static String getMaxFileSize() {
		List<PosteCandidatureFile> files = entityManager().createQuery("SELECT o FROM PosteCandidatureFile o order by o.fileSize desc ", PosteCandidatureFile.class).setMaxResults(1).getResultList();
		if(!files.isEmpty())
			return files.get(0).getFileSizeFormatted();
		else 
			return "Nan";
    }
}
