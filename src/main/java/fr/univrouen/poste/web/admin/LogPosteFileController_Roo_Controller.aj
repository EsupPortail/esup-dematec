// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.domain.LogPosteFile;
import fr.univrouen.poste.web.admin.LogPosteFileController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

privileged aspect LogPosteFileController_Roo_Controller {
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String LogPosteFileController.show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("logpostefile", LogPosteFile.findLogPosteFile(id));
        uiModel.addAttribute("itemId", id);
        return "admin/logpostefiles/show";
    }
    
    @RequestMapping(produces = "text/html")
    public String LogPosteFileController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logpostefiles", LogPosteFile.findLogPosteFileEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) LogPosteFile.countLogPosteFiles() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logpostefiles", LogPosteFile.findAllLogPosteFiles(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logpostefiles/list";
    }
    
    void LogPosteFileController.addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("logPosteFile_actiondate_date_format", "dd/MM/yyyy HH:mm");
    }
    
}
