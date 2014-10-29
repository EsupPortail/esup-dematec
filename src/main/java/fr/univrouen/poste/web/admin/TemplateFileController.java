package fr.univrouen.poste.web.admin;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.serial.SerialBlob;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import fr.univrouen.poste.domain.TemplateFile;

@RequestMapping("/admin/templatefiles")
@Controller
@RooWebScaffold(path = "admin/templatefiles", formBackingObject = TemplateFile.class)
public class TemplateFileController {


	private final Logger logger = Logger.getLogger(getClass());

	@RequestMapping(value = "/addFile", method = RequestMethod.POST, produces = "text/html")
	public String addFile(@Valid TemplateFile templateFile, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) throws IOException, SQLException {
		if (bindingResult.hasErrors()) {
			logger.warn(bindingResult.getAllErrors());
			return "redirect:/admin/templatefiles";
		}
		uiModel.asMap().clear();

		// upload file
		MultipartFile file = templateFile.getFile();
		String filename = file.getOriginalFilename();
		InputStream inputStream = file.getInputStream();
		byte[] bytes = IOUtils.toByteArray(inputStream);

		templateFile.setFilename(filename);
		templateFile.getBigFile().setBinaryFile(new SerialBlob(bytes)); 
		templateFile.getBigFile().persist();

		// set current date 
		Calendar cal = Calendar.getInstance();
		templateFile.setSendTime(cal.getTime());

		// persist
		templateFile.persist();

		return "redirect:/admin/templatefiles";
	}
	
    void populateEditForm(Model uiModel, TemplateFile templateFile) {
        uiModel.addAttribute("templateFile", templateFile);
        addDateTimeFormatPatterns(uiModel);
    }

}

