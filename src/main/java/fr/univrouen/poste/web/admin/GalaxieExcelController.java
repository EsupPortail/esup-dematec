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
package fr.univrouen.poste.web.admin;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.serial.SerialBlob;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.services.ExcelParser;
import fr.univrouen.poste.services.GalaxieExcelParser;
import fr.univrouen.poste.services.LogService;

@RequestMapping("/admin/galaxieexcels")
@Controller
@RooWebScaffold(path = "admin/galaxieexcels", formBackingObject = GalaxieExcel.class)
@Transactional
public class GalaxieExcelController {
	
	private final Logger logger = Logger.getLogger(getClass());
	
	@Autowired 
	GalaxieExcelParser galaxieExcelParser;
	
	@Autowired 	
	ExcelParser excelParser;
	

    @RequestMapping(value = "/addFile", method = RequestMethod.POST, produces = "text/html")
    public String addFile(@Valid GalaxieExcel galaxieExcel, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) throws IOException, SQLException {
        if (bindingResult.hasErrors()) {
        	logger.warn(bindingResult.getAllErrors());
            return "redirect:/admin/galaxieexcels";
        }
        uiModel.asMap().clear();
        
        // upload file
        MultipartFile file = galaxieExcel.getFile();
        String filename = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);

        galaxieExcel.setFilename(filename);
        galaxieExcel.getBigFile().setBinaryFile(new SerialBlob(bytes)); 
        galaxieExcel.getBigFile().persist();
        
        // set current date 
        Calendar cal = Calendar.getInstance();
        galaxieExcel.setCreation(cal.getTime());    
        
        // persist
        galaxieExcel.persist();
        
        // process : generate GalaxieEntries
    	galaxieExcelParser.process(galaxieExcel);
        
        return "redirect:/admin/galaxieexcels";
    }

    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) throws SQLException {
        addDateTimeFormatPatterns(uiModel);
        
        GalaxieExcel galaxieExcel = GalaxieExcel.findGalaxieExcel(id);
    	InputStream xslFile = galaxieExcel.getBigFile().getBinaryFile().getBinaryStream();
    	List<List<String>> cells = excelParser.getCells(xslFile);
    	galaxieExcel.setCells(cells);
       
        uiModel.addAttribute("galaxieexcel", galaxieExcel);
        uiModel.addAttribute("itemId", id);
        return "admin/galaxieexcels/show";
    }
    
    @RequestMapping(value = "/{id}/file")
    public void downloadFile(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    	try {
    		GalaxieExcel galaxieExcel = GalaxieExcel.findGalaxieExcel(id);
    		String filename = galaxieExcel.getFilename();
    		String contentType = "application/vnd.ms-office";
    		response.setContentType(contentType);
    		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    		IOUtils.copy(galaxieExcel.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
    	} catch(IOException ioe) {
    		String ip = request.getRemoteAddr();	
    		logger.info("Download IOException, that can be just because the client [" + ip +
    				"] canceled the download process : " + ioe.getCause());
    	}
    }
    
    void populateEditForm(Model uiModel, GalaxieExcel galaxieExcel) {
        uiModel.addAttribute("galaxieExcel", galaxieExcel);
        addDateTimeFormatPatterns(uiModel);
    }

}
