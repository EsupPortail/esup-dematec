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

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.springframework.stereotype.Service;

@Service
public class ExcelParser {

	private final Logger logger = Logger.getLogger(getClass());
	
	public List<List<String>>  getCells(InputStream xslFileInput) {

		List<List<String>> cellVectorHolder = new Vector<List<String>> ();

		try {

			POIFSFileSystem fileSystem = new POIFSFileSystem(xslFileInput);
			HSSFWorkbook workBook = new HSSFWorkbook(fileSystem);
			HSSFSheet sheet = workBook.getSheetAt(0);
			Iterator<Row> rowIter = sheet.rowIterator();

			while (rowIter.hasNext()) {
				HSSFRow myRow = (HSSFRow) rowIter.next();
				List<String> cellStoreVector = new Vector<String>();
				// take care of blank cell !
				// @see http://stackoverflow.com/questions/4929646/how-to-get-an-excel-blank-cell-value-in-apache-poi
				int max = myRow.getLastCellNum();
				for(int i=0; i<max; i++) {
					HSSFCell myCell = (HSSFCell) myRow.getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);
					if(Cell.CELL_TYPE_STRING == myCell.getCellType())
						cellStoreVector.add(myCell.getStringCellValue());
					else if((Cell.CELL_TYPE_NUMERIC == myCell.getCellType()))
						cellStoreVector.add(Long.toString(new Double(myCell.getNumericCellValue()).longValue()));
					else if((Cell.CELL_TYPE_BLANK == myCell.getCellType()))
						cellStoreVector.add("");
					else {
						logger.debug("This cell is not numeric or string ... : " + myCell + " \n ... cellType : " + myCell.getCellType());
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

}
