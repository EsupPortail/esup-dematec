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
package fr.univrouen.poste.services;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

@Service
public class WordParser {

	private final Logger log = Logger.getLogger(getClass());
	
	public void  modifyWord(InputStream docx, Map<String, String> textMap, OutputStream out) {
		try {
			XWPFDocument doc = new XWPFDocument(OPCPackage.open(docx));
			for (XWPFParagraph p : doc.getParagraphs()) {
			   for (XWPFRun r : p.getRuns()) {
			      String text = r.getText(0);
			      for(String key : textMap.keySet()) {
				      if (text!=null && text.contains("{{" + key + "}}")) {
				         text = text.replace("{{" + key + "}}", textMap.get(key));
				         r.setText(text, 0);
				      }
			      }
			   }
			}
			
			doc.write(out);
		} catch(Exception e) {
			log.error("Pb durant la modification du document word", e);
		}
		
	}

}
