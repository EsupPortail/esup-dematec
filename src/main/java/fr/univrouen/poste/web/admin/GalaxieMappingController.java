package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.dao.GalaxieMappingDao;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.GalaxieMapping;
import fr.univrouen.poste.services.ExcelParser;
import fr.univrouen.poste.services.GalaxieMappingService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/admin/galaxiemapping")
@Controller
public class GalaxieMappingController {

    final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	ExcelParser excelParser;

	@Resource
	GalaxieMappingService galaxieMappingService;

    @Resource
    GalaxieMappingDao galaxieMappingDao;

	@ModelAttribute
	public GalaxieExcel getGalaxieExcel() {
		return new GalaxieExcel();
	}
	
    @RequestMapping(value = "/testFile", method = RequestMethod.POST, produces = "text/html")
    public String testFile(@Valid GalaxieExcel galaxieExcel, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest, final RedirectAttributes redirectAttributes) throws IOException, SQLException {
        if (bindingResult.hasErrors()) {
        	logger.warn("Errors on testFile method : {}", bindingResult.getAllErrors());
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
			cellsPosition.put(cellName, Long.valueOf(p++));
		}
		
		try {
			galaxieMappingService.checkCellsHead(cellsPosition);
		} catch(Exception e) {
			redirectAttributes.addFlashAttribute("test", "failed");
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/admin/galaxiemapping";
		}
		
		redirectAttributes.addFlashAttribute("test", "success");
        return "redirect:/admin/galaxiemapping";
    }


	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, Model uiModel) {
        Page<GalaxieMapping> result = galaxieMappingDao.findGalaxieMappingEntries(pageable);
        uiModel.addAttribute("galaxiemappings", result.getContent());
        uiModel.addAttribute("maxPages", result.getTotalPages());
        return "admin/galaxiemapping/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid GalaxieMapping galaxieMapping, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, galaxieMapping);
            return "admin/galaxiemapping/update";
        }
        uiModel.asMap().clear();
        galaxieMappingDao.saveGalaxieMapping(galaxieMapping);
        return "redirect:/admin/galaxiemapping";
    }

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, galaxieMappingDao.findGalaxieMapping(id));
        return "admin/galaxiemapping/update";
    }

	void populateEditForm(Model uiModel, GalaxieMapping galaxieMapping) {
        uiModel.addAttribute("galaxieMapping", galaxieMapping);
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
