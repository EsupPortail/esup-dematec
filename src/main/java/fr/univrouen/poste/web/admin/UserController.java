/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.web.admin;

import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.util.Date;

@RequestMapping("/admin/users")
@Controller
public class UserController {

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	UserDao userDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;
	
    @Resource
    PasswordEncoder passwordEncoder;

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid User user, BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            logger.error("Error when creating an user : " + result.getGlobalError());
            return "admin/users/create";
        }
        if (user.getId() != null) {
            User savedUser = userDao.findUser(user.getId());
            if (!savedUser.getPassword().equals(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                if(user.getActivationDate() == null) {
                	user.setActivationDate(new Date());
                }
            }
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if(user.getActivationDate() == null) {
            	user.setActivationDate(new Date());
            }
        }
        userDao.saveUser(user);
        return "redirect:/admin/users/" + user.getId().toString();
    }
    
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid User user, BindingResult bindingResult, Model uiModel, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, user);
            return "admin/users/update";
        }
        if (user.getId() != null) {
            User savedUser = userDao.findUser(user.getId());
            if (!user.getPassword().equals(savedUser.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                if(user.getActivationDate() == null) {
                	user.setActivationDate(new Date());
                }
            }
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if(user.getActivationDate() == null) {
            	user.setActivationDate(new Date());
            }
        }
        userDao.saveUser(user);
        return "redirect:/admin/users/" + encodeUrlPathSegment(user.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new User());
        return "admin/users/create";
    }

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String show(@PathVariable Long id, Model uiModel) {
        uiModel.addAttribute("user", userDao.findUser(id));
        uiModel.addAttribute("itemId", id);
        return "admin/users/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault Pageable pageable, Model uiModel,
                       @RequestParam(value="status", required = false) String status,
                       @RequestParam(value="nomOrPrenomOrEmailAddress", required = false) String nomOrPrenomOrEmailAddress
        ) {
        Page<User> result = userDao.findUserEntries(status, nomOrPrenomOrEmailAddress, pageable);
        uiModel.addAttribute("users", result);
        uiModel.addAttribute("status", status);
        uiModel.addAttribute("nomOrPrenomOrEmailAddress", nomOrPrenomOrEmailAddress);
        return "admin/users/list";
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, userDao.findUser(id));
        return "admin/users/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, RedirectAttributes redirectAttributes) {
        User user = userDao.findUser(id);
        userDao.deleteUser(user);
        redirectAttributes.addFlashAttribute("page", (page == null) ? "1" : page.toString());
        redirectAttributes.addFlashAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/users";
    }

	void populateEditForm(Model uiModel, User user) {
        uiModel.addAttribute("user", user);
        uiModel.addAttribute("posteapourvoirs", posteAPourvoirDao.findAllPosteAPourvoirs());
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

