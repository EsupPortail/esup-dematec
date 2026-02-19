package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.dao.AppliConfigFileTypeDao;
import fr.univrouen.poste.dao.PosteCandidatureFileDao;
import fr.univrouen.poste.domain.AppliConfigFileType;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/admin/appliconfigfiletype")
@Controller
public class AppliConfigFileTypeController {

	@Resource
	AppliConfigFileTypeDao appliConfigFileTypeDao;
	
	@Resource
	PosteCandidatureFileDao posteCandidatureFileDao;
	
    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, Model uiModel) {
        Page<AppliConfigFileType> page = appliConfigFileTypeDao.findAllAppliConfigFileTypes(pageable);
        uiModel.addAttribute("appliconfigfiletypes", page);
        return "admin/appliconfigfiletype/list";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    	AppliConfigFileType appliConfigFileType = appliConfigFileTypeDao.findAppliConfigFileType(id);
        if(posteCandidatureFileDao.countFindPosteCandidatureFilesByFileType(appliConfigFileType)>0) {
        	redirectAttributes.addFlashAttribute("deleteErrorCandidaturesExist", "true");
        } else {
        	appliConfigFileTypeDao.delete(appliConfigFileType);
        }
        return "redirect:/admin/appliconfigfiletype";
    }
    

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid AppliConfigFileType appliConfigFileType, BindingResult bindingResult, Model uiModel) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, appliConfigFileType);
            return "admin/appliconfigfiletype/create";
        }
        appliConfigFileTypeDao.saveAppliConfigFileType(appliConfigFileType);
        return "redirect:/admin/appliconfigfiletype";
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new AppliConfigFileType());
        return "admin/appliconfigfiletype/create";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid AppliConfigFileType appliConfigFileType, BindingResult bindingResult, Model uiModel) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, appliConfigFileType);
            return "admin/appliconfigfiletype/update";
        }
        appliConfigFileTypeDao.saveAppliConfigFileType(appliConfigFileType);
        return "redirect:/admin/appliconfigfiletype";
    }

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, appliConfigFileTypeDao.findAppliConfigFileType(id));
        return "admin/appliconfigfiletype/update";
    }

	void populateEditForm(Model uiModel, AppliConfigFileType appliConfigFileType) {
        uiModel.addAttribute("appliConfigFileType", appliConfigFileType);
    }
}
