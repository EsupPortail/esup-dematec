package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.PosteCandidatureTagDao;
import fr.univrouen.poste.dao.PosteCandidatureTagValueDao;
import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/admin/candidaturevaluetags")
@Controller
public class PosteCandidatureTagValueController {

	@Resource
	PosteCandidatureTagDao posteCandidatureTagDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	PosteCandidatureTagValueDao posteCandidatureTagValueDao;
    
    @Transactional
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, final RedirectAttributes redirectAttributes) {
        PosteCandidatureTagValue posteCandidatureTagValue = posteCandidatureTagValueDao.findPosteCandidatureTagValue(id);
        PosteCandidatureTag tag = posteCandidatureTagDao.findPosteCandidatureTagsByValue(posteCandidatureTagValue);
        if(posteCandidatureDao.countFindPosteCandidaturesByTag(tag, posteCandidatureTagValue) == 0) {
	        tag.getValues().remove(posteCandidatureTagValue);
	        posteCandidatureTagValueDao.deletePosteCandidatureTagValue(posteCandidatureTagValue);
        } else {
        	redirectAttributes.addFlashAttribute("deleteFailed", "deleteFailed");
        }
        Long tagId = tag.getId();
        return "redirect:/admin/candidaturetags/" + tagId;
    }

    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, Model uiModel) {
        Page<PosteCandidatureTagValue> page = posteCandidatureTagValueDao.findPosteCandidatureTagValueEntries(pageable);
        uiModel.addAttribute("candidaturevaluetags", page);
        return "admin/candidaturevaluetags/list";
    }
}
