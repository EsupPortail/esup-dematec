package fr.univrouen.poste.web.admin;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;

@RequestMapping("/admin/candidaturevaluetags")
@Controller
@RooWebScaffold(path = "admin/candidaturevaluetags", formBackingObject = PosteCandidatureTagValue.class)
public class PosteCandidatureTagValueController {
	
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
    	PosteCandidatureTagValue tagValue= PosteCandidatureTagValue.findPosteCandidatureTagValue(id);
    	PosteCandidatureTag tag = PosteCandidatureTag.findPosteCandidatureTagsByValue(tagValue);
    	Long tagId = tag.getId();
        return "redirect:/admin/candidaturetags/" + tagId;
    }
    
    @Transactional
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        PosteCandidatureTagValue posteCandidatureTagValue = PosteCandidatureTagValue.findPosteCandidatureTagValue(id);
        PosteCandidatureTag tag = PosteCandidatureTag.findPosteCandidatureTagsByValue(posteCandidatureTagValue);
        tag.getValues().remove(posteCandidatureTagValue);
        posteCandidatureTagValue.remove();
        uiModel.asMap().clear();
        Long tagId = tag.getId();
        return "redirect:/admin/candidaturetags/" + tagId;
    }
}

