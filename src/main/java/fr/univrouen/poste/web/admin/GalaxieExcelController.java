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
import fr.univrouen.poste.dao.GalaxieExcelDao;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.services.ExcelParser;
import fr.univrouen.poste.services.GalaxieExcelParser;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

@RequestMapping("/admin/galaxieexcels")
@Controller
public class GalaxieExcelController {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	GalaxieExcelParser galaxieExcelParser;
	
	@Resource
	ExcelParser excelParser;

    @Resource
    GalaxieExcelDao galaxieExcelDao;

    @Resource
    BigFileDao bigFileDao;

    @RequestMapping(value = "/addFile", method = RequestMethod.POST, produces = "text/html")
    public String addFile(@Valid GalaxieExcel galaxieExcel, BindingResult bindingResult) throws IOException, SQLException {
        if (bindingResult.hasErrors()) {
        	logger.warn("Errors on addFile method : {}", bindingResult.getAllErrors());
            return "redirect:/admin/galaxieexcels";
        }
        
        // upload file
        MultipartFile file = galaxieExcel.getFile();
        String filename = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);

        galaxieExcel.setFilename(filename);
        galaxieExcel.getBigFile().setBinaryFile(new SerialBlob(bytes));
        bigFileDao.saveBigFile( galaxieExcel.getBigFile());
        
        // set current date 
        Calendar cal = Calendar.getInstance();
        galaxieExcel.setCreation(cal.getTime());    
        
        // persist
        galaxieExcelDao.saveGalaxieExcel(galaxieExcel);
        
        // process : generate GalaxieEntries
    	galaxieExcelParser.process(galaxieExcel);
        
        return "redirect:/admin/galaxieexcels";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    @Transactional(readOnly = true)
    public String show(@PathVariable Long id, Model uiModel) throws SQLException, IOException {
        addDateTimeFormatPatterns(uiModel);
        
        GalaxieExcel galaxieExcel = galaxieExcelDao.findGalaxieExcel(id);
    	List<List<String>> cells = excelParser.getCells(galaxieExcel);
    	galaxieExcel.setCells(cells);
       
        uiModel.addAttribute("galaxieexcel", galaxieExcel);
        uiModel.addAttribute("itemId", id);
        return "admin/galaxieexcels/show";
    }

    @Transactional(readOnly = true)
    @RequestMapping(value = "/{id}/file")
    public void downloadFile(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    	try {
    		GalaxieExcel galaxieExcel = galaxieExcelDao.findGalaxieExcel(id);
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


	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid GalaxieExcel galaxieExcel, BindingResult bindingResult, Model uiModel, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, galaxieExcel);
            return "admin/galaxieexcels/create";
        }
        galaxieExcelDao.saveGalaxieExcel(galaxieExcel);
        return "redirect:/admin/galaxieexcels/" + encodeUrlPathSegment(galaxieExcel.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new GalaxieExcel());
        return "admin/galaxieexcels/create";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "creation") Pageable pageable, Model uiModel) {
        Page<GalaxieExcel> page = galaxieExcelDao.findGalaxieExcelEntries(pageable);
        uiModel.addAttribute("galaxieexcels", page);
        addDateTimeFormatPatterns(uiModel);
        return "admin/galaxieexcels/list";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("galaxieExcel_creation_date_format", "dd/MM/yyyy HH:mm");
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
