// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.web.admin.AppliConfigController;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect AppliConfigController_Roo_Controller {
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String AppliConfigController.show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("appliconfig", AppliConfig.findAppliConfig(id));
        uiModel.addAttribute("itemId", id);
        return "admin/appliconfig/show";
    }
    
    @RequestMapping(produces = "text/html")
    public String AppliConfigController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("appliconfigs", AppliConfig.findAppliConfigEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) AppliConfig.countAppliConfigs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("appliconfigs", AppliConfig.findAllAppliConfigs(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/appliconfig/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String AppliConfigController.update(@Valid AppliConfig appliConfig, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, appliConfig);
            return "admin/appliconfig/update";
        }
        uiModel.asMap().clear();
        appliConfig.merge();
        return "redirect:/admin/appliconfig/" + encodeUrlPathSegment(appliConfig.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String AppliConfigController.updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, AppliConfig.findAppliConfig(id));
        return "admin/appliconfig/update";
    }
    
    void AppliConfigController.addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("appliConfig_dateendcandidat_date_format", "dd/MM/yyyy HH:mm");
        uiModel.addAttribute("appliConfig_dateendcandidatactif_date_format", "dd/MM/yyyy HH:mm");
        uiModel.addAttribute("appliConfig_dateendmembre_date_format", "dd/MM/yyyy HH:mm");
    }
    
    void AppliConfigController.populateEditForm(Model uiModel, AppliConfig appliConfig) {
        uiModel.addAttribute("appliConfig", appliConfig);
        addDateTimeFormatPatterns(uiModel);
    }
    
    String AppliConfigController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
    
}
