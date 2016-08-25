package fr.univrouen.poste.web.admin;
import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.domain.PosteCandidatureFile;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/appliconfigfiletype")
@Controller
@RooWebScaffold(path = "admin/appliconfigfiletype", formBackingObject = AppliConfigFileType.class)
public class AppliConfigFileTypeController {	
    
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if(sortFieldName == null || sortFieldName.isEmpty()) {
        	sortFieldName = "listIndex, id";
        	sortOrder = "asc";
        }
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("appliconfigfiletypes", AppliConfigFileType.findAppliConfigFileTypeEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) AppliConfigFileType.countAppliConfigFileTypes() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("appliconfigfiletypes", AppliConfigFileType.findAllAppliConfigFileTypes(sortFieldName, sortOrder));
        }
        return "admin/appliconfigfiletype/list";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	uiModel.asMap().clear();
    	AppliConfigFileType appliConfigFileType = AppliConfigFileType.findAppliConfigFileType(id);
        if(PosteCandidatureFile.countFindPosteCandidatureFilesByFileType(appliConfigFileType)>0) {
        	uiModel.addAttribute("deleteErrorCandidaturesExist", "true");
        } else {
        	appliConfigFileType.remove();
        }
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/appliconfigfiletype";
    }
    
}
