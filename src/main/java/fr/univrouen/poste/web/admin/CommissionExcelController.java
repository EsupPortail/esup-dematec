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

import fr.univrouen.poste.dao.BigFileDao;
import fr.univrouen.poste.dao.CommissionExcelDao;
import fr.univrouen.poste.domain.CommissionExcel;
import fr.univrouen.poste.services.CommissionExcelParser;
import fr.univrouen.poste.services.ExcelParser;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDateTime;

@RequestMapping("/admin/commissionexcels")
@Controller
@Transactional
public class CommissionExcelController {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	ExcelParser excelParser;
	
	@Resource
	CommissionExcelParser commissionExcelParser;

	@Resource
	CommissionExcelDao commissionExcelDao;

	@Resource
	BigFileDao bigFileDao;

    void populateEditForm(Model uiModel, CommissionExcel commissionExcel) {
        uiModel.addAttribute("commissionExcel", commissionExcel);
        addDateTimeFormatPatterns(uiModel);
    }
    
    @RequestMapping(value = "/addFile", method = RequestMethod.POST, produces = "text/html")
    public String addFile(@Valid CommissionExcel commissionExcel, BindingResult bindingResult) throws IOException, SQLException {
        if (bindingResult.hasErrors()) {
        	logger.warn("Errors on addFile method : {}", bindingResult.getAllErrors());
            return "redirect:/admin/commissionexcels";
        }

        // upload file
        MultipartFile file = commissionExcel.getFile();
        String filename = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);

        commissionExcel.setFilename(filename);
        commissionExcel.getBigFile().setBinaryFile(new SerialBlob(bytes)); 
        bigFileDao.saveBigFile(commissionExcel.getBigFile());

        // set current date 
        
        commissionExcel.setCreation(LocalDateTime.now());    
        
        // persist
        commissionExcelDao.saveCommissionExcel(commissionExcel);

        // process : generate CommissionEntries
    	commissionExcelParser.process(commissionExcel);
        
        return "redirect:/admin/commissionexcels";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String show(@PathVariable Long id, Model uiModel) throws SQLException, IOException {
        addDateTimeFormatPatterns(uiModel);
        
        CommissionExcel commissionExcel = commissionExcelDao.findCommissionExcel(id);
    	InputStream xslInputStream = commissionExcel.getBigFile().getBinaryFile().getBinaryStream();
    	
    	// hack : transform getBinaryStream from postgresql as ByteArrayInputStream
    	// using directly xslInputStream I get : 
    	// org.apache.poi.poifs.filesystem.NotOLE2FileException: Invalid header signature; read 0x0000000000000000, expected 0xE11AB1A1E011CFD0 - Your file appears not to be a valid OLE2 document
    	byte[] xslBytes = IOUtils.toByteArray(xslInputStream);
    	ByteArrayInputStream bis = new ByteArrayInputStream(xslBytes);
    	
    	List<List<String>> cells = excelParser.getCells(bis);
    	commissionExcel.setCells(cells);
       
        uiModel.addAttribute("commissionexcel", commissionExcel);
        uiModel.addAttribute("itemId", id);
        return "admin/commissionexcels/show";
    }
    
    @RequestMapping(value = "/{id}/file")
    public void downloadFile(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    	try {
    		CommissionExcel commissionExcel = commissionExcelDao.findCommissionExcel(id);
    		String filename = commissionExcel.getFilename();
    		String contentType = "application/vnd.ms-office";
    		response.setContentType(contentType);
    		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    		IOUtils.copy(commissionExcel.getBigFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
    	} catch(IOException ioe) {
    		String ip = request.getRemoteAddr();	
    		logger.info("Download IOException, that can be just because the client [" + ip +
    				"] canceled the download process : " + ioe.getCause());
    	}
    }
    

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid CommissionExcel commissionExcel, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, commissionExcel);
            return "admin/commissionexcels/create";
        }
        commissionExcelDao.saveCommissionExcel(commissionExcel);
        return "redirect:/admin/commissionexcels/" + encodeUrlPathSegment(commissionExcel.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new CommissionExcel());
        return "admin/commissionexcels/create";
    }


	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (pageable.isPaged()) {
            Page<CommissionExcel> page = commissionExcelDao.findCommissionExcelEntries(pageable, sortFieldName, sortOrder);
            uiModel.addAttribute("commissionexcels", page);
        } else {
            uiModel.addAttribute("commissionexcels", commissionExcelDao.findAllCommissionExcels(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/commissionexcels/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid CommissionExcel commissionExcel, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, commissionExcel);
            return "admin/commissionexcels/update";
        }
        commissionExcelDao.saveCommissionExcel(commissionExcel);
        return "redirect:/admin/commissionexcels/" + encodeUrlPathSegment(commissionExcel.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, commissionExcelDao.findCommissionExcel(id));
        return "admin/commissionexcels/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id) {
        CommissionExcel commissionExcel = commissionExcelDao.findCommissionExcel(id);
        commissionExcelDao.deleteCommissionExcel(commissionExcel);
        return "redirect:/admin/commissionexcels";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("commissionExcel_creation_date_format", "dd/MM/yyyy HH:mm");
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
}
