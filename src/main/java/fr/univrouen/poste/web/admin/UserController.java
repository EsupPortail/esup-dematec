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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.time.LocalDateTime;


@RequestMapping("/admin/users")
@Controller
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
public class UserController {

    @Resource
    fr.univrouen.poste.services.SessionRevocationService sessionRevocationService;

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	UserDao userDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;
	
    @Resource
    PasswordEncoder passwordEncoder;

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid User user, BindingResult result, Model model) {
        requireUserManagementPermission();
        if (!fr.univrouen.poste.utils.CredentialValidation.validPassword(user.getPassword())) {
            result.rejectValue("password", "changepassword.invalidpassword");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            logger.error("Error when creating an user : " + result.getGlobalError());
            return "admin/users/create";
        }
        if (user.getId() != null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Use PUT to update an existing user");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getActivationDate() == null) user.setActivationDate(LocalDateTime.now());
        userDao.saveUser(user);
        return "redirect:/admin/users/" + user.getId().toString();
    }
    
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid User user, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        requireUserManagementPermission();
        if ((user.getId() == null || org.springframework.util.StringUtils.hasLength(user.getPassword()))
                && !fr.univrouen.poste.utils.CredentialValidation.validPassword(user.getPassword())) {
            bindingResult.rejectValue("password", "changepassword.invalidpassword");
        }
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, user);
            return "admin/users/update";
        }
        String previousEmail = null;
        if (user.getId() != null) {
            User savedUser = userDao.findUser(user.getId());
            if (savedUser == null) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
            }
            previousEmail = savedUser.getEmailAddress();
            if(StringUtils.hasLength(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                if(user.getActivationDate() == null) {
                	user.setActivationDate(LocalDateTime.now());
                }
                user.setActivationKey(null);
            } else {
                user.setPassword(savedUser.getPassword());
                user.setActivationDate(savedUser.getActivationDate());
                user.setActivationKey(savedUser.getActivationKey());
            }
            user.setLoginFailedTime(savedUser.getLoginFailedTime());
            user.setPostes(savedUser.getPostes());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if(user.getActivationDate() == null) {
            	user.setActivationDate(LocalDateTime.now());
            }
        }
        userDao.saveUser(user);
        sessionRevocationService.revoke(previousEmail);
        return "redirect:/admin/users/" + encodeUrlPathSegment(user.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        requireUserManagementPermission();
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
        User user = userDao.findUser(id);
        requireUserManagementPermission();
        populateEditForm(uiModel, user);
        return "admin/users/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, RedirectAttributes redirectAttributes) {
        User user = userDao.findUser(id);
        requireUserManagementPermission();
        userDao.deleteUser(user);
        sessionRevocationService.revoke(user.getEmailAddress());
        redirectAttributes.addFlashAttribute("page", (page == null) ? "1" : page.toString());
        redirectAttributes.addFlashAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/users";
    }

    @ModelAttribute("canManagePrivileges")
    public boolean canManagePrivileges() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(authority ->
                    "ROLE_ADMIN".equals(authority.getAuthority())
                    || "ROLE_SUPER_MANAGER".equals(authority.getAuthority()));
    }

    /** The web interface reserves all user mutations for ADMIN and SUPER_MANAGER. */
    private void requireUserManagementPermission() {
        if (!canManagePrivileges()) {
            throw new AccessDeniedException("Seuls les administrateurs et super-gestionnaires peuvent modifier les comptes utilisateurs.");
        }
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

