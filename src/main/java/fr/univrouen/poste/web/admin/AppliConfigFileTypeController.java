package fr.univrouen.poste.web.admin;
import fr.univrouen.poste.domain.AppliConfigFileType;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/appliconfigfiletype")
@Controller
@RooWebScaffold(path = "admin/appliconfigfiletype", formBackingObject = AppliConfigFileType.class)
public class AppliConfigFileTypeController {	
    
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if(sortFieldName == null || sortFieldName.isEmpty()) {
        	sortFieldName = "id";
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
    
}
