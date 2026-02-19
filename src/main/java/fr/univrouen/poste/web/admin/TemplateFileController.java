package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.dao.BigFileDao;
import fr.univrouen.poste.dao.PosteCandidatureTagDao;
import fr.univrouen.poste.dao.TemplateFileDao;
import fr.univrouen.poste.domain.TemplateFile;
import fr.univrouen.poste.domain.TemplateFile.TemplateFileType;
import fr.univrouen.poste.services.TemplateService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.io.IOUtils;
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

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDateTime;

@RequestMapping("/admin/templatefiles")
@Controller
public class TemplateFileController {

    final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	TemplateFileDao templateFileDao;

	@Resource
	PosteCandidatureTagDao posteCandidatureTagDao;
	
	@Resource
	TemplateService templateService;

    @Resource
    BigFileDao bigFileDao;
	
	@ModelAttribute("templateFileTypes")
	public List<TemplateFileType> getTemplateFileTypeEnum() {
		return Arrays.asList(TemplateFileType.values());
	}

	@RequestMapping(value = "/addFile", method = RequestMethod.POST, produces = "text/html")
	public String addFile(@Valid TemplateFile templateFile, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) throws IOException, SQLException {
		if (bindingResult.hasErrors()) {
			logger.warn("Errors on addFile method : {}", bindingResult.getAllErrors());
			return "redirect:/admin/templatefiles";
		}

		// upload file
		MultipartFile file = templateFile.getFile();
		String filename = file.getOriginalFilename();
		InputStream inputStream = file.getInputStream();
		byte[] bytes = IOUtils.toByteArray(inputStream);

		templateFile.setFilename(filename);
		templateFile.getBigFile().setBinaryFile(new SerialBlob(bytes));
        bigFileDao.saveBigFile(templateFile.getBigFile());

		// set current date 
		
		templateFile.setSendTime(LocalDateTime.now());

		// persist
		templateFileDao.saveTemplateFile(templateFile);

		return "redirect:/admin/templatefiles";
	}
	
    void populateEditForm(Model uiModel, TemplateFile templateFile) {
        uiModel.addAttribute("templateFile", templateFile);
        addDateTimeFormatPatterns(uiModel);
    }
    
    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if(sortFieldName == null || sortFieldName.isEmpty()) {
            sortFieldName = "id";
            sortOrder = "asc";
        }
        if (pageable.isPaged()) {
            Page<TemplateFile> page = templateFileDao.findTemplateFileEntries(pageable.getPageNumber(), pageable.getPageSize(), sortFieldName, sortOrder);
            uiModel.addAttribute("templatefiles", page);
        } else {
            uiModel.addAttribute("templatefiles", templateFileDao.findAllTemplateFiles(sortFieldName, sortOrder));
        }
        uiModel.addAttribute("allPosteCandidatureTags", posteCandidatureTagDao.findAllPosteCandidatureTags());
        uiModel.addAttribute("galaxieKeys", templateService.getGalaxieKeys());
        addDateTimeFormatPatterns(uiModel);
        return "admin/templatefiles/list";
    }

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String createdOrUpdate(@Valid TemplateFile templateFile, BindingResult bindingResult, Model uiModel) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, templateFile);
            return "admin/templatefiles/update";
        }
        templateFileDao.saveTemplateFile(templateFile);
        return "redirect:/admin/templatefiles";
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new TemplateFile());
        return "admin/templatefiles/update";
    }

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, templateFileDao.findTemplateFile(id));
        return "admin/templatefiles/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, RedirectAttributes redirectAttributes) {
        templateFileDao.deleteTemplateFile(id);
        redirectAttributes.addFlashAttribute("page", (page == null) ? "1" : page.toString());
        redirectAttributes.addFlashAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/templatefiles";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("templateFile_sendtime_date_format", "dd/MM/yyyy HH:mm");
    }

}
