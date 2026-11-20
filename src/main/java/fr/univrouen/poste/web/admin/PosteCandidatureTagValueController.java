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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        Map<Long, PosteCandidatureTag> tagsByValueId = new HashMap<>();
        for (PosteCandidatureTagValue value : page) {
            tagsByValueId.put(value.getId(), posteCandidatureTagDao.findPosteCandidatureTagsByValue(value));
        }
        uiModel.addAttribute("tagsByValueId", tagsByValueId);
        uiModel.addAttribute("candidaturevaluetags", page);
        return "admin/candidaturevaluetags/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_MANAGER')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, params = "form", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        uiModel.addAttribute("posteCandidatureTagValue", findValue(id));
        return "admin/candidaturevaluetags/update";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_MANAGER')")
    @Transactional
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "text/html")
    public String update(@PathVariable Long id, @RequestParam Integer version, @RequestParam String value) {
        PosteCandidatureTagValue tagValue = findValue(id);
        if (!Objects.equals(version, tagValue.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette valeur de tag a déjà été modifiée.");
        }
        tagValue.setValue(value);
        return "redirect:/admin/candidaturevaluetags";
    }

    private PosteCandidatureTagValue findValue(Long id) {
        PosteCandidatureTagValue value = posteCandidatureTagValueDao.findPosteCandidatureTagValue(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return value;
    }
}
