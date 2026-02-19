package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.PosteCandidatureTagDao;
import fr.univrouen.poste.dao.PosteCandidatureTagValueDao;
import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/admin/candidaturetags")
@Controller
public class PosteCandidatureTagController {

	@Resource
	PosteCandidatureTagDao posteCandidatureTagDao;
	
	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	PosteCandidatureTagValueDao posteCandidatureTagValueDao;

	@Transactional
    @RequestMapping(value = "/{tagId}", params={"create=value"}, method = RequestMethod.POST, produces = "text/html")
    public String createValue(@PathVariable Long tagId, @Valid PosteCandidatureTagValue posteCandidatureTagValue, BindingResult bindingResult, Model uiModel, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
    	PosteCandidatureTag posteCandidatureTag = posteCandidatureTagDao.findPosteCandidatureTag(tagId);
    	if (bindingResult.hasErrors()) {
    		uiModel.addAttribute("posteCandidatureTag", posteCandidatureTag);
            uiModel.addAttribute("posteCandidatureTagValue", posteCandidatureTagValue);
            return "admin/candidaturevaluetags/create";
        }
        posteCandidatureTagValueDao.savePosteCandidatureTagValue(posteCandidatureTagValue);
        posteCandidatureTag.getValues().add(posteCandidatureTagValue);
        return "redirect:/admin/candidaturetags/" + encodeUrlPathSegment(tagId.toString(), httpServletRequest);
    }
    
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, posteCandidatureTagDao.findPosteCandidatureTag(id));
        uiModel.addAttribute("posteCandidatureTagValue", new PosteCandidatureTagValue());
        return "admin/candidaturetags/update";
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String show(@PathVariable Long id, Model uiModel) {
        return "redirect:/admin/candidaturetags/" + id + "?form";
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid PosteCandidatureTag posteCandidatureTag, BindingResult bindingResult, Model uiModel, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, posteCandidatureTag);
            return "admin/candidaturetags/update";
        }
        PosteCandidatureTag posteCandidatureTagOld = posteCandidatureTagDao.findPosteCandidatureTag(posteCandidatureTag.getId());
        posteCandidatureTag.setValues(posteCandidatureTagOld.getValues());
        posteCandidatureTagDao.savePosteCandidatureTag(posteCandidatureTag);
        return "redirect:/admin/candidaturetags/" + encodeUrlPathSegment(posteCandidatureTag.getId().toString(), httpServletRequest);
    }
    
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, final RedirectAttributes redirectAttributes) {
        PosteCandidatureTag posteCandidatureTag = posteCandidatureTagDao.findPosteCandidatureTag(id);
        if(posteCandidatureDao.countFindPosteCandidaturesByTag(posteCandidatureTag, null) == 0) {
        	posteCandidatureTagDao.deletePosteCandidatureTag(posteCandidatureTag);
        } else {
        	redirectAttributes.addFlashAttribute("deleteFailed", "deleteFailed");
        }
        return "redirect:/admin/candidaturetags";
    }
    

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid PosteCandidatureTag posteCandidatureTag, BindingResult bindingResult, Model uiModel, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, posteCandidatureTag);
            return "admin/candidaturetags/create";
        }
        posteCandidatureTagDao.savePosteCandidatureTag(posteCandidatureTag);
        return "redirect:/admin/candidaturetags/" + encodeUrlPathSegment(posteCandidatureTag.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new PosteCandidatureTag());
        return "admin/candidaturetags/create";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, Model uiModel) {
        Page<PosteCandidatureTag> page = posteCandidatureTagDao.findPosteCandidatureTagEntries(pageable);
        uiModel.addAttribute("candidaturetags", page);
        return "admin/candidaturetags/list";
    }

	void populateEditForm(Model uiModel, PosteCandidatureTag posteCandidatureTag) {
        uiModel.addAttribute("posteCandidatureTag", posteCandidatureTag);
        uiModel.addAttribute("postecandidaturetagvalues", posteCandidatureTagValueDao.findAllPosteCandidatureTagValues());
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
}
