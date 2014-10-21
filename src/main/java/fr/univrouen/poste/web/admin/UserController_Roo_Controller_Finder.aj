// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.web.admin.UserController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

privileged aspect UserController_Roo_Controller_Finder {
    
    @RequestMapping(params = { "find=ByActivationKeyAndEmailAddress", "form" }, method = RequestMethod.GET)
    public String UserController.findUsersByActivationKeyAndEmailAddressForm(Model uiModel) {
        return "admin/users/findUsersByActivationKeyAndEmailAddress";
    }
    
    @RequestMapping(params = "find=ByActivationKeyAndEmailAddress", method = RequestMethod.GET)
    public String UserController.findUsersByActivationKeyAndEmailAddress(@RequestParam("activationKey") String activationKey, @RequestParam("emailAddress") String emailAddress, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findUsersByActivationKeyAndEmailAddress(activationKey, emailAddress, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countFindUsersByActivationKeyAndEmailAddress(activationKey, emailAddress) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findUsersByActivationKeyAndEmailAddress(activationKey, emailAddress, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
    }
    
    @RequestMapping(params = { "find=ByEmailAddress", "form" }, method = RequestMethod.GET)
    public String UserController.findUsersByEmailAddressForm(Model uiModel) {
        return "admin/users/findUsersByEmailAddress";
    }
    
    @RequestMapping(params = "find=ByEmailAddress", method = RequestMethod.GET)
    public String UserController.findUsersByEmailAddress(@RequestParam("emailAddress") String emailAddress, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findUsersByEmailAddress(emailAddress, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countFindUsersByEmailAddress(emailAddress) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findUsersByEmailAddress(emailAddress, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
    }
    
    @RequestMapping(params = { "find=ByEmailAddressAndActivationDateIsNotNull", "form" }, method = RequestMethod.GET)
    public String UserController.findUsersByEmailAddressAndActivationDateIsNotNullForm(Model uiModel) {
        return "admin/users/findUsersByEmailAddressAndActivationDateIsNotNull";
    }
    
    @RequestMapping(params = "find=ByEmailAddressAndActivationDateIsNotNull", method = RequestMethod.GET)
    public String UserController.findUsersByEmailAddressAndActivationDateIsNotNull(@RequestParam("emailAddress") String emailAddress, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findUsersByEmailAddressAndActivationDateIsNotNull(emailAddress, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countFindUsersByEmailAddressAndActivationDateIsNotNull(emailAddress) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findUsersByEmailAddressAndActivationDateIsNotNull(emailAddress, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
    }
    
    @RequestMapping(params = { "find=ByIsAdmin", "form" }, method = RequestMethod.GET)
    public String UserController.findUsersByIsAdminForm(Model uiModel) {
        return "admin/users/findUsersByIsAdmin";
    }
    
    @RequestMapping(params = "find=ByIsAdmin", method = RequestMethod.GET)
    public String UserController.findUsersByIsAdmin(@RequestParam(value = "isAdmin", required = false) Boolean isAdmin, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findUsersByIsAdmin(isAdmin == null ? Boolean.FALSE : isAdmin, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countFindUsersByIsAdmin(isAdmin == null ? Boolean.FALSE : isAdmin) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findUsersByIsAdmin(isAdmin == null ? Boolean.FALSE : isAdmin, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
    }
    
    @RequestMapping(params = { "find=ByIsManager", "form" }, method = RequestMethod.GET)
    public String UserController.findUsersByIsManagerForm(Model uiModel) {
        return "admin/users/findUsersByIsManager";
    }
    
    @RequestMapping(params = "find=ByIsManager", method = RequestMethod.GET)
    public String UserController.findUsersByIsManager(@RequestParam(value = "isManager", required = false) Boolean isManager, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findUsersByIsManager(isManager == null ? Boolean.FALSE : isManager, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countFindUsersByIsManager(isManager == null ? Boolean.FALSE : isManager) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findUsersByIsManager(isManager == null ? Boolean.FALSE : isManager, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
    }
    
    @RequestMapping(params = { "find=ByIsSuperManager", "form" }, method = RequestMethod.GET)
    public String UserController.findUsersByIsSuperManagerForm(Model uiModel) {
        return "admin/users/findUsersByIsSuperManager";
    }
    
    @RequestMapping(params = "find=ByIsSuperManager", method = RequestMethod.GET)
    public String UserController.findUsersByIsSuperManager(@RequestParam(value = "isSuperManager", required = false) Boolean isSuperManager, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findUsersByIsSuperManager(isSuperManager == null ? Boolean.FALSE : isSuperManager, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countFindUsersByIsSuperManager(isSuperManager == null ? Boolean.FALSE : isSuperManager) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findUsersByIsSuperManager(isSuperManager == null ? Boolean.FALSE : isSuperManager, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
    }
    
    @RequestMapping(params = { "find=ByNumCandidat", "form" }, method = RequestMethod.GET)
    public String UserController.findUsersByNumCandidatForm(Model uiModel) {
        return "admin/users/findUsersByNumCandidat";
    }
    
    @RequestMapping(params = "find=ByNumCandidat", method = RequestMethod.GET)
    public String UserController.findUsersByNumCandidat(@RequestParam("numCandidat") String numCandidat, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findUsersByNumCandidat(numCandidat, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countFindUsersByNumCandidat(numCandidat) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findUsersByNumCandidat(numCandidat, sortFieldName, sortOrder).getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
    }
    
}
