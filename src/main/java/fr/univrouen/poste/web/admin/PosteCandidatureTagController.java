package fr.univrouen.poste.web.admin;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;

@RequestMapping("/admin/candidaturetags")
@Controller
@RooWebScaffold(path = "admin/candidaturetags", formBackingObject = PosteCandidatureTag.class)
public class PosteCandidatureTagController {
	
	@Transactional
    @RequestMapping(value = "/{id}", params={"create=value"}, method = RequestMethod.POST, produces = "text/html")
    public String createValue(@PathVariable("id") Long id, @Valid PosteCandidatureTagValue posteCandidatureTagValue, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	PosteCandidatureTag posteCandidatureTag = PosteCandidatureTag.findPosteCandidatureTag(id);
    	if (bindingResult.hasErrors()) {
    		uiModel.addAttribute("posteCandidatureTag", posteCandidatureTag);
            uiModel.addAttribute("posteCandidatureTagValue", posteCandidatureTagValue);
            return "admin/candidaturevaluetags/create";
        }
        uiModel.asMap().clear();
        posteCandidatureTagValue.persist();
        posteCandidatureTag.getValues().add(posteCandidatureTagValue);
        return "redirect:/admin/candidaturetags/" + encodeUrlPathSegment(id.toString(), httpServletRequest);
    }
    
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, PosteCandidatureTag.findPosteCandidatureTag(id));
        uiModel.addAttribute("posteCandidatureTagValue", new PosteCandidatureTagValue());
        return "admin/candidaturetags/update";
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        return "redirect:/admin/candidaturetags/" + id + "?form";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid PosteCandidatureTag posteCandidatureTag, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, posteCandidatureTag);
            return "admin/candidaturetags/update";
        }
        uiModel.asMap().clear();
        PosteCandidatureTag posteCandidatureTagOld = PosteCandidatureTag.findPosteCandidatureTag(posteCandidatureTag.getId());
        posteCandidatureTag.setValues(posteCandidatureTagOld.getValues());
        posteCandidatureTag.merge();
        return "redirect:/admin/candidaturetags/" + encodeUrlPathSegment(posteCandidatureTag.getId().toString(), httpServletRequest);
    }
    
}

