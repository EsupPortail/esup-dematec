package fr.univrouen.poste.web.admin;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.GalaxieMapping;
import fr.univrouen.poste.services.ExcelParser;
import fr.univrouen.poste.services.GalaxieMappingService;

@RequestMapping("/admin/galaxiemapping")
@Controller
@RooWebScaffold(path = "admin/galaxiemapping", formBackingObject = GalaxieMapping.class, delete=false, create=false)
public class GalaxieMappingController {
	
	@Autowired
	ExcelParser excelParser;

	@Autowired	
	GalaxieMappingService galaxieMappingService;
	
	private final Logger logger = Logger.getLogger(getClass());
	
	@ModelAttribute
	public GalaxieExcel getGalaxieExcel() {
		return new GalaxieExcel();
	}
	
    @RequestMapping(value = "/testFile", method = RequestMethod.POST, produces = "text/html")
    public String testFile(@Valid GalaxieExcel galaxieExcel, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) throws IOException, SQLException {
        if (bindingResult.hasErrors()) {
        	logger.warn(bindingResult.getAllErrors());
            return "redirect:/admin/galaxieexcels";
        }
        uiModel.asMap().clear();
        
        // upload file
        MultipartFile file = galaxieExcel.getFile();
        InputStream inputStream = file.getInputStream();
        
        List<List<String>> cells = excelParser.getCells(inputStream);

		Map<String, Long> cellsPosition = new HashMap<String, Long>();

		int p = 0;
		List<String> cellsHead = cells.remove(0);
		for (String cellName : cellsHead) {
			cellsPosition.put(cellName, new Long(p++));
		}
		
		try {
			galaxieMappingService.checkCellsHead(cellsPosition);
		} catch(Exception e) {
			return "redirect:/admin/galaxiemapping?test=failed";
		}
		
        return "redirect:/admin/galaxiemapping?test=success";
    }
	
}
