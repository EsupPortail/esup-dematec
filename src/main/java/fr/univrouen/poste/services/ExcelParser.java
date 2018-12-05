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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.GalaxieExcel;

@Service
public class ExcelParser {

	private final Logger logger = Logger.getLogger(getClass());
	
	public List<List<String>>  getCells(InputStream xslFileInput) {

		List<List<String>> cellVectorHolder = new Vector<List<String>> ();

		try {
			Workbook workBook = WorkbookFactory.create(xslFileInput);
			Sheet sheet = workBook.getSheetAt(0);
			Iterator<Row> rowIter = sheet.rowIterator();

			while (rowIter.hasNext()) {
				Row myRow = (Row) rowIter.next();
				List<String> cellStoreVector = new Vector<String>();
				// take care of blank cell !
				// @see http://stackoverflow.com/questions/4929646/how-to-get-an-excel-blank-cell-value-in-apache-poi
				int max = myRow.getLastCellNum();
				for(int i=0; i<max; i++) {
					Cell myCell = (Cell) myRow.getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);
					if(CellType.STRING == myCell.getCellTypeEnum())
						cellStoreVector.add(myCell.getStringCellValue());
					else if((CellType.NUMERIC == myCell.getCellTypeEnum()))
						cellStoreVector.add(Long.toString(new Double(myCell.getNumericCellValue()).longValue()));
					else if((CellType.BLANK == myCell.getCellTypeEnum()))
						cellStoreVector.add("");
					else {
						logger.debug("This cell is not numeric or string ... : " + myCell + " \n ... getCellTypeEnum : " + myCell.getCellTypeEnum());
						cellStoreVector.add("");
					}
				}
				cellVectorHolder.add(cellStoreVector);
			}
		} catch (Exception e) {
			logger.error("Error during parsing the XSL File", e);
			throw new RuntimeException("Error during parsing the XSL File", e);
		}

		return cellVectorHolder;
	}

	public List<List<String>> getCells(GalaxieExcel galaxieExcel) throws SQLException, IOException {
		// hack : transform getBinaryStream from postgresql as ByteArrayInputStream
    	// using directly xslInputStream I get : 
    	// org.apache.poi.poifs.filesystem.NotOLE2FileException: Invalid header signature; read 0x0000000000000000, expected 0xE11AB1A1E011CFD0 - Your file appears not to be a valid OLE2 document
		InputStream xslInputStream = galaxieExcel.getBigFile().getBinaryFile().getBinaryStream();
		byte[] xslBytes = IOUtils.toByteArray(xslInputStream);
    	ByteArrayInputStream bis = new ByteArrayInputStream(xslBytes);
    	
    	return this.getCells(bis);
	}

}
